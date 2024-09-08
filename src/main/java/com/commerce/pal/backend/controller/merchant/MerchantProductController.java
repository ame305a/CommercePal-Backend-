package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.repo.product.ProductFeatureRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

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
    private final SubProductService subProductService;
    private final ProductRepository productRepository;
    private final ProductFeatureRepository productFeatureRepository;

    @Autowired
    public MerchantProductController(GlobalMethods globalMethods,
                                     ProductService productService,
                                     SpecificationsDao specificationsDao,
                                     MerchantRepository merchantRepository,
                                     SubProductService subProductService, ProductRepository productRepository, ProductFeatureRepository productFeatureRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.specificationsDao = specificationsDao;
        this.merchantRepository = merchantRepository;
        this.subProductService = subProductService;
        this.productRepository = productRepository;
        this.productFeatureRepository = productFeatureRepository;
    }

    @RequestMapping(value = {"/get-features"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getProductFeatures() {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        productFeatureRepository.findAll().forEach(productFeature -> {
            JSONObject detail = new JSONObject();
            detail.put("subCategoryId", productFeature.getSubCategoryId());
            detail.put("featureName", productFeature.getFeatureName());
            detail.put("unitOfMeasure", productFeature.getUnitOfMeasure());
            detail.put("variableType", productFeature.getVariableType());
            details.add(detail);
        });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = "/add-product", method = RequestMethod.POST)
    public ResponseEntity<?> addProduct(@RequestBody String req) {
        log.log(Level.INFO, req);
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

                        if (subProductService.validateFeature(Long.valueOf(request.getString("productSubCategoryId")),
                                request.getJSONArray("productFeature")).equals(1)) {
                            JSONObject retDet = productService.doAddProduct(request);
                            int returnValue = retDet.getInt("productId");
                            if (returnValue == 0) {
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "Failed to process request")
                                        .put("statusMessage", "Internal system error");
                            } else {
                                JSONObject slackBody = new JSONObject();
                                slackBody.put("TemplateId", "7");
                                slackBody.put("product_name", request.getString("productName"));
                                slackBody.put("product_id", String.valueOf(retDet.getInt("productId")));
                                globalMethods.sendSlackNotification(slackBody);

                                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                        .put("statusDescription", "success")
                                        .put("productId", retDet.getInt("productId"))
                                        .put("subProductId", retDet.getInt("subProductId"))
                                        .put("statusMessage", "Product successful");

                                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                        .put("statusDescription", "success")
                                        .put("productId", retDet.getInt("productId"))
                                        .put("subProductId", retDet.getInt("subProductId"))
                                        .put("statusMessage", "Product successful");
                                subProductService.updateInsertFeatures(Long.valueOf(retDet.getInt("subProductId")), request.getJSONArray("productFeature"));
                            }
                        } else {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Product features not defined well")
                                    .put("statusMessage", "Product features not defined well");
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

    @RequestMapping(value = "/add-multi-sub-product", method = RequestMethod.POST)
    public ResponseEntity<?> addMultipleProduct(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONArray arrayReq = new JSONArray(req);
            arrayReq.forEach(jsonBody -> {
                JSONObject request = new JSONObject(jsonBody.toString());

                LoginValidation user = globalMethods.fetchUserDetails();
                merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                        .ifPresentOrElse(merchant -> {
                            productRepository.findProductByProductId(Long.valueOf(request.getString("ProductId")))
                                    .ifPresentOrElse(product -> {
                                        if (subProductService.validateFeature(product.getProductSubCategoryId(), request.getJSONArray("productFeature")).equals(1)) {
                                            responseMap.set(subProductService.addSubProduct(request));
                                            product.setStatus(0);
                                            product.setStatusComment("Added SubProduct - " + request.getString("shortDescription"));
                                            product.setStatusUpdatedDate(Timestamp.from(Instant.now()));
                                            productRepository.save(product);
                                        } else {
                                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                                    .put("statusDescription", "Product features not defined well")
                                                    .put("statusMessage", "Product features not defined well");
                                        }
                                    }, () -> {
                                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                                .put("statusDescription", "Product Does not exists")
                                                .put("statusMessage", "Product Does not exists");
                                    });
                        }, () -> {
                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Merchant Does not exists")
                                    .put("statusMessage", "Merchant Does not exists");
                        });
            });


        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-product", method = RequestMethod.POST)
    public ResponseEntity<?> updateProduct(@RequestBody String req) {
        log.log(Level.INFO, req);
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getMerchantId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateMerchantProduct(
                        globalMethods.getMerchantId(user.getEmailAddress()),
                        request.getLong("productId"))) {
                    responseMap = productService.updateProduct(request);

                    JSONObject slackBody = new JSONObject();
                    slackBody.put("TemplateId", "8");
                    slackBody.put("product_name", request.getString("productName"));
                    slackBody.put("product_id", String.valueOf(request.getLong("productId")));
                    globalMethods.sendSlackNotification(slackBody);

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

    @RequestMapping(value = "/add-sub-product", method = RequestMethod.POST)
    public ResponseEntity<?> addSubProduct(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            productRepository.findProductByProductId(Long.valueOf(request.getString("ProductId")))
                    .ifPresentOrElse(product -> {
                        if (subProductService.validateFeature(product.getProductSubCategoryId(), request.getJSONArray("productFeature")).equals(1)) {
                            responseMap.set(subProductService.addSubProduct(request));
                            product.setStatus(0);
                            product.setStatusComment("Added SubProduct - " + request.getString("shortDescription"));
                            product.setStatusUpdatedDate(Timestamp.from(Instant.now()));
                            productRepository.save(product);
                        } else {
                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Product features not defined well")
                                    .put("statusMessage", "Product features not defined well");
                        }
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Product Does not exists")
                                .put("statusMessage", "Product Does not exists");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.get().toString());
    }

    @RequestMapping(value = "/update-sub-product", method = RequestMethod.POST)
    public ResponseEntity<?> updateSubProduct(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            productRepository.findProductByProductId(Long.valueOf(request.getString("ProductId")))
                    .ifPresentOrElse(product -> {
                        if (subProductService.validateFeature(product.getProductSubCategoryId(), request.getJSONArray("productFeature")).equals(1)) {
                            responseMap.set(subProductService.updateSubProduct(request));
                        } else {
                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Product features not defined well")
                                    .put("statusMessage", "Product features not defined well");
                        }
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Product Does not exists")
                                .put("statusMessage", "Product Does not exists");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.get().toString());
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
                        request.getLong("productId"))) {
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

    @RequestMapping(value = {"/get-product-by-id"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getProductById(@RequestParam("parent") Optional<String> parent,
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
                    AtomicReference<JSONObject> detail = new AtomicReference<>(new JSONObject());
                    specificationsDao.getProducts(params)
                            .forEach(pro -> {
                                detail.set(productService.getProductLimitedDetails(pro.getProductId()));
                            });
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("detail", detail.get())
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-sub-product-by-id"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getSubProductById(@RequestParam("subProduct") String subProduct) {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    List<SearchCriteria> params = new ArrayList<SearchCriteria>();
                    params.add(new SearchCriteria("merchantId", ":", merchant.getMerchantId()));
                    AtomicReference<JSONObject> detail = new AtomicReference<>(new JSONObject());
                    detail.set(subProductService.getSubProductInfo(Long.valueOf(subProduct), "ETB"));
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("detail", detail.get())
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
                        JSONObject updateRes = productService.enableDisableAccount(request);
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
