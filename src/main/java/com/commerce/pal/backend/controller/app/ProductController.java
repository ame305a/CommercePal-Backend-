package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.ProductFeatureRepository;
import com.commerce.pal.backend.repo.product.SubProductRepository;
import com.commerce.pal.backend.repo.product.categories.BrandImageRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/app"})
@SuppressWarnings("Duplicates")
public class ProductController {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final SpecificationsDao specificationsDao;
    private final ProductRepository productRepository;
    private final BrandImageRepository brandImageRepository;
    private final SubProductRepository subProductRepository;
    private final ProductDatabaseService productDatabaseService;
    private final ProductFeatureRepository productFeatureRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;

    @Autowired
    public ProductController(GlobalMethods globalMethods,
                             ProductService productService,
                             CategoryService categoryService,
                             SpecificationsDao specificationsDao,
                             ProductRepository productRepository,
                             BrandImageRepository brandImageRepository,
                             SubProductRepository subProductRepository,
                             ProductDatabaseService productDatabaseService,
                             ProductFeatureRepository productFeatureRepository,
                             ProductCategoryRepository productCategoryRepository,
                             ProductSubCategoryRepository productSubCategoryRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.categoryService = categoryService;
        this.specificationsDao = specificationsDao;
        this.productRepository = productRepository;
        this.brandImageRepository = brandImageRepository;
        this.subProductRepository = subProductRepository;
        this.productDatabaseService = productDatabaseService;
        this.productFeatureRepository = productFeatureRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
    }

