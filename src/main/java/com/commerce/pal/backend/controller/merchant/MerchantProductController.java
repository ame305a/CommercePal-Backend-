package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/merchant/product"})
@SuppressWarnings("Duplicates")
public class MerchantProductController {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final SpecificationsDao specificationsDao;
    private final MerchantRepository merchantRepository;

    @Autowired
    public MerchantProductController(GlobalMethods globalMethods,
                                     ProductService productService,
                                     SpecificationsDao specificationsDao,
                                     MerchantRepository merchantRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.specificationsDao = specificationsDao;
        this.merchantRepository = merchantRepository;
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
                        int returnValue = retDet.getInt("productId");
                        if (returnValue == 0) {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "failed to process request")
                                    .put("statusMessage", "internal system error");
                        } else {
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("productId", retDet.getInt("productId"))
                                    .put("subProductId", retDet.getInt("subProductId"))
                                    .put("statusMessage", "Product successful");
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

    @RequestMapping(value = "/disable-product", method = RequestMethod.POST)
    public ResponseEntity<?> disableProduct(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getMerchantId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateMerchantProduct(
                        globalMethods.getMerchantId(user.getEmailAddress()),
                        request.getString("productId"))) {
                    responseMap = productService.disableProduct(request);
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
                                         @RequestParam("brand") Optional<String> brand,
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
                    brand.ifPresent(value -> {
                        params.add(new SearchCriteria("manufucturer", ":", value));
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

    @RequestMapping(value = "/enable-disable-account", method = RequestMethod.POST)
    public ResponseEntity<?> enableDisableAccount(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        JSONObject uploadJson = new JSONObject();
                        uploadJson.put("MerchantId", merchant.getMerchantId());
                        uploadJson.put("Type", request.getString("type"));
                        uploadJson.put("Platform", request.getString("StatusComment"));
                        JSONObject updateRes = new JSONObject();
                        updateRes = productService.enableDisableAccount(request);
                        if (updateRes.getString("Status").equals("00")) {
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("statusMessage", "Request Successful");
                        } else {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "failed to process request")
                                    .put("statusMessage", "internal system error");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "The User is not a Merchant")
                                .put("statusMessage", "The User is not a Merchant");
                    });

        } catch (Exception e) {

            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
