package com.commerce.pal.backend.controller.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.ProductService;
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
@RequestMapping({"/prime/api/v1/distributor/merchant"})
@SuppressWarnings("Duplicates")
public class DistributorMerchantController {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final SpecificationsDao specificationsDao;

    @Autowired
    public DistributorMerchantController(GlobalMethods globalMethods,
                                         ProductService productService,
                                         SpecificationsDao specificationsDao) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.specificationsDao = specificationsDao;
    }

    @RequestMapping(value = {"/merchant-product"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getMerchantProduct(@RequestParam("merchant") Long merchant,
                                                @RequestParam("parent") Optional<String> parent,
                                                @RequestParam("category ") Optional<String> category,
                                                @RequestParam("subCat") Optional<String> subCat,
                                                @RequestParam("brand") Optional<String> brand,
                                                @RequestParam("product") Optional<String> product) {
        JSONObject responseMap = new JSONObject();

        LoginValidation user = globalMethods.fetchUserDetails();
        List<JSONObject> details = new ArrayList<>();
        if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
            if (globalMethods.validateDistUser(
                    globalMethods.getDistributorId(user.getEmailAddress()),
                    "MERCHANT", String.valueOf(merchant))) {
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
                params.add(new SearchCriteria("merchantId", ":", merchant));

                specificationsDao.getProducts(params)
                        .forEach(pro -> {
                            JSONObject detail = productService.getProductLimitedDetails(pro.getProductId());
                            details.add(detail);
                        });
            }
        }
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");


        return ResponseEntity.ok(responseMap.toString());
    }
}
