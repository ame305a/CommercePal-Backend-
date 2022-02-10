package com.commerce.pal.backend.controller.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.database.ProductDatabaseService;
import com.commerce.pal.backend.integ.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.ProductService;
import com.commerce.pal.backend.module.multi.MerchantService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @RequestMapping(value = "/add-product", method = RequestMethod.POST)
    public ResponseEntity<?> addProduct(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        request.put("merchantId", String.valueOf(merchant.getMerchantId()));
                        request.put("productImage", "defaultImage.png");
                        request.put("isPromoted", "0");
                        request.put("isPrioritized", "0");
                        request.put("ownerType", "MERCHANT");
                        JSONObject retDet = productService.doAddProduct(request);
                        int returnValue = retDet.getInt("returnValue");
                        if (returnValue == 1) {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "failed to process request")
                                    .put("statusMessage", "internal system error");
                        } else {
                            int exists = retDet.getInt("exists");
                            if (exists == 1) {
                                responseMap.put("statusCode", ResponseCodes.REGISTERED)
                                        .put("statusDescription", "Product already added")
                                        .put("statusMessage", "Product already added");
                            } else {
                                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                        .put("statusDescription", "success")
                                        .put("productId", retDet.getInt("exists"))
                                        .put("statusMessage", "Product successful");
                            }
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

    @RequestMapping(value = "/update-product", method = RequestMethod.POST)
    public ResponseEntity<?> updateProduct(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getMerchantId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateMerchantProduct(
                        globalMethods.getMerchantId(user.getEmailAddress()),
                        request.getString("productId"))) {
                    responseMap = productService.updateProduct(request);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to the Merchant")
                            .put("statusMessage", "he User does not belong to the Merchant");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a Merchant")
                        .put("statusMessage", "The User is not a Merchant");
            }

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/get-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetProducts(@RequestParam("parent") Optional<String> parent,
                                         @RequestParam("category ") Optional<String> category,
                                         @RequestParam("subCat") Optional<String> subCat,
                                         @RequestParam("product") Optional<String> product) {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    List<SearchCriteria> params = new ArrayList<SearchCriteria>();
                    params.add(new SearchCriteria("merchantId", ":", merchant.getMerchantId()));
                    parent.ifPresent(value -> {
                        params.add(new SearchCriteria("productParentCateoryId", ":", value));
                    });
                    category.ifPresent(value -> {
                        params.add(new SearchCriteria("productCategoryId", ":", value));
                    });
                    subCat.ifPresent(value -> {
                        params.add(new SearchCriteria("productSubCategoryId", ":", value));
                    });
                    product.ifPresent(value -> {
                        params.add(new SearchCriteria("productId", ":", value));
                    });
                    List<JSONObject> details = new ArrayList<>();
                    specificationsDao.getProducts(params)
                            .forEach(pro -> {
                                JSONObject detail = productService.getProductDetail(pro.getProductId());
                                details.add(detail);
                            });
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("details", details)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

}
