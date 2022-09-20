package com.commerce.pal.backend.controller.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.users.business.BusinessService;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/business"})
@SuppressWarnings("Duplicates")
public class BusinessController {

    @Autowired
    private EmailClient emailClient;
    @Autowired
    private UploadService uploadService;

    private final GlobalMethods globalMethods;
    private final BusinessService businessService;
    private final BusinessRepository businessRepository;

    @Autowired
    public BusinessController(GlobalMethods globalMethods,
                              BusinessService businessService,
                              BusinessRepository businessRepository) {
        this.globalMethods = globalMethods;
        this.businessService = businessService;
        this.businessRepository = businessRepository;
    }

    @RequestMapping(value = "/accept-terms", method = RequestMethod.POST)
    public ResponseEntity<?> acceptTerms(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        if (business.getTermsOfServiceStatus().equals(1)) {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Merchant has already accepted terms of service")
                                    .put("statusMessage", "Merchant has already accepted terms of service");
                        } else {
                            business.setTermsOfServiceStatus(1);
                            business.setTermsOfServiceDate(Timestamp.from(Instant.now()));
                            business.setStatus(1);
                            businessRepository.save(business);
                            JSONObject emailBody = new JSONObject();
                            emailBody.put("email", business.getEmailAddress());
                            emailBody.put("subject", "Terms of Service Agreement");
                            emailBody.put("template", "business-service-agreement.ftl");
                            emailClient.emailTemplateSender(emailBody);
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Successful")
                                    .put("statusMessage", "Successful");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Merchant Does not exists")
                                .put("statusMessage", "Merchant Does not exists");
                    });

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-detail", method = RequestMethod.POST)
    public ResponseEntity<?> updateDetails(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        request.put("ownerType", business.getOwnerType());
                        request.put("ownerId", business.getOwnerId().toString());
                        responseMap.set(businessService.updateBusiness(String.valueOf(business.getBusinessId()), request));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Business Does not exists")
                                .put("statusMessage", "Business Does not exists");
                    });

        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/upload-docs", method = RequestMethod.POST)
    public ResponseEntity<?> uploadUserDocs(@RequestPart(value = "file") MultipartFile multipartFile,
                                            @RequestPart(value = "fileType") String fileType) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        String imageFileUrl = uploadService.uploadFileAlone(multipartFile, "Web", "BUSINESS");
                        responseMap.set(businessService.uploadDocs(String.valueOf(business.getBusinessId()), fileType, imageFileUrl));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Business Does not exists")
                                .put("statusMessage", "Business Does not exists");
                    });
        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> updateDeliveryAddress(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        business.setRegionId(reqBody.has("regionId") ? reqBody.getInt("regionId") : business.getRegionId());
                        business.setCountry(reqBody.has("country") ? reqBody.getString("country") : business.getCountry());
                        business.setCity(reqBody.has("city") ? reqBody.getInt("city") : business.getCity());
                        business.setServiceCodeId(reqBody.has("serviceCodeId") ? reqBody.getInt("serviceCodeId") : business.getServiceCodeId());
                        business.setPhysicalAddress(reqBody.has("physicalAddress") ? reqBody.getString("physicalAddress") : business.getPhysicalAddress());
                        business.setLatitude(reqBody.has("latitude") ? reqBody.getString("latitude") : business.getLatitude());
                        business.setLongitude(reqBody.has("longitude") ? reqBody.getString("longitude") : business.getLongitude());
                        businessRepository.save(business);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "BUSINESS DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-delivery-address", method = RequestMethod.GET)
    public ResponseEntity<?> getDeliveryAddress() {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        JSONObject payload = new JSONObject();
                        payload.put("country", business.getCountry());
                        payload.put("city", business.getCity());
                        payload.put("regionId", business.getRegionId());
                        payload.put("serviceCodeId", business.getServiceCodeId());
                        payload.put("physicalAddress", business.getPhysicalAddress());
                        payload.put("latitude", business.getLatitude());
                        payload.put("longitude", business.getLongitude());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("data", payload)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "BUSINESS DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