    @RequestMapping(value = {"/dynamic-category"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getDynamicCategory() {
        JSONObject responseMap = new JSONObject();
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("data", categoryService.dynamicCategories())
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-brands"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getBrands(@RequestParam("parentCat") Optional<String> parentCat) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        parentCat.ifPresentOrElse(parCat -> {
            brandImageRepository.findBrandImagesByParentCategoryId(Long.valueOf(parCat))
                    .forEach(cat -> {
                        JSONObject detail = categoryService.getBrandInfo(cat.getId());
                        details.add(detail);
                    });
        }, () -> {
            brandImageRepository.findAll()
                    .forEach(cat -> {
                        JSONObject detail = categoryService.getBrandInfo(cat.getId());
                        details.add(detail);
                    });
        });

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-parent-categories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getParentCategories() {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = categoryService.getParentCategories();
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-categories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getCategories(@RequestParam("parentCat") Optional<String> parentCat) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        parentCat.ifPresentOrElse(parCat -> {
            productCategoryRepository.findProductCategoriesByParentCategoryId(Long.valueOf(parCat))
                    .forEach(cat -> {
                        JSONObject detail = categoryService.getCategoryInfo(cat.getId());
                        details.add(detail);
                    });
        }, () -> {
            productCategoryRepository.findAll()
                    .forEach(cat -> {
                        JSONObject detail = categoryService.getCategoryInfo(cat.getId());
                        details.add(detail);
                    });
        });

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-sub-categories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetSubCategories(@RequestParam("category") Optional<String> category) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        category.ifPresentOrElse(cate -> {
            productSubCategoryRepository.findProductSubCategoriesByProductCategoryId(Long.valueOf(cate))
                    .forEach(cat -> {
                        JSONObject detail = categoryService.getSubCategoryInfo(cat.getId());
                        details.add(detail);
                    });
        }, () -> {
            productSubCategoryRepository.findAll()
                    .forEach(cat -> {
                        JSONObject detail = categoryService.getSubCategoryInfo(cat.getId());
                        details.add(detail);
                    });
        });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-sub-category-features"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetSubCategoriesFeatures(@RequestParam("sub-category") Optional<String> sub) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        sub.ifPresentOrElse(subCat -> {
            productFeatureRepository.findProductFeaturesBySubCategoryId(Long.valueOf(subCat))
                    .forEach(productFeature -> {
                        JSONObject detail = new JSONObject();
                        detail.put("featureId", productFeature.getId());
                        detail.put("subCategoryId", productFeature.getSubCategoryId());
                        detail.put("featureName", productFeature.getFeatureName());
                        detail.put("unitOfMeasure", productFeature.getUnitOfMeasure());
                        detail.put("variableType", productFeature.getVariableType());
                        details.add(detail);
                    });
        }, () -> {
            productFeatureRepository.findAll().forEach(productFeature -> {
                JSONObject detail = new JSONObject();
                detail.put("featureId", productFeature.getId());
                detail.put("subCategoryId", productFeature.getSubCategoryId());
                detail.put("featureName", productFeature.getFeatureName());
                detail.put("unitOfMeasure", productFeature.getUnitOfMeasure());
                detail.put("variableType", productFeature.getVariableType());
                details.add(detail);
            });
        });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

//    @RequestMapping(value = {"/get-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
//    @ResponseBody
//    public ResponseEntity<?> getProducts(@RequestParam("parent") Optional<String> parent,
//                                         @RequestParam("category ") Optional<String> category,
//                                         @RequestParam("subCat") Optional<String> subCat,
//                                         @RequestParam("brand") Optional<String> brand,
//                                         @RequestParam("product") Optional<String> product,
//                                         @RequestParam("unique_id") Optional<String> uniqueId) {
//        JSONObject responseMap = new JSONObject();
//
//        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
//        parent.ifPresent(value -> {
//            params.add(new SearchCriteria("productParentCateoryId", ":", value));
//        });
//        category.ifPresent(value -> {
//            params.add(new SearchCriteria("productCategoryId", ":", value));
//        });
//        subCat.ifPresent(value -> {
//            params.add(new SearchCriteria("productSubCategoryId", ":", value));
//        });
//        brand.ifPresent(value -> {
//            params.add(new SearchCriteria("manufucturer", ":", value));
//        });
//        product.ifPresent(value -> {
//            params.add(new SearchCriteria("productId", ":", value));
//        });
//        uniqueId.ifPresent(value -> {
//            params.add(new SearchCriteria("productId", ":", Long.valueOf(globalMethods.getStringValue(value))));
//        });
//        params.add(new SearchCriteria("status", ":", 1));
//        params.add(new SearchCriteria("productType", ":", "RETAIL"));
//
//
//        List<JSONObject> details = new ArrayList<>();
//        specificationsDao.getProducts(params)
//                .forEach(pro -> {
//                    JSONObject detail = productService.getProductDetail(pro.getProductId());
//                    details.add(detail);
//                });
//
//        if (details.isEmpty()) {
//            responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
//        } else {
//            responseMap.put("statusCode", ResponseCodes.SUCCESS);
//        }
//        responseMap.put("statusDescription", "success")
//                .put("details", details)
//                .put("statusMessage", "Request Successful");
//        return ResponseEntity.ok(responseMap.toString());
//    }

