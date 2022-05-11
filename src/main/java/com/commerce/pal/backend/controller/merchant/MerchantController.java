package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.multi.MerchantService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
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

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/merchant"})
@SuppressWarnings("Duplicates")
public class MerchantController {

    @Autowired
    private EmailClient emailClient;
    @Autowired
    private UploadService uploadService;

    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final MerchantService merchantService;
    private final SpecificationsDao specificationsDao;
    private final MerchantRepository merchantRepository;

    @Autowired
    public MerchantController(GlobalMethods globalMethods,
                              ProductService productService,
                              MerchantService merchantService,
                              SpecificationsDao specificationsDao,
                              MerchantRepository merchantRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.merchantService = merchantService;
        this.specificationsDao = specificationsDao;
        this.merchantRepository = merchantRepository;
    }

    @RequestMapping(value = "/accept-terms", method = RequestMethod.POST)
    public ResponseEntity<?> acceptTerms(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        if (merchant.getTermsOfServiceStatus().equals(1)) {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Merchant has already accepted terms of service")
                                    .put("statusMessage", "Merchant has already accepted terms of service");
                        } else {
                            merchant.setTermsOfServiceStatus(1);
                            merchant.setTermsOfServiceDate(Timestamp.from(Instant.now()));
                            merchant.setStatus(1);
                            merchantRepository.save(merchant);
                            JSONObject emailBody = new JSONObject();
                            emailBody.put("email", merchant.getEmailAddress());
                            emailBody.put("subject", "Terms of Service Agreement");
                            emailBody.put("template", "merchant-service-agreement.ftl");
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
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        request.put("ownerType", merchant.getOwnerType());
                        request.put("ownerId", merchant.getOwnerId().toString());
                        responseMap.set(merchantService.updateMerchant(String.valueOf(merchant.getMerchantId()), request));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Merchant Does not exists")
                                .put("statusMessage", "Merchant Does not exists");
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
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        String imageFileUrl = uploadService.uploadFileAlone(multipartFile, "Web", "MERCHANT");
                        responseMap.set(merchantService.uploadDocs(String.valueOf(merchant.getMerchantId()), fileType, imageFileUrl));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Merchant Does not exists")
                                .put("statusMessage", "Merchant Does not exists");
                    });
        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }



}
