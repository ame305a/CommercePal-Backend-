package com.commerce.pal.backend.controller;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.BadRequestException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ForbiddenException;
import com.commerce.pal.backend.common.security.JwtTokenUtil;
import com.commerce.pal.backend.common.security.JwtUserDetailsService;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.database.RegistrationStoreService;
import com.commerce.pal.backend.module.users.referral_codes.ReferralCodeUtils;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level;
//TODO: also do it here halal pay customer registrations

@Log
@CrossOrigin(origins = "https://localhost:2095", maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/halal-pay"})
@RequiredArgsConstructor
public class HalalPayController {

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;
    private final LoginValidationRepository loginValidationRepository;
    private final GlobalMethods globalMethods;
    private final ReferralCodeUtils referralCodeUtils;
    private final RegistrationStoreService registrationStoreService;
    private final PasswordEncoder bcryptEncoder;

    @Value(value = "${commerce.pal.halal.pay.pass-flag}")
    private String passFlag;

    @PostMapping(value = "/get-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getToken(@RequestHeader(value = "X-Halal-pay-Flag") String halalPayFlag,
                                           @RequestParam(value = "phoneNumber") String phoneNumber) {

        //Don't change it. If changed, also change the corresponding part in the payment app.
        if (!passFlag.equals(halalPayFlag))
            throw new ForbiddenException("Access denied");

        // Extract the last 9 digits from the phone number and add the "251" prefix
        if (phoneNumber.length() < 9)
            throw new BadRequestException("Invalid phone number");

        String transformedPhoneNumber = "251" + phoneNumber.substring(phoneNumber.length() - 9);

        JSONObject responseMap = new JSONObject();
        loginValidationRepository.findLoginValidationByPhoneNumber(transformedPhoneNumber)
                .ifPresentOrElse(userLogin -> {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getEmailAddress());
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
                }, () -> {
                    throw new BadRequestException("User not found");
                });

        return ResponseEntity.ok(responseMap.toString());
    }


    @PostMapping(value = "/register-customer", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> customerRegistration(@RequestHeader(value = "X-Halal-pay-Flag") String halalPayFlag,
                                                       @RequestBody String registration) {

        //Don't change it. If changed, also change the corresponding part in the payment app.
        if (!passFlag.equals(halalPayFlag))
            throw new ForbiddenException("Access denied");

        JSONObject responseMap = new JSONObject();
        log.log(Level.INFO, registration);
        try {
            JSONObject request = new JSONObject(registration);
            String phoneNumber = request.getString("msisdn");
            String transformedPhoneNumber = "251" + phoneNumber.substring(phoneNumber.length() - 9);

            Optional<LoginValidation> loginValidation = loginValidationRepository.findLoginValidationByPhoneNumber(transformedPhoneNumber);
            if (loginValidation.isEmpty()) {
                populateCustomerJSON(request, transformedPhoneNumber);

                String password = globalMethods.getGeneratePassword();
                String passwordHash = bcryptEncoder.encode(password);
                request.put("password", passwordHash);

                String customerReferralCode = referralCodeUtils.generateCustomerReferralCode();
                request.put("referralCode", customerReferralCode);
                request.put("referringUserId", 0L);
                request.put("referringUserType", "");
                request.put("pointsEarned", 0);

                responseMap = registrationStoreService.doCustomerRegistration(request);
                int exists = responseMap.getInt("exists");
                if (exists == 0) {
                    JSONObject pinOtp = new JSONObject();
                    pinOtp.put("PhoneOtp", "1");
                    pinOtp.put("EmailOtp", "1");
                    registrationStoreService.pinOtp(pinOtp);
                }
            }

            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "Request completed successfully")
                    .put("statusMessage", "The request has been processed successfully.");

        } catch (Exception e) {
            log.log(Level.WARNING, "HalalPay User Registration Error : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Failed to process request")
                    .put("errorDescription", e.getMessage())
                    .put("statusMessage", "Internal system error");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    private void populateCustomerJSON(JSONObject customerReq, String transformedPhoneNumber) {
        customerReq.put("msisdn", transformedPhoneNumber);
        customerReq.put("email", transformedPhoneNumber);
        customerReq.put("password", "");
        customerReq.put("city", "1");
        customerReq.put("country", "ET");
        customerReq.put("language", "en");
        customerReq.put("channel", "ANDROID");
        customerReq.put("deviceId", "12345678");
        customerReq.put("registeredBy", "Halal pay");

    }

}
