package com.commerce.pal.backend.controller.user.auth;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.common.security.JwtTokenUtil;
import com.commerce.pal.backend.common.security.JwtUserDetailsService;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.referral_codes.ReferralType;
import com.commerce.pal.backend.module.AuthenticationService;
import com.commerce.pal.backend.module.DistributorService;
import com.commerce.pal.backend.module.database.RegistrationStoreService;
import com.commerce.pal.backend.module.users.AgentService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.module.users.MessengerService;
import com.commerce.pal.backend.module.users.business.BusinessService;
import com.commerce.pal.backend.module.users.referral_codes.ReferralCodeUtils;
import com.commerce.pal.backend.objects.LoginPayload;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.repo.user.*;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import com.commerce.pal.backend.utils.HttpProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1"})
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class AuthenticationController {

    @Value(value = "${commerce.pal.payment.promotion.url}")
    private String promoUrl;

    @Value(value = "${org.java.trial.attempts}")
    private Integer trialsAttempt;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    private final HttpProcessor httpProcessor;
    private final AgentService agentService;
    private final GlobalMethods globalMethods;
    private final MerchantService merchantService;
    private final BusinessService businessService;
    private final AgentRepository agentRepository;
    private final MessengerService messengerService;
    private final CustomerRepository customerRepository;
    private final DistributorService distributorService;
    private final BusinessRepository businessRepository;
    private final MerchantRepository merchantRepository;
    private final MessengerRepository messengerRepository;
    private final DistributorRepository distributorRepository;
    private final RegistrationStoreService registrationStoreService;
    private final LoginValidationRepository loginValidationRepository;
    private final AuthenticationService authenticationService;
    private final ReferralCodeUtils referralCodeUtils;


    @RequestMapping(value = "/auth-user", method = RequestMethod.POST)
    public ResponseEntity<?> firstLogin(@RequestBody String authRequest) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBdy = new JSONObject(authRequest);
            loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumber(
                            reqBdy.getString("user"), reqBdy.getString("user"))
                    .ifPresentOrElse(userLogin -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("changePin", userLogin.getPinChange())
                                .put("isPhoneValidated", userLogin.getIsPhoneValidated())
                                .put("isEmailValidated", userLogin.getIsEmailValidated())
                                .put("statusMessage", "successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_LOGIN_NOT_SUCCESSFUL)
                                .put("statusDescription", "failed invalid details")
                                .put("statusMessage", "failed invalid details");
                    });
        } catch (Exception e) {
            log.log(Level.INFO, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());

    }

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> userAuthentication(HttpServletRequest request,
                                                @Valid @RequestBody LoginPayload authenticationRequest) {
        JSONObject responseMap = new JSONObject();

        try {
            authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        } catch (Exception e) {
            log.log(Level.WARNING, "Login Error : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_LOGIN_NOT_SUCCESSFUL)
                    .put("statusDescription", "login failed invalid details")
                    .put("statusMessage", "login failed invalid details");

            loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumber(authenticationRequest.getEmail(), authenticationRequest.getEmail())
                    .ifPresent(userLogin -> {
                        userLogin.setPinAttempt(userLogin.getPinAttempt() + 1);
                        userLogin.setLastAttemptDate(Timestamp.from(Instant.now()));
                        if (userLogin.getPinAttempt() > trialsAttempt) {
                            userLogin.setStatus(5);
                            responseMap.put("message", "You have reached maximum trials. Contact Admin for Password Reset");
                        }
                        responseMap.put("message", "Invalid Credentials.");
                        loginValidationRepository.save(userLogin);
                    });
            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
        try {

            loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumber(authenticationRequest.getEmail(), authenticationRequest.getEmail())
                    .ifPresent(userLogin -> {
                        final UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getEmailAddress());
                        String token = jwtTokenUtil.generateToken(userDetails);
                        userLogin.setPinAttempt(0);
                        userLogin.setLastAttemptDate(Timestamp.from(Instant.now()));
                        loginValidationRepository.save(userLogin);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("userToken", token)
                                .put("refreshToken", token)
                                .put("changePin", userLogin.getPinChange())
                                .put("isEmailValidated", userLogin.getIsEmailValidated())
                                .put("isPhoneValidated", userLogin.getIsPhoneValidated())
                                .put("statusMessage", "login successful");
                    });
        } catch (Exception e) {
            log.log(Level.INFO, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());

    }

    @PostMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> changePassword(@RequestBody String changePasswordRequest) {
        JSONObject responseMap = new JSONObject();

        JSONObject reqJson = new JSONObject(changePasswordRequest);
        LoginValidation loginValidation = globalMethods.fetchUserDetails();

//        boolean isPasswordMatch = bcryptEncoder.matches(reqJson.getString("oldPassword"), loginValidation.getPinHash());
//        if (!isPasswordMatch)
//            throw new BadRequestException("Incorrect old Password");

        loginValidation.setPinHash(bcryptEncoder.encode(reqJson.getString("newPassword")));
        loginValidation.setPinChange(1);
        loginValidationRepository.save(loginValidation);

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Password changed successfully.");

        return ResponseEntity.ok(responseMap.toString());
    }

    @PostMapping(value = "/password-reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> passwordReset(@RequestBody String requestBody) {
        JSONObject responseMap = authenticationService.passwordResetReq(requestBody);
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/password-type-reset"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> passwordResetType(@RequestBody String requestBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(requestBody);
            String userValue = jsonObject.getString("user");
            loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumber(userValue, userValue)
                    .ifPresentOrElse(user -> {
                        String code = globalMethods.getMobileValidationCode();
                        String token = code + "-" + UUID.randomUUID().toString();
                        user.setPasswordResetToken(token);
                        user.setPasswordResetTokenStatus(0);
                        user.setPasswordResetTokenExpire(Timestamp.from(Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(30))));
                        loginValidationRepository.save(user);

                        String userEmail = user.getEmailAddress();
                        JSONObject emailPayload = new JSONObject();
                        emailPayload.put("HasTemplate", "YES");
                        emailPayload.put("TemplateName", "reset-pin-request");
                        emailPayload.put("name", globalMethods.getMultiUserCustomer(userEmail).getString("firstName"));
                        emailPayload.put("otp", code);
                        emailPayload.put("email", userEmail);
                        emailPayload.put("EmailDestination", userEmail);
                        emailPayload.put("EmailSubject", "PASSWORD RESET");
                        emailPayload.put("EmailMessage", "Password Reset");
                        globalMethods.sendEmailNotification(emailPayload);

                        JSONObject smsBody = new JSONObject();
                        smsBody.put("TemplateId", "9");
                        smsBody.put("TemplateLanguage", "en");
                        smsBody.put("Phone", globalMethods.getMultiUserCustomer(user.getEmailAddress()).getString("phoneNumber"));
                        smsBody.put("otp", code);
                        smsBody.put("hash", globalMethods.getHashkey());
                        globalMethods.sendSMSNotification(smsBody);

                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Password Reset request was successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("errorDescription", "User does not exist")
                                .put("statusMessage", "User does not exist");
                    });
        } catch (Exception ex) {
            log.log(Level.WARNING, "Pass Reset Error : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/confirm-code"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> confirmCode(@RequestBody String code) {
        log.log(Level.INFO, "CODE REQUEST  : " + code);
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(code);
            loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumberAndPasswordResetTokenStatus(
                            jsonObject.getString("user"), jsonObject.getString("user"), 0)
                    .ifPresentOrElse(user -> {
                        if (user.getPasswordResetTokenExpire().equals(Timestamp.from(Instant.now()))) {
                        }
                        if (!user.getPasswordResetToken().substring(0, 4).equals(jsonObject.getString("code"))) {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "Code is not valid or expired")
                                    .put("errorDescription", "Code is not valid or expired")
                                    .put("statusMessage", "Code is not valid or expired");
                        } else {
                            user.setStatus(1);
                            loginValidationRepository.save(user);

                            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmailAddress());
                            String userToken = jwtTokenUtil.generateToken(userDetails);
                            responseMap.put("jwttoken", userToken);
                            user.setPasswordResetTokenStatus(1);
                            loginValidationRepository.save(user);

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("statusMessage", "Change Password was successful");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Code is not valid or expired")
                                .put("errorDescription", "Code is not valid or expired")
                                .put("statusMessage", "Token is not valid or expired");
                    });
        } catch (Exception ex) {
            log.log(Level.WARNING, "Confirm Code Error : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());

        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/reset-password"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody String requestBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(requestBody);
            LoginValidation user = globalMethods.fetchUserDetails();
            user.setPinHash(bcryptEncoder.encode(jsonObject.getString("password")));
            user.setPinAttempt(0);
            user.setPinChange(1);
            user.setLastAttemptDate(Timestamp.from(Instant.now()));
            loginValidationRepository.save(user);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Change Password was successful");
        } catch (Exception ex) {
            log.log(Level.WARNING, "Change Password Error : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @PostMapping(value = "/validate-type-request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> validateTypeRequest(@RequestBody String requestBody) {
        JSONObject jsonObject = new JSONObject(requestBody);
        String userValue = jsonObject.getString("user");
        String type = jsonObject.getString("type");

        if (!Objects.equals(type, "EMAIL") && !Objects.equals(type, "PHONE"))
            throw new IllegalArgumentException("Type must be EMAIL or PHONE");

        JSONObject responseMap = new JSONObject();
        loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumber(userValue, userValue)
                .ifPresentOrElse(user -> {
                    jsonObject.put("msisdn", user.getPhoneNumber());
                    if (jsonObject.getString("type").equals("EMAIL")) {
                        String otpEmailCode = globalMethods.getMobileValidationCode();
                        String userEmail = user.getEmailAddress();
                        JSONObject emailPayload = new JSONObject();
                        emailPayload.put("HasTemplate", "YES");
                        emailPayload.put("TemplateName", "reset-pin-request");
                        emailPayload.put("name", globalMethods.getMultiUserCustomer(userEmail).getString("firstName"));
                        emailPayload.put("otp", otpEmailCode);
                        emailPayload.put("email", userEmail);
                        emailPayload.put("EmailDestination", userEmail);
                        emailPayload.put("EmailSubject", "PASSWORD RESET");
                        emailPayload.put("EmailMessage", "Password Reset");
                        globalMethods.sendEmailNotification(emailPayload);
                        jsonObject.put("otpEmailCode", otpEmailCode);
                        jsonObject.put("otpCode", "Code");
                        jsonObject.put("PhoneOtp", "0");
                        jsonObject.put("EmailOtp", "1");
                    } else {
                        String code = globalMethods.getMobileValidationCode();
                        jsonObject.put("otpCode", code);
                        jsonObject.put("otpEmailCode", "otpEmailCode");
                        jsonObject.put("PhoneOtp", "1");
                        jsonObject.put("EmailOtp", "0");

                        JSONObject userData = globalMethods.getMultiUserCustomer(user.getPhoneNumber());
                        JSONObject smsBody = new JSONObject();
                        smsBody.put("TemplateId", "16");
                        smsBody.put("TemplateLanguage", "en");
                        smsBody.put("Phone", userData.getString("phoneNumber"));
                        smsBody.put("firstName", userData.getString("firstName"));
                        smsBody.put("otp", code);
                        smsBody.put("hash", globalMethods.getHashkey());
                        globalMethods.sendSMSNotification(smsBody);
                    }

                    registrationStoreService.pinOtp(jsonObject);

                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("statusMessage", "Account validation Reset request was successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("errorDescription", "User does not exist")
                            .put("statusMessage", "User does not exist");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/validate-confirm-otp"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> validateConfirmOtp(@RequestBody String code) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(code);
            loginValidationRepository.findLoginValidationByEmailAddressOrPhoneNumber(jsonObject.getString("user"), jsonObject.getString("user"))
                    .ifPresentOrElse(user -> {
                        if (user.getOtpHash().equals(jsonObject.getString("code")) && jsonObject.getString("type").equals("PHONE")) {
                            user.setStatus(1);
                            loginValidationRepository.save(user);

                            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmailAddress());
                            String userToken = jwtTokenUtil.generateToken(userDetails);
                            responseMap.put("jwttoken", userToken);
                            user.setPasswordResetTokenStatus(1);
                            user.setIsPhoneValidated(1);
                            loginValidationRepository.save(user);

                            JSONObject promo = new JSONObject();
                            promo.put("Phone", user.getPhoneNumber());
                            promo.put("Device", user.getDeviceId());
                            RequestBuilder builder = new RequestBuilder("POST");
                            builder.addHeader("Content-Type", "application/json")
                                    .setBody(promo.toString())
                                    .setUrl(promoUrl)
                                    .build();
                            httpProcessor.processProperRequest(builder);

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("statusMessage", "Phone validation was successful");
                        } else if (user.getEmailOtpHash().equals(jsonObject.getString("code")) && jsonObject.getString("type").equals("EMAIL")) {
                            user.setStatus(1);
                            loginValidationRepository.save(user);

                            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmailAddress());
                            String userToken = jwtTokenUtil.generateToken(userDetails);
                            responseMap.put("jwttoken", userToken);
                            user.setPasswordResetTokenStatus(1);
                            user.setIsEmailValidated(1);
                            loginValidationRepository.save(user);

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("statusMessage", "Email validation was successful");
                        } else {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "Code is not valid or expired")
                                    .put("errorDescription", "Code is not valid or expired")
                                    .put("statusMessage", "Code is not valid or expired");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Code is not valid or expired")
                                .put("errorDescription", "Code is not valid or expired")
                                .put("statusMessage", "Token is not valid or expired");
                    });
        } catch (Exception ex) {
            log.log(Level.WARNING, "Confirm Code Error : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());

        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/get-details"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getUserDetails() {
        JSONObject responseMap = new JSONObject();
        try {
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "success");
            LoginValidation user = globalMethods.fetchUserDetails();

            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresent(customer -> {
                        JSONObject customerData = new JSONObject();
                        customerData.put("userId", customer.getCustomerId());
                        customerData.put("firstName", customer.getFirstName());
                        customerData.put("lastName", customer.getLastName());
                        customerData.put("language", customer.getLanguage());
                        customerData.put("phoneNumber", customer.getPhoneNumber());
                        customerData.put("email", customer.getEmailAddress());
                        customerData.put("customerAccountNumber", customer.getAccountNumber());
                        customerData.put("customerCommissionAccount", customer.getAccountNumber().concat("1"));
                        customerData.put("city", customer.getCity());
                        customerData.put("country", customer.getCountry());
                        customerData.put("district", customer.getDistrict());
                        customerData.put("location", customer.getLocation());
                        customerData.put("oneSignalToken", user.getUserOneSignalId() != null ? user.getUserOneSignalId() : "12WEQWE21313");
                        responseMap.put("Details", customerData);
                    });
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        JSONObject merchantInfo = merchantService.getMerchantInfo(merchant.getMerchantId());
                        responseMap.put("merchantInfo", merchantInfo);
                        responseMap.put("IsMerchant", "YES");
                    }, () -> {
                        responseMap.put("IsMerchant", "NO");
                    });
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        JSONObject businessInfo = businessService.getBusinessInfo(business.getBusinessId());
                        responseMap.put("businessInfo", businessInfo);
                        responseMap.put("IsBusiness", "YES");
                    }, () -> {
                        responseMap.put("IsBusiness", "NO");
                    });
            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        JSONObject agentInfo = agentService.getAgentInfo(agent.getAgentId());
                        responseMap.put("agentInfo", agentInfo);
                        responseMap.put("IsAgent", "YES");
                    }, () -> {
                        responseMap.put("IsAgent", "NO");
                    });
            messengerRepository.findMessengerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(messenger -> {
                        JSONObject messengerInfo = messengerService.getMessengerInfo(messenger.getMessengerId());
                        responseMap.put("messengerInfo", messengerInfo);
                        responseMap.put("IsMessenger", "YES");
                    }, () -> {
                        responseMap.put("IsMessenger", "NO");
                    });

            distributorRepository.findDistributorByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(distributor -> {
                        JSONObject distributorInfo = distributorService.getDistributorInfo(distributor.getDistributorId());
                        responseMap.put("distributorInfo", distributorInfo);
                        responseMap.put("IsDistributor", "YES");
                    }, () -> {
                        responseMap.put("IsDistributor", "NO");
                    });
        } catch (Exception ex) {
            log.log(Level.INFO, ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/validate-token"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> validateToken() {
        JSONObject responseMap = new JSONObject();
        try {
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "success");
            LoginValidation user = globalMethods.fetchUserDetails();

            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresent(customer -> {
                        JSONObject customerData = new JSONObject();
                        customerData.put("userId", customer.getCustomerId());
                        customerData.put("firstName", customer.getFirstName());
                        customerData.put("lastName", customer.getLastName());
                        customerData.put("language", customer.getLanguage());
                        customerData.put("phoneNumber", customer.getPhoneNumber());
                        customerData.put("email", customer.getEmailAddress());
                        customerData.put("city", customer.getCity());
                        customerData.put("country", customer.getCountry());
                        customerData.put("district", customer.getDistrict());
                        customerData.put("location", customer.getLocation());
                        customerData.put("oneSignalToken", user.getUserOneSignalId() != null ? user.getUserOneSignalId() : "12WEQWE21313");
                        responseMap.put("Details", customerData);
                    });
        } catch (Exception ex) {
            log.log(Level.INFO, ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/user-language"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getUserLanguage() {
        JSONObject responseMap = new JSONObject();
        try {
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "success");
            LoginValidation user = globalMethods.fetchUserDetails();
            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        responseMap.put("Language", customer.getLanguage());
                    }, () -> {
                        responseMap.put("Language", "en");
                    });
        } catch (Exception ex) {
            log.log(Level.INFO, ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/update-one-signal"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> updateOneSignalId(@RequestBody String requestBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(requestBody);
            LoginValidation user = globalMethods.fetchUserDetails();
            user.setUserOneSignalId(jsonObject.getString("UserId"));
            loginValidationRepository.save(user);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "successful");
        } catch (Exception ex) {
            log.log(Level.WARNING, "Change Password Error : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", ex.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }


    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ResponseEntity<?> customerRegistration(@RequestBody String registration) {
        JSONObject responseMap = new JSONObject();
        log.log(Level.INFO, registration);

        try {
            JSONObject request = new JSONObject(registration);
            Long referringUserId = 0L;
            String referringUserType = "";
            String transRef = "";

            if (request.has("referralCode")) {
                referringUserType = referralCodeUtils.getReferringUserType(request.getString("referralCode"));
                referringUserId = referralCodeUtils.getReferringUserId(request.getString("referralCode"));
                transRef = globalMethods.generateTrans();
            }

            String customerReferralCode = referralCodeUtils.generateCustomerReferralCode();

            request.put("referralCode", customerReferralCode);
            request.put("referringUserId", referringUserId);
            request.put("referringUserType", referringUserType);
            request.put("TransRef", transRef);

            String password = globalMethods.getGeneratePassword();
            String passwordHash = bcryptEncoder.encode(password);
            request.put("password", passwordHash);
            String otpCode = globalMethods.getMobileValidationCode();
            request.put("otpCode", otpCode);
            String otpEmailCode = globalMethods.getMobileValidationCode();
            request.put("otpEmailCode", otpEmailCode);

            responseMap = registrationStoreService.doCustomerRegistration(request);

            int exists = responseMap.getInt("exists");
            String userId = globalMethods.getUserId("CUSTOMER", request.getString("email").trim());

            if (exists == 1) {
                responseMap.put("statusCode", ResponseCodes.REGISTERED)
                        .put("statusDescription", "email address already registered")
                        .put("statusMessage", "registration exists");
            } else if (exists == -1) {
                JSONObject smsBody = new JSONObject();
                smsBody.put("TemplateId", "1");
                smsBody.put("TemplateLanguage", "en");
                smsBody.put("Phone", request.getString("msisdn").substring(request.getString("msisdn").length() - 9));
                globalMethods.sendSMSNotification(smsBody);

                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("userId", userId)
                        .put("statusDescription", "success. Use current login details")
                        .put("statusMessage", "registration successful. Use current login details");
            } else {
                request.put("PhoneOtp", "1");
                request.put("EmailOtp", "1");

                registrationStoreService.pinOtp(request);

                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("userId", userId)
                        .put("statusDescription", "success")
                        .put("statusMessage", "registration successful");


                JSONObject emailPayload = new JSONObject();
                emailPayload.put("HasTemplate", "YES");
                emailPayload.put("TemplateName", "registration");
                emailPayload.put("name", request.getString("firstName"));
                emailPayload.put("password", password);
                emailPayload.put("EmailDestination", request.getString("email").trim());
                emailPayload.put("EmailSubject", "COMMERCE PAL REGISTRATION");
                emailPayload.put("EmailMessage", "Password Reset");
                globalMethods.sendEmailNotification(emailPayload);


                JSONObject smsBody = new JSONObject();
                smsBody.put("TemplateId", "1");
                smsBody.put("TemplateLanguage", "en");
                smsBody.put("otp", password);
                smsBody.put("Phone", request.getString("msisdn").substring(request.getString("msisdn").length() - 9));
                smsBody.put("hash", globalMethods.getHashkey());
                globalMethods.sendSMSNotification(smsBody);
            }

        } catch (Exception e) {
            log.log(Level.WARNING, "User Registration Error : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("errorDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
