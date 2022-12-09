package com.commerce.pal.backend.controller.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final BusinessRepository businessRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;

    @Autowired
    public BusinessShoppingController(GlobalMethods globalMethods,
                                      ProductService productService,
                                      CategoryService categoryService,
                                      SpecificationsDao specificationsDao,
                                      BusinessRepository businessRepository,
                                      ProductCategoryRepository productCategoryRepository,
                                      ProductSubCategoryRepository productSubCategoryRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.categoryService = categoryService;
        this.specificationsDao = specificationsDao;
        this.businessRepository = businessRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
    }

    @RequestMapping(value = {"/get-categories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getCategories() {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        LoginValidation user = globalMethods.fetchUserDetails();
        businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(business -> {
                    productCategoryRepository.findProductCategoriesByParentCategoryId(Long.valueOf(business.getBusinessSector()))
                            .forEach(cat -> {
                                JSONObject detail = categoryService.getCategoryInfo(cat.getId());
                                details.add(detail);
                            });
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-sub-categories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getSubCategories(@RequestParam("category") String category) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        LoginValidation user = globalMethods.fetchUserDetails();
        businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(business -> {
                    productCategoryRepository.findProductCategoryByIdAndParentCategoryId(
                                    Long.valueOf(category), Long.valueOf(business.getBusinessSector()))
                            .ifPresentOrElse(cat -> {
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
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Business Does not exists")
                            .put("statusMessage", "Business Does not exists");
                });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-list-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
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
        params.add(new SearchCriteria("productType", ":", "RETAIL"));
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

    @RequestMapping(value = {"/get-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getProducts(@RequestParam("category") Optional<String> category,
                                         @RequestParam("subCat") Optional<String> subCat,
                                         @RequestParam("brand") Optional<String> brand,
                                         @RequestParam("product") Optional<String> product,
                                         @RequestParam("unique_id") Optional<String> uniqueId) {
        JSONObject responseMap = new JSONObject();

        LoginValidation user = globalMethods.fetchUserDetails();
        businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(business -> {
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
                                JSONObject detail = productService.getProductDetail(pro);
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
