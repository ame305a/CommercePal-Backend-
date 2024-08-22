package com.commerce.pal.backend.module.socialLogin;

import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.dto.auth.OAuth2FollowUpReqDto;
import com.commerce.pal.backend.dto.auth.OAuth2ReqDto;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.module.database.RegistrationStoreService;
import com.commerce.pal.backend.module.users.referral_codes.ReferralCodeUtils;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.security.JwtTokenUtil;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import org.springframework.transaction.annotation.Transactional;

//TODO: WHEN DOING FOLLOW UP PHONE TAKE, VALIDATE IT LIKE NORMAL REGISTRATIONS
@Log
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final LoginValidationRepository loginValidationRepository;
    private final CustomerRepository customerRepository;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RegistrationStoreService registrationStoreService;
    private final GlobalMethods globalMethods;
    private final ReferralCodeUtils referralCodeUtils;
    private final PasswordEncoder bcryptEncoder;

    @Transactional
    public JSONObject processAuthLogin(OAuth2ReqDto oAuth2ReqDto) {
        LoginValidation loginValidation;

        if (oAuth2ReqDto.getEmail() != null) {
            Optional<LoginValidation> optionalLoginValidation = loginValidationRepository
                    .findLoginValidationByEmailAddress(oAuth2ReqDto.getEmail());

            if (optionalLoginValidation.isPresent()) {
                loginValidation = optionalLoginValidation.get();
                updateLoginValidation(loginValidation, oAuth2ReqDto);
                updateCustomer(oAuth2ReqDto);
            } else {
                saveNewCustomer(oAuth2ReqDto);
                loginValidation = loginValidationRepository.findByProviderUserId(oAuth2ReqDto.getProviderUserId());
            }
        } else {
            loginValidation = loginValidationRepository.findByProviderUserId(oAuth2ReqDto.getProviderUserId());
            if (loginValidation == null) {
                saveNewCustomer(oAuth2ReqDto);
                loginValidation = loginValidationRepository.findByProviderUserId(oAuth2ReqDto.getProviderUserId());
            }
        }

        return getToken(loginValidation);
    }

    private void updateLoginValidation(LoginValidation loginValidation, OAuth2ReqDto oAuth2ReqDto) {
        if (oAuth2ReqDto.getDeviceId() != null)
            loginValidation.setDeviceId(oAuth2ReqDto.getDeviceId());

        loginValidation.setOAuthProvider(oAuth2ReqDto.getProvider());
        loginValidation.setProviderUserId(oAuth2ReqDto.getProviderUserId());
        loginValidationRepository.save(loginValidation);
    }

    private void updateCustomer(OAuth2ReqDto oAuth2ReqDto) {
        Optional<Customer> optionalCustomer = customerRepository.findCustomerByEmailAddress(oAuth2ReqDto.getEmail());
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setOAuthUserId(oAuth2ReqDto.getProviderUserId());
            customerRepository.save(customer);
        }
    }

    public void saveNewCustomer(OAuth2ReqDto oAuth2ReqDto) {
        String password = globalMethods.getGeneratePassword();
        String passwordHash = bcryptEncoder.encode(password);
        String customerReferralCode = referralCodeUtils.generateCustomerReferralCode();

        JSONObject request = getJsonObject(oAuth2ReqDto, passwordHash, customerReferralCode);

        log.log(Level.INFO, request.toString());
        registrationStoreService.doSocialLoginCustomerRegistration(request);
    }

    private static JSONObject getJsonObject(OAuth2ReqDto oAuth2ReqDto, String passwordHash, String customerReferralCode) {
        JSONObject request = new JSONObject();
        request.put("firstName", oAuth2ReqDto.getFirstName());
        request.put("lastName", oAuth2ReqDto.getLastName());
        request.put("email", oAuth2ReqDto.getEmail());
        request.put("city", "1");
        request.put("country", "ET");
        request.put("language", "en");
        request.put("channel", oAuth2ReqDto.getChannel().toString());
        request.put("deviceId", oAuth2ReqDto.getDeviceId() != null ? oAuth2ReqDto.getDeviceId() : "12345678");
        request.put("registeredBy", "System");
        request.put("oAuthProvider", oAuth2ReqDto.getProvider().toString());
        request.put("providerUserId", oAuth2ReqDto.getProviderUserId());
        request.put("password", passwordHash);
        request.put("referralCode", customerReferralCode);

        return request;
    }

    @Transactional
    public JSONObject processAuthFollowUpReq(OAuth2FollowUpReqDto followUpReqDto) {
        // Fetch user details and validate existence by OAuth provider user ID
        LoginValidation loginValidation = globalMethods.fetchUserDetails();
        Customer customer = customerRepository.findByOAuthUserId(loginValidation.getProviderUserId());

        String phoneNumber = followUpReqDto.getPhoneNumber();
        String transformedPhoneNumber = "251" + phoneNumber.substring(phoneNumber.length() - 9);

        // Check if phone number or email already exists
        if (loginValidationRepository.existsByPhoneNumber(transformedPhoneNumber))
            throw new ResourceAlreadyExistsException("Phone number already exists.");

        if (followUpReqDto.getEmail() != null && loginValidationRepository.existsByEmailAddress(followUpReqDto.getEmail()))
            throw new ResourceAlreadyExistsException("Email address already exists.");


        // Update customer and login validation with new email or phone number
        customer.setPhoneNumber(transformedPhoneNumber);
        loginValidation.setPhoneNumber(transformedPhoneNumber);

        if (followUpReqDto.getEmail() != null) {
            customer.setEmailAddress(followUpReqDto.getEmail());
            loginValidation.setEmailAddress(followUpReqDto.getEmail());
        }

        // Save changes in repositories
        loginValidation = loginValidationRepository.save(loginValidation);
        customer = customerRepository.save(customer);


//        JSONObject request = new JSONObject();
//        request.put("providerUserId", loginValidation.getProviderUserId());
//        request.put("phoneNumber", followUpReqDto.getPhoneNumber());
//        request.put("emailAddress", followUpReqDto.getEmail());
//        request.put("channel", followUpReqDto.getChannel().toString());
//        request.put("deviceId", loginValidation.getDeviceId());
//
//        registrationStoreService.doUpdateCustomerAndLoginValidation(request);

        //issue new token
        return getToken(loginValidation);
    }

    //TODO: If facebook account is created with phone number only.
    public JSONObject getToken(LoginValidation userLogin) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getEmailAddress());
        String token = jwtTokenUtil.generateToken(userDetails);
        userLogin.setPinAttempt(0);
        userLogin.setLastAttemptDate(Timestamp.from(Instant.now()));
        loginValidationRepository.save(userLogin);

        int isPhoneProvided = (userLogin.getPhoneNumber() == null || userLogin.getPhoneNumber().isEmpty()) ? 0 : 1;
        int isEmailProvided = (userLogin.getEmailAddress() == null || userLogin.getEmailAddress().isEmpty()) ? 0 : 1;

        return new JSONObject().put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("userToken", token)
                .put("refreshToken", token)
                .put("changePin", userLogin.getPinChange())
                .put("isEmailProvided", isEmailProvided)
                .put("isPhoneProvided", isPhoneProvided)
                .put("isEmailValidated", userLogin.getIsEmailValidated())
                .put("isPhoneValidated", userLogin.getIsPhoneValidated())
                .put("statusMessage", "login successful");
    }

}


