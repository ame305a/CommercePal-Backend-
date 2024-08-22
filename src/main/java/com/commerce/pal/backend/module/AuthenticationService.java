package com.commerce.pal.backend.module;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class AuthenticationService {

    private final GlobalMethods globalMethods;
    private final LoginValidationRepository loginValidationRepository;

    public JSONObject passwordResetReq(String requestBody) {
        JSONObject responseMap = new JSONObject();
        JSONObject jsonObject = new JSONObject(requestBody);
        String userEmail = jsonObject.getString("email");

        LoginValidation user = getLoginValidationByUserEmail(userEmail);

        String code = globalMethods.getMobileValidationCode();
        String token = code + "-" + UUID.randomUUID();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenStatus(0);
        user.setPasswordResetTokenExpire(Timestamp.from(Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(30))));
        loginValidationRepository.save(user);

        sendPasswordResetEmail(userEmail, code);
        sendPasswordResetSMS(userEmail, code);

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Password reset initiated successfully.");

        return responseMap;
    }


    private void sendPasswordResetEmail(String userEmail, String code) {
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
    }

    private void sendPasswordResetSMS(String userEmail, String code) {
        JSONObject smsBody = new JSONObject();
        smsBody.put("TemplateId", "9");
        smsBody.put("TemplateLanguage", "en");
        smsBody.put("Phone", globalMethods.getMultiUserCustomer(userEmail).getString("phoneNumber"));
        smsBody.put("Phone", "917275901");
        smsBody.put("otp", code);
        smsBody.put("hash", globalMethods.getHashkey());
        globalMethods.sendSMSNotification(smsBody);
    }

    private LoginValidation getLoginValidationByUserEmail(String userEmail) {
        return loginValidationRepository.findLoginValidationByEmailAddress(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No user account exists with the provided email address."));
    }


}
