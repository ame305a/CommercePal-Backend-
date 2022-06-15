package com.commerce.pal.backend.controller.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.user.BusinessRepository;
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
    private final SpecificationsDao specificationsDao;
    private final BusinessRepository businessRepository;

    @Autowired
    public BusinessShoppingController(GlobalMethods globalMethods,
                                      ProductService productService,
                                      SpecificationsDao specificationsDao,
                                      BusinessRepository businessRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.specificationsDao = specificationsDao;
        this.businessRepository = businessRepository;
    }

    @RequestMapping(value = {"/get-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getProducts(@RequestParam("parent") Optional<String> parent,
                                         @RequestParam("category ") Optional<String> category,
                                         @RequestParam("subCat") Optional<String> subCat,
                                         @RequestParam("brand") Optional<String> brand,
                                         @RequestParam("product") Optional<String> product) {
        JSONObject responseMap = new JSONObject();

        LoginValidation user = globalMethods.fetchUserDetails();
        businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(business -> {
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
                    params.add(new SearchCriteria("status", ":", 1));
                    params.add(new SearchCriteria("businessSector", ":", business.getBusinessSector()));
                    params.add(new SearchCriteria("productType", ":", "WHOLESALE"));

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
