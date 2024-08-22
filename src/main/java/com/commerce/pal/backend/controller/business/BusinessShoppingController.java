package com.commerce.pal.backend.controller.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.business.Business;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.SubProductRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/business/shopping"})
@SuppressWarnings("Duplicates")
public class BusinessShoppingController {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final SpecificationsDao specificationsDao;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final SubProductRepository subProductRepository;
    private final ProductDatabaseService productDatabaseService;

    @Autowired
    public BusinessShoppingController(GlobalMethods globalMethods,
                                      ProductService productService,
                                      CategoryService categoryService,
                                      SpecificationsDao specificationsDao,
                                      ProductRepository productRepository, BusinessRepository businessRepository,
                                      ProductCategoryRepository productCategoryRepository,
                                      ProductSubCategoryRepository productSubCategoryRepository, SubProductRepository subProductRepository, ProductDatabaseService productDatabaseService) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.categoryService = categoryService;
        this.specificationsDao = specificationsDao;
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.subProductRepository = subProductRepository;
        this.productDatabaseService = productDatabaseService;
    }

    @GetMapping(value = "/get-categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCategories(HttpServletRequest request) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();

        // Check if the header contains "hustler"
        if ("true".equals(request.getHeader("X-Hustler-Flag"))) {
            productCategoryRepository.findAll().forEach(cat -> {
                JSONObject detail = categoryService.getCategoryInfo(cat.getId());
                details.add(detail);
            });
        } else {
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        productCategoryRepository.findAll().forEach(cat -> {
                            JSONObject detail = categoryService.getCategoryInfo(cat.getId());
                            details.add(detail);
                        });
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Merchant Does not exists")
                                .put("statusMessage", "Merchant Does not exists");
                    });
        }

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }


    @GetMapping(value = "/get-sub-categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSubCategories(HttpServletRequest request, @RequestParam("category") String category) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();

        // Check if the header contains "hustler"
        if ("true".equals(request.getHeader("X-Hustler-Flag"))) {
            productSubCategoryRepository.findProductSubCategoriesByProductCategoryId(Long.valueOf(category))
                    .forEach(subCat -> {
                        JSONObject detail = categoryService.getSubCategoryInfo(subCat.getId());
                        details.add(detail);
                    });
        } else {
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        productSubCategoryRepository.findProductSubCategoriesByProductCategoryId(Long.valueOf(category))
                                .forEach(subCat -> {
                                    JSONObject detail = categoryService.getSubCategoryInfo(subCat.getId());
                                    details.add(detail);
                                });

                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Business Does not exists")
                                .put("statusMessage", "Business Does not exists");
                    });
        }

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-list-products"}, method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getListProducts(@RequestParam("parent") Optional<String> parent,
                                             @RequestParam("category ") Optional<String> category,
                                             @RequestParam("subCat") Optional<String> subCat,
                                             @RequestParam("brand") Optional<String> brand,
                                             @RequestParam("product") Optional<String> product,
                                             @RequestParam("unique_id") Optional<String> uniqueId) {
        JSONObject responseMap = new JSONObject();

        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
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
        uniqueId.ifPresent(value -> {
            params.add(new SearchCriteria("productId", ":", Long.valueOf(globalMethods.getStringValue(value))));
        });

        params.add(new SearchCriteria("status", ":", 1));
        params.add(new SearchCriteria("productType", ":", "WHOLESALE"));
        List<JSONObject> details = new ArrayList<>();
        specificationsDao.getProducts(params)
                .forEach(pro -> {
                    JSONObject detail = productService.getProductListDetailsAlready(pro);
                    detail.put("unique_id", globalMethods.generateUniqueString(pro.getProductId().toString()));
                    details.add(detail);
                });
        if (details.isEmpty()) {
            responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
        } else {
            responseMap.put("statusCode", ResponseCodes.SUCCESS);
        }
        responseMap.put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @GetMapping(value = "/get-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProducts(HttpServletRequest request,
                                         @RequestParam("category") Optional<String> category,
                                         @RequestParam("subCat") Optional<String> subCat,
                                         @RequestParam("brand") Optional<String> brand,
                                         @RequestParam("product") Optional<String> product,
                                         @RequestParam("unique_id") Optional<String> uniqueId) {
        JSONObject responseMap = new JSONObject();

        if ("true".equals(request.getHeader("X-Hustler-Flag"))) {
            // don't authenticate
        } else {
            LoginValidation user = globalMethods.fetchUserDetails();
            Optional<Business> business = businessRepository.findBusinessByEmailAddress(user.getEmailAddress());
            if (business.isEmpty()) {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "Merchant Does not exists")
                        .put("statusMessage", "Merchant Does not exists");

                return ResponseEntity.ok(responseMap.toString());
            }
        }

        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
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
        params.add(new SearchCriteria("status", ":", 1));
//                    params.add(new SearchCriteria("productParentCateoryId", ":", business.getBusinessSector()));
        params.add(new SearchCriteria("productType", ":", "WHOLESALE"));
        uniqueId.ifPresent(value -> {
            params.add(new SearchCriteria("productId", ":", Long.valueOf(globalMethods.getStringValue(value))));
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

        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/search-products"}, method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> searchProducts(@RequestParam("parent") Optional<String> parent,
                                            @RequestParam("reqName") String reqName) {
        JSONObject responseMap = new JSONObject();

        List<SearchCriteria> params = new ArrayList<SearchCriteria>();

        List<JSONObject> details = new ArrayList<>();
        productRepository.findProductByProductId(reqName, reqName, reqName, reqName, "WHOLESALE")
                .forEach(pro -> {
                    JSONObject detail = productService.getProductListDetailsAlready(pro);
                    details.add(detail);
                });
        if (details.isEmpty()) {
            responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
        } else {
            responseMap.put("statusCode", ResponseCodes.SUCCESS);
        }
        responseMap.put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @PostMapping(value = "/products-with-sub-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductsWithSubProduct(@RequestBody String reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONArray items = new JSONArray(reqBody);
            List<JSONObject> data = new ArrayList<>();
            items.forEach(item -> {
                JSONObject itmValue = new JSONObject(item.toString());
                JSONObject productInfo = productService.getSubProductInfo(
                        itmValue.getLong("productId"),
                        itmValue.getLong("subProductId")
                );

                data.add(productInfo);
            });

            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "Request processed successfully")
                    .put("data", data);

        } catch (Exception e) {
            log.log(Level.WARNING, "BUSINESS PRODUCT AND SUB-PRODUCT GET : " + e.getMessage());
            responseMap
                    .put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Failed to process request")
                    .put("statusMessage", "Internal system error");
        }

        return ResponseEntity.ok(responseMap.toString());
    }


    @GetMapping(value = "/product-charge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductWithCharge(@RequestParam Long productId, @RequestParam Long
            subProductId) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject productObject = new JSONObject();

            productRepository.findById(productId).ifPresentOrElse(product -> {
                subProductRepository.findSubProductsByProductIdAndSubProductId(productId, subProductId)
                        .ifPresentOrElse(subProduct -> {

                            BigDecimal discountAmount;
                            if (subProduct.getIsDiscounted().equals(1)) {
                                if (subProduct.getDiscountType().equals("FIXED")) {
                                    discountAmount = subProduct.getDiscountValue();
                                } else {
                                    discountAmount = new BigDecimal(subProduct.getUnitPrice().doubleValue() * subProduct.getDiscountValue().doubleValue() / 100);
                                }
                            } else {
                                discountAmount = new BigDecimal(0);
                            }
                            BigDecimal productDis = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount.doubleValue());

                            JSONObject charge = productDatabaseService.calculateProductPrice(productDis);

                            JSONObject chargeDetail = new JSONObject();
                            chargeDetail.put("chargeId", charge.getInt("ChargeId"));
                            chargeDetail.put("charge", charge.getBigDecimal("Charge"));
                            chargeDetail.put("finalPrice", charge.getBigDecimal("FinalPrice"));

                            JSONObject productDetail = new JSONObject();
                            productDetail.put("productId", product.getProductId());
                            productDetail.put("ownerType", product.getOwnerType());
                            productDetail.put("merchantId", product.getMerchantId());
                            productDetail.put("productName", product.getProductName());
                            productDetail.put("merchantId", product.getMerchantId());

                            JSONObject subProductDetail = new JSONObject();
                            subProductDetail.put("SubProductId", subProduct.getSubProductId());
                            subProductDetail.put("UnitPrice", subProduct.getUnitPrice());
                            subProductDetail.put("IsDiscounted", subProduct.getIsDiscounted());
                            subProductDetail.put("discountType", subProduct.getDiscountType());
                            subProductDetail.put("discountValue", subProduct.getDiscountValue());

                            productObject.put("product", productDetail);
                            productObject.put("subProduct", subProductDetail);
                            productObject.put("chargeDetail", chargeDetail);

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Request processed successfully")
                                    .put("data", productObject);
                        }, () -> {
                            // Handle case when sub-product is not found
                            responseMap.put("statusCode", ResponseCodes.RECORD_NOT_FOUND)
                                    .put("statusDescription", "Sub-product not found for the given productId: " + productId + ", subProductId: " + subProductId)
                                    .put("statusMessage", "No matching sub-product found");
                        });
            }, () -> {
                // Handle case when product is not found
                responseMap.put("statusCode", ResponseCodes.RECORD_NOT_FOUND)
                        .put("statusDescription", "Product not found for the given productId: " + productId)
                        .put("statusMessage", "No matching product found");
            });
        } catch (Exception e) {
            log.log(Level.WARNING, "BUSINESS PRODUCT AND SUB-PRODUCT GET : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Failed to process request")
                    .put("statusMessage", "Internal system error");
        }
        return ResponseEntity.ok(responseMap.toString());
    }


}
