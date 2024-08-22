//package com.commerce.pal.backend.serverSideRendering;
//
//import com.commerce.pal.backend.common.ResponseCodes;
//import com.commerce.pal.backend.models.user.referral_codes.ReferralType;
//import com.commerce.pal.backend.module.AuthenticationService;
//import com.commerce.pal.backend.module.DistributorService;
//import com.commerce.pal.backend.module.database.RegistrationStoreService;
//import com.commerce.pal.backend.module.users.AgentService;
//import com.commerce.pal.backend.module.users.MerchantService;
//import com.commerce.pal.backend.module.users.MessengerService;
//import com.commerce.pal.backend.module.users.business.BusinessService;
//import com.commerce.pal.backend.module.users.referral_codes.ReferralCodeUtils;
//import com.commerce.pal.backend.repo.LoginValidationRepository;
//import com.commerce.pal.backend.repo.user.*;
//import com.commerce.pal.backend.repo.user.business.BusinessRepository;
//import com.commerce.pal.backend.utils.GlobalMethods;
//import com.commerce.pal.backend.utils.HttpProcessor;
//import lombok.RequiredArgsConstructor;
//import org.asynchttpclient.RequestBuilder;
//import org.json.JSONObject;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.bind.support.SessionStatus;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//import java.util.logging.Level;
//
//@Controller
//@CrossOrigin(origins = "*")
//@RequiredArgsConstructor
//public class HomeController {
//    private final HttpProcessor httpProcessor;
//    private final GlobalMethods globalMethods;
//    private final MerchantService merchantService;
//    private final BusinessService businessService;
//    private final AgentRepository agentRepository;
//    private final MessengerService messengerService;
//    private final CustomerRepository customerRepository;
//    private final DistributorService distributorService;
//    private final BusinessRepository businessRepository;
//    private final MerchantRepository merchantRepository;
//    private final MessengerRepository messengerRepository;
//    private final DistributorRepository distributorRepository;
//    private final RegistrationStoreService registrationStoreService;
//    private final LoginValidationRepository loginValidationRepository;
//    private final AuthenticationService authenticationService;
//    private final ReferralCodeUtils referralCodeUtils;
//    private final PasswordEncoder bcryptEncoder;
//
//    @GetMapping(value = "/browse")
//    public String index(HttpServletRequest request, HttpSession session, SessionStatus status) {
//        try {
//            //For Hijra Bank
//            String source = request.getHeader("source");
//            if (source != null && source.equalsIgnoreCase("Hijra"))
//                return handleHijraReq(request);
//
////            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
////                RequestBuilder builder = new RequestBuilder("GET");
////                builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
////                        .setHeader(HttpHeaders.ACCEPT, "application/json")
////                        .setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader)
////                        .setUrl("AUTH_URL")
////                        .build();
////
////                JSONObject resp = httpProcessor.jsonRequestProcessor(builder);
////
////                if (resp.getString("StatusCode").equals("200")) {
////                    session.setAttribute(session.getId(), authorizationHeader);
////                    return "index.html";
////
////                } else return "invalid-token.html";
////
////            } else return "missing-valid-token.html";
//
//            throw new RuntimeException();
//        } catch (Exception ex) {
//
//            ex.printStackTrace();
//            status.setComplete(); // Clears the session attribute if an exception occurs
//            return "server-error.html";
//        }
//    }
//
//    public String handleHijraReq(HttpServletRequest request) {
//        try {
//            String firstName = request.getHeader("firstName");
//            String lastName = request.getHeader("lastName");
//            String phoneNumber = request.getHeader("phoneNumber");
//
//            if (firstName == null || lastName == null || phoneNumber == null)
//                throw new IllegalArgumentException("The 'firstName', 'lastName', and 'phoneNumber' headers are required.");
//
//            return "hijra/index.html";
//        } catch (IllegalArgumentException ex) {
//            return "hijra/missing-required-headers.html";
//        }
//    }
//
//
//    public void customerRegistration(String firstName, String lastName, String phoneNumber) {
//        try {
//
//            String customerReferralCode = referralCodeUtils.generateCustomerReferralCode();
//
//            JSONObject request = new JSONObject();
//            request.put("firstName", firstName);
//            request.put("lastName", lastName);
//            request.put("email", "");
//            request.put("city", 1);
//            request.put("country", "ET");
//            request.put("language", "en");
//            request.put("msisdn", phoneNumber);
//            request.put("registeredBy", "HalalPay");
//            request.put("channel", "ANDROID");
//            request.put("deviceId", "12345678");
//            request.put("referralCode", customerReferralCode);
//
//            String password = globalMethods.getGeneratePassword();
//            String passwordHash = bcryptEncoder.encode(password);
//            request.put("password", passwordHash);
//
//            JSONObject queryResponse = registrationStoreService.doCustomerRegistration(request);
//
//            int exists = queryResponse.getInt("exists");
//
//            if (exists == 1) {
//                responseMap.put("statusCode", ResponseCodes.REGISTERED)
//                        .put("statusDescription", "email address already registered")
//                        .put("statusMessage", "registration exists");
//            } else if (exists == -1) {
//                JSONObject smsBody = new JSONObject();
//                smsBody.put("TemplateId", "1");
//                smsBody.put("TemplateLanguage", "en");
//                smsBody.put("Phone", request.getString("msisdn").substring(request.getString("msisdn").length() - 9));
//                globalMethods.sendSMSNotification(smsBody);
//
//
//            } else {
//                request.put("PhoneOtp", "1");
//                request.put("EmailOtp", "1");
//
//                registrationStoreService.pinOtp(request);
//
//
//                JSONObject emailPayload = new JSONObject();
//                emailPayload.put("HasTemplate", "YES");
//                emailPayload.put("TemplateName", "registration");
//                emailPayload.put("name", request.getString("firstName"));
//                emailPayload.put("password", password);
//                emailPayload.put("EmailDestination", request.getString("email").trim());
//                emailPayload.put("EmailSubject", "COMMERCE PAL REGISTRATION");
//                emailPayload.put("EmailMessage", "Password Reset");
//                globalMethods.sendEmailNotification(emailPayload);
//
//
//                JSONObject smsBody = new JSONObject();
//                smsBody.put("TemplateId", "1");
//                smsBody.put("TemplateLanguage", "en");
//                smsBody.put("otp", password);
//                smsBody.put("Phone", request.getString("msisdn").substring(request.getString("msisdn").length() - 9));
//                smsBody.put("hash", globalMethods.getHashkey());
//                globalMethods.sendSMSNotification(smsBody);
//            }
//
//        } catch (Exception ex) {
//            throw ex;
//        }
//    }
//
//
//}
//
