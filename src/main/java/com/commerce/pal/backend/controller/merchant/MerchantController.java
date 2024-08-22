package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.Merchant;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @RequestMapping(value = "/update-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> updateDeliveryAddress(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();

            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        merchant.setRegionId(reqBody.has("regionId") ? reqBody.getInt("regionId") : merchant.getRegionId());
                        merchant.setCountry(reqBody.has("country") ? reqBody.getString("country") : merchant.getCountry());
                        merchant.setCity(reqBody.has("city") ? reqBody.getInt("city") : merchant.getCity());
                        merchant.setServiceCodeId(reqBody.has("serviceCodeId") ? reqBody.getInt("serviceCodeId") : merchant.getServiceCodeId());
                        merchant.setPhysicalAddress(reqBody.has("physicalAddress") ? reqBody.getString("physicalAddress") : merchant.getPhysicalAddress());
                        merchant.setLatitude(reqBody.has("latitude") ? reqBody.getString("latitude") : merchant.getLatitude());
                        merchant.setLongitude(reqBody.has("longitude") ? reqBody.getString("longitude") : merchant.getLongitude());
                        merchantRepository.save(merchant);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "MERCHANT DELIVERY ADDRESS INFO : " + e.getMessage());
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
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        JSONObject payload = new JSONObject();
                        payload.put("country", merchant.getCountry());
                        payload.put("city", merchant.getCity());
                        payload.put("regionId", merchant.getRegionId());
                        payload.put("serviceCodeId", merchant.getServiceCodeId());
                        payload.put("physicalAddress", merchant.getPhysicalAddress());
                        payload.put("latitude", merchant.getLatitude());
                        payload.put("longitude", merchant.getLongitude());
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
            log.log(Level.WARNING, "MERCHANT DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @GetMapping(value = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer city,
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by merchant name in ascending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "merchantName";

            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = merchantService.getAllMerchants(page, size, sort, status, city, regionId, searchKeyword, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "MERCHANT REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllMerchants(@RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, "merchantName");

            JSONObject response = merchantService.getAllMerchants(sort);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "GET ALL MERCHANTS: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.ok(responseMap.toString());
        }
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchMerchants(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        JSONObject response = merchantService.searchMerchants(name, page, size);

        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/{merchantId}/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMerchantAboutWithProducts(@PathVariable Long merchantId) {
        JSONObject response = merchantService.getMerchantAboutWithProducts(merchantId);
        return ResponseEntity.ok(response.toString());
    }
}
