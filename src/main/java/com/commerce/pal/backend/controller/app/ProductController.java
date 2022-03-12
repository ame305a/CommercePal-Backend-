package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.BrandImageRepository;
import com.commerce.pal.backend.repo.product.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.ProductSubCategoryRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
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
@RequestMapping({"/prime/api/v1/app"})
@SuppressWarnings("Duplicates")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SpecificationsDao specificationsDao;
    private final BrandImageRepository brandImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;

    @Autowired
    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             SpecificationsDao specificationsDao,
                             BrandImageRepository brandImageRepository,
                             ProductCategoryRepository productCategoryRepository,
                             ProductSubCategoryRepository productSubCategoryRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.specificationsDao = specificationsDao;
        this.brandImageRepository = brandImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
    }


    @RequestMapping(value = {"/get-brands"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getBrands() {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        brandImageRepository.findAll()
                .forEach(cat -> {
                    JSONObject detail = categoryService.getBrandInfo(cat.getId());
                    details.add(detail);
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

    @RequestMapping(value = {"/get-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getProducts(@RequestParam("parent") Optional<String> parent,
                                         @RequestParam("category ") Optional<String> category,
                                         @RequestParam("subCat") Optional<String> subCat,
                                         @RequestParam("brand") Optional<String> brand,
                                         @RequestParam("product") Optional<String> product) {
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
        params.add(new SearchCriteria("status", ":", 1));
        params.add(new SearchCriteria("productType", ":", "RETAIL"));
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
}
