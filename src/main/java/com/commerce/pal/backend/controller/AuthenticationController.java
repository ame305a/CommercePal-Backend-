package com.commerce.pal.backend.controller;

import com.commerce.pal.backend.common.JwtTokenUtil;
import com.commerce.pal.backend.common.JwtUserDetailsService;
import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.database.RegistrationStoreService;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.DistributorService;
import com.commerce.pal.backend.module.users.AgentService;
import com.commerce.pal.backend.module.users.business.BusinessService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.module.users.MessengerService;
import com.commerce.pal.backend.objects.LoginPayload;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.repo.user.*;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1"})
@SuppressWarnings("Duplicates")
public class AuthenticationController {

    @Value(value = "${org.java.frontend.confirm.reset.token}")
    private String passwordResetUrl;

    @Value(value = "${org.java.trial.attempts}")
    private Integer trialsAttempt;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private JwtUserDetailsService userDetailsService;

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

    @Autowired
    public AuthenticationController(AgentService agentService,
                                    GlobalMethods globalMethods,
                                    MerchantService merchantService,
                                    BusinessService businessService,
                                    AgentRepository agentRepository,
                                    MessengerService messengerService,
                                    CustomerRepository customerRepository,
                                    DistributorService distributorService,
                                    BusinessRepository businessRepository,
                                    MerchantRepository merchantRepository,
                                    MessengerRepository messengerRepository,
                                    DistributorRepository distributorRepository,
                                    RegistrationStoreService registrationStoreService,
                                    LoginValidationRepository loginValidationRepository) {
        this.globalMethods = globalMethods;
        this.agentService = agentService;
        this.merchantService = merchantService;
        this.businessService = businessService;
        this.agentRepository = agentRepository;
        this.messengerService = messengerService;
        this.customerRepository = customerRepository;
        this.distributorService = distributorService;
        this.businessRepository = businessRepository;
        this.merchantRepository = merchantRepository;
        this.messengerRepository = messengerRepository;
        this.distributorRepository = distributorRepository;
        this.registrationStoreService = registrationStoreService;

        this.loginValidationRepository = loginValidationRepository;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
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

            loginValidationRepository.findLoginValidationByEmailAddress(authenticationRequest.getEmail())
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
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
            String token = jwtTokenUtil.generateToken(userDetails);
            loginValidationRepository.findLoginValidationByEmailAddress(authenticationRequest.getEmail())
                    .ifPresent(userLogin -> {
                        userLogin.setPinAttempt(0);
                        userLogin.setLastAttemptDate(Timestamp.from(Instant.now()));
                        loginValidationRepository.save(userLogin);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("userToken", token)
                                .put("refreshToken", token)
                                .put("changePin", userLogin.getPinChange())
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

    @RequestMapping(value = {"/change-password"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody String changePas) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqJson = new JSONObject(changePas);
            LoginValidation user = globalMethods.fetchUserDetails();
            loginValidationRepository.findLoginValidationByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(loginValidation -> {
                        loginValidation.setPinHash(bcryptEncoder.encode(reqJson.getString("newPassword")));
                        loginValidation.setPinChange(1);
                        loginValidationRepository.save(loginValidation);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Change Password was successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.NOT_REGISTERED)
                                .put("statusDescription", "The User does not exists")
                                .put("statusMessage", "The User does not exists");
                    });
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

    @RequestMapping(value = {"/password-reset"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> passwordReset(@RequestBody String requestBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(requestBody);
            String userEmail = jsonObject.getString("email");
            loginValidationRepository.findLoginValidationByEmailAddress(userEmail)
                    .ifPresentOrElse(user -> {

                        String code = globalMethods.getMobileValidationCode();
                        String token = code + "-" + UUID.randomUUID().toString();
                        user.setPasswordResetToken(token);
                        user.setPasswordResetTokenStatus(0);
                        user.setPasswordResetTokenExpire(Timestamp.from(Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(30))));
                        loginValidationRepository.save(user);

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
                        smsBody.put("Phone", globalMethods.getMultiUserCustomer(userEmail).getString("phoneNumber"));
                        smsBody.put("otp", code);
                        globalMethods.sendSMSNotification(smsBody);

                        responseMap.put("Status", "00");
                        responseMap.put("Message", "Request to reset password received.");
                    }, () -> {
                        responseMap.put("Status", "99");
                        responseMap.put("Message", "User does not exist");
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
            loginValidationRepository.findLoginValidationByEmailAddressAndPasswordResetTokenStatus(jsonObject.getString("userEmail"), 0)
                    .ifPresentOrElse(user -> {
                        if (user.getPasswordResetTokenExpire().equals(Timestamp.from(Instant.now()))) {
                        }
                        if (!user.getPasswordResetToken().substring(0, 4).equals(jsonObject.getString("code"))) {
                            responseMap.put("Status", "99");
                            responseMap.put("Message", "Code is not valid or expired");
                        } else {
                            user.setStatus(1);
                            loginValidationRepository.save(user);
                            responseMap.put("Status", "00");
                            responseMap.put("Message", "Code Valid");
                            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmailAddress());
                            String userToken = jwtTokenUtil.generateToken(userDetails);
                            responseMap.put("jwttoken", userToken);
                            user.setPasswordResetTokenStatus(1);
                            loginValidationRepository.save(user);
                        }
                    }, () -> {
                        responseMap.put("Status", "99");
                        responseMap.put("Message", "Token is not valid or expired");
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
            responseMap.put("Status", "00");
            responseMap.put("Message", "Password changed successfully");
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
            responseMap.put("Status", "00");
            responseMap.put("Message", "Password changed successfully");
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

        try {
            JSONObject request = new JSONObject(registration);
            String password = globalMethods.getGeneratePassword();
            String passwordHash = bcryptEncoder.encode(password);
            request.put("password", passwordHash);

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
