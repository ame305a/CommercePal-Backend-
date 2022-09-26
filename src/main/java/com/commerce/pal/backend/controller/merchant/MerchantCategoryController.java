package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/merchant/category"})
@SuppressWarnings("Duplicates")
public class MerchantCategoryController {
    private final GlobalMethods globalMethods;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;

    @Autowired
    public MerchantCategoryController(GlobalMethods globalMethods,
                                      CategoryService categoryService,
                                      ProductRepository productRepository,
                                      MerchantRepository merchantRepository) {
        this.globalMethods = globalMethods;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.merchantRepository = merchantRepository;
    }

    @RequestMapping(value = {"/get-parent-category"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getParentCategory() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    List<JSONObject> data = new ArrayList<>();
                    productRepository.findProductsByProductParentCateoryId(merchant.getMerchantId())
                            .forEach(parentCategory -> {
                                data.add(categoryService.getCategoryInfo(parentCategory));
                            });
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("data", data)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-category"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getCategory(@RequestParam("parentCat") String parentCat) {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    List<JSONObject> data = new ArrayList<>();
                    productRepository.findProductsByProductCategoryId(merchant.getMerchantId(), Long.valueOf(parentCat))
                            .forEach(parentCategory -> {
                                data.add(categoryService.getCategoryInfo(parentCategory));
                            });
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("data", data)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-sub-category"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getSubCategory(@RequestParam("category") String category) {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    List<JSONObject> data = new ArrayList<>();
                    productRepository.findProductsByProductSubCategoryId(merchant.getMerchantId(), Long.valueOf(category))
                            .forEach(parentCategory -> {
                                data.add(categoryService.getSubCategoryInfo(parentCategory));
                            });
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("data", data)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }
}