    @GetMapping(value = "/get-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProducts(@RequestParam("parent") Optional<String> parent,
                                              @RequestParam("category") Optional<String> category,
                                              @RequestParam("subCat") Optional<String> subCat,
                                              @RequestParam("brand") Optional<String> brand,
                                              @RequestParam("product") Optional<String> product,
                                              @RequestParam("unique_id") Optional<String> uniqueId) {

        List<Product> products = productRepository.findAll((Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            parent.ifPresent(p -> predicates.add(criteriaBuilder.equal(root.get("productParentCateoryId"), p)));
            category.ifPresent(c -> predicates.add(criteriaBuilder.equal(root.get("productCategoryId"), c)));
            subCat.ifPresent(s -> predicates.add(criteriaBuilder.equal(root.get("productSubCategoryId"), s)));
            brand.ifPresent(b -> predicates.add(criteriaBuilder.equal(root.get("manufucturer"), b)));
            product.ifPresent(p -> predicates.add(criteriaBuilder.equal(root.get("productId"), p)));
            uniqueId.ifPresent(u -> predicates.add(criteriaBuilder.equal(root.get("productId"), Long.valueOf(globalMethods.getStringValue(u)))));

            predicates.add(criteriaBuilder.equal(root.get("status"), 1));
            predicates.add(criteriaBuilder.equal(root.get("productType"), "RETAIL"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = productService.getProductDetail(pro.getProductId());
            details.add(detail);
        });

        JSONObject responseMap = new JSONObject();
        if (details.isEmpty()) responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
        else responseMap.put("statusCode", ResponseCodes.SUCCESS);

        responseMap.put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");

        return ResponseEntity.ok(responseMap.toString());
    }

    @GetMapping(value = "/get-list-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getListProducts(@RequestParam("parent") Optional<String> parent,
                                                  @RequestParam("category ") Optional<String> category,
                                                  @RequestParam("subCat") Optional<String> subCat,
                                                  @RequestParam("brand") Optional<String> brand,
                                                  @RequestParam("product") Optional<String> product,
                                                  @RequestParam("unique_id") Optional<String> uniqueId) {

        List<Product> products = productRepository.findAll((Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            parent.ifPresent(p -> predicates.add(criteriaBuilder.equal(root.get("productParentCateoryId"), p)));
            category.ifPresent(c -> predicates.add(criteriaBuilder.equal(root.get("productCategoryId"), c)));
            subCat.ifPresent(s -> predicates.add(criteriaBuilder.equal(root.get("productSubCategoryId"), s)));
            brand.ifPresent(b -> predicates.add(criteriaBuilder.equal(root.get("manufucturer"), b)));
            product.ifPresent(p -> predicates.add(criteriaBuilder.equal(root.get("productId"), p)));
            uniqueId.ifPresent(u -> predicates.add(criteriaBuilder.equal(root.get("productId"), Long.valueOf(globalMethods.getStringValue(u)))));

            predicates.add(criteriaBuilder.equal(root.get("status"), 1));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), 1));
            predicates.add(criteriaBuilder.equal(root.get("productType"), "RETAIL"));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = productService.getProductDetail(pro.getProductId());
            details.add(detail);
        });

        JSONObject responseMap = new JSONObject();
        if (details.isEmpty()) responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
        else responseMap.put("statusCode", ResponseCodes.SUCCESS);

        responseMap.put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");

        return ResponseEntity.ok(responseMap.toString());
    }

