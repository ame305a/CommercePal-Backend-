package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.categories.BrandImage;
import com.commerce.pal.backend.models.product.categories.ProductCategory;
import com.commerce.pal.backend.models.product.categories.ProductParentCategory;
import com.commerce.pal.backend.models.product.categories.ProductSubCategory;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.*;
import com.commerce.pal.backend.repo.product.categories.BrandImageRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductParentCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal/category"})
@SuppressWarnings("Duplicates")
public class CategoriesController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final SpecificationsDao specificationsDao;
    private final BrandImageRepository brandImageRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final ProductParentCategoryRepository productParentCategoryRepository;

    @Autowired
    public CategoriesController(ProductService productService,
                                CategoryService categoryService,
                                ProductRepository productRepository,
                                SpecificationsDao specificationsDao,
                                BrandImageRepository brandImageRepository,
                                ProductImageRepository productImageRepository,
                                ProductCategoryRepository productCategoryRepository,
                                ProductSubCategoryRepository productSubCategoryRepository,
                                ProductParentCategoryRepository productParentCategoryRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.specificationsDao = specificationsDao;
        this.brandImageRepository = brandImageRepository;
        this.productImageRepository = productImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.productParentCategoryRepository = productParentCategoryRepository;
    }




    @RequestMapping(value = {"/AddParentCategory"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> addParentCategory(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            ProductParentCategory par = new ProductParentCategory();
            par.setParentCategoryName(jsonObject.getString("name"));
            par.setCreatedDate(Timestamp.from(Instant.now()));
            par.setStatus(1);
            productParentCategoryRepository.save(par);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/GetParentCategories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetParentCategories() {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = categoryService.getParentCategories();
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }


    @RequestMapping(value = {"/UpdateParentCategory"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> UpdateParentCategory(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            productParentCategoryRepository.findById(jsonObject.getLong("id"))
                    .ifPresentOrElse(par -> {
                        par.setParentCategoryName(jsonObject.getString("name"));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request failed");
                    });
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/AddCategory"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> addCategory(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            ProductCategory par = new ProductCategory();
            par.setParentCategoryId(jsonObject.getLong("parentCategoryId"));
            par.setCategoryName(jsonObject.getString("name"));
            par.setCreatedDate(Timestamp.from(Instant.now()));
            par.setStatus(1);
            productCategoryRepository.save(par);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/GetCategories"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetCategories(@RequestParam("parentCat") Optional<String> parentCat) {
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

    @RequestMapping(value = {"/UpdateCategory"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> UpdateCategories(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            productCategoryRepository.findById(jsonObject.getLong("id"))
                    .ifPresentOrElse(par -> {
                        par.setCategoryName(jsonObject.getString("name"));
                        par.setParentCategoryId(jsonObject.getLong("parentCategoryId"));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request failed");
                    });
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/AddSubCategory"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> addSubCategory(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            ProductSubCategory par = new ProductSubCategory();
            par.setSubCategoryName(jsonObject.getString("name"));
            par.setProductCategoryId(jsonObject.getLong("categoryId"));
            par.setCreatedDate(Timestamp.from(Instant.now()));
            par.setStatus(1);
            productSubCategoryRepository.save(par);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/GetSubCategories"}, method = {RequestMethod.GET}, produces = {"application/json"})
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


    @RequestMapping(value = {"/UpdateSubCategory"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> UpdateSubCategories(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            productSubCategoryRepository.findById(jsonObject.getLong("id"))
                    .ifPresentOrElse(par -> {
                        par.setSubCategoryName(jsonObject.getString("name"));
                        par.setProductCategoryId(jsonObject.getLong("categoryId"));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request failed");
                    });
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/AddBrand"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> addBrand(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            brandImageRepository.findBrandImageByBrand(jsonObject.getString("brandName"))
                    .ifPresentOrElse(brandImage -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "Request failed. Brand Already Exist")
                                .put("statusMessage", "Request failed. Brand Already Exist");
                    }, () -> {
                        BrandImage brandImage = new BrandImage();
                        brandImage.setBrand(jsonObject.getString("brandName"));
                        brandImage.setStatus(1);
                        brandImage.setCreatedDate(Timestamp.from(Instant.now()));
                        brandImageRepository.save(brandImage);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    });

        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/UpdateBrand"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> updateBrand(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            brandImageRepository.findById(jsonObject.getLong("id"))
                    .ifPresentOrElse(par -> {
                        par.setBrand(jsonObject.getString("name"));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request failed");
                    });
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/GetBrands"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetBrands() {
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
}