//    @RequestMapping(value = {"/get-list-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
//    @ResponseBody
//    public ResponseEntity<?> getListProducts(@RequestParam("parent") Optional<String> parent,
//                                             @RequestParam("category ") Optional<String> category,
//                                             @RequestParam("subCat") Optional<String> subCat,
//                                             @RequestParam("brand") Optional<String> brand,
//                                             @RequestParam("product") Optional<String> product,
//                                             @RequestParam("unique_id") Optional<String> uniqueId) {
//        JSONObject responseMap = new JSONObject();
//
//        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
//        parent.ifPresent(value -> {
//            params.add(new SearchCriteria("productParentCateoryId", ":", value));
//        });
//        category.ifPresent(value -> {
//            params.add(new SearchCriteria("productCategoryId", ":", value));
//        });
//        subCat.ifPresent(value -> {
//            params.add(new SearchCriteria("productSubCategoryId", ":", value));
//        });
//        brand.ifPresent(value -> {
//            params.add(new SearchCriteria("manufucturer", ":", value));
//        });
//        product.ifPresent(value -> {
//            params.add(new SearchCriteria("productId", ":", value));
//        });
//        uniqueId.ifPresent(value -> {
//            params.add(new SearchCriteria("productId", ":", Long.valueOf(globalMethods.getStringValue(value))));
//        });
//
//        params.add(new SearchCriteria("status", ":", 1));
//        params.add(new SearchCriteria("quantity", ">", 1));
//        params.add(new SearchCriteria("productType", ":", "RETAIL"));
//        List<JSONObject> details = new ArrayList<>();
//        specificationsDao.getProducts(params)
//                .forEach(pro -> {
//                    JSONObject detail = productService.getProductListDetailsAlready(pro);
//                    detail.put("unique_id", globalMethods.generateUniqueString(pro.getProductId().toString()));
//                    details.add(detail);
//                });
//        if (details.isEmpty()) {
//            responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
//        } else {
//            responseMap.put("statusCode", ResponseCodes.SUCCESS);
//        }
//        responseMap.put("statusDescription", "success")
//                .put("details", details)
//                .put("statusMessage", "Request Successful");
//        return ResponseEntity.ok(responseMap.toString());
//    }


    @RequestMapping(value = {"/search-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> searchProducts(@RequestParam("parent") Optional<String> parent,
                                            @RequestParam("reqName") String reqName) {
        JSONObject responseMap = new JSONObject();

        List<SearchCriteria> params = new ArrayList<SearchCriteria>();

        List<JSONObject> details = new ArrayList<>();
        productRepository.findProductByProductId(reqName, reqName, reqName, reqName, "RETAIL")
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

    @RequestMapping(value = "/get-pricing", method = RequestMethod.POST)
    public ResponseEntity<?> updatePricing(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(checkOut);
            productRepository.findById(Long.valueOf(request.getInt("productId")))
                    .ifPresentOrElse(product -> {
                        subProductRepository.findSubProductsByProductIdAndSubProductId(
                                product.getProductId(), Long.valueOf(request.getInt("subProductId"))
                        ).ifPresentOrElse(subProduct -> {
                            JSONObject proValue = new JSONObject();
                            proValue.put("UnitPrice", subProduct.getUnitPrice());
                            proValue.put("Quantity", request.getInt("quantity"));
                            proValue.put("IsDiscounted", subProduct.getIsDiscounted());
                            Double discountAmount = 0D;
                            if (subProduct.getIsDiscounted().equals(1)) {
                                proValue.put("DiscountType", subProduct.getDiscountType());
                                if (subProduct.getDiscountType().equals("FIXED")) {
                                    proValue.put("DiscountValue", subProduct.getDiscountValue());
                                    proValue.put("DiscountAmount", subProduct.getDiscountValue());
                                    proValue.put("discountDescription", subProduct.getDiscountValue() + " " + product.getCurrency());
                                } else {
                                    discountAmount = subProduct.getUnitPrice().doubleValue() * subProduct.getDiscountValue().doubleValue() / 100;
                                    proValue.put("DiscountValue", subProduct.getDiscountValue());
                                    proValue.put("DiscountAmount", new BigDecimal(discountAmount));
                                    proValue.put("discountDescription", subProduct.getDiscountValue() + "% Discount");
                                }
                                proValue.put("offerPrice", subProduct.getUnitPrice().doubleValue() - discountAmount);
                            } else {
                                proValue.put("DiscountType", "NotDiscounted");
                                proValue.put("DiscountValue", new BigDecimal(0));
                                proValue.put("DiscountAmount", new BigDecimal(0));
                                proValue.put("offerPrice", subProduct.getUnitPrice());
                                proValue.put("discountDescription", subProduct.getDiscountValue() + " " + product.getCurrency());
                            }
                            BigDecimal subProductPrice = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount);
                            JSONObject chargeBdy = productDatabaseService.calculateProductPrice(subProductPrice);
                            proValue.put("offerPrice", chargeBdy.getBigDecimal("FinalPrice").doubleValue());

                            proValue.put("TotalUnitPrice", new BigDecimal(chargeBdy.getBigDecimal("FinalPrice").doubleValue() * Double.valueOf(request.getInt("quantity"))));
                            proValue.put("TotalDiscount", new BigDecimal(proValue.getBigDecimal("DiscountAmount").doubleValue() * Double.valueOf(request.getInt("quantity"))));
                            proValue.put("FinalPrice", proValue.getBigDecimal("TotalUnitPrice").doubleValue() - proValue.getBigDecimal("TotalDiscount").doubleValue());

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("productPricing", proValue)
                                    .put("statusDescription", "Product Passed")
                                    .put("statusMessage", "Product Passed");
                        }, () -> {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Invalid Product Passed")
                                    .put("statusMessage", "Invalid Product Passed");
                        });
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Invalid Product Passed")
                                .put("statusMessage", "Invalid Product Passed");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, "GET PRICING ERROR : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
