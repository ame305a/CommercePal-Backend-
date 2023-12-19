package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.categories.BrandImage;
import com.commerce.pal.backend.models.product.categories.ProductCategory;
import com.commerce.pal.backend.models.product.categories.ProductParentCategory;
import com.commerce.pal.backend.models.product.categories.ProductSubCategory;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductCategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.ProductImageRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.categories.BrandImageRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductParentCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    private final ProductCategoryService productCategoryService;

    @Autowired
    public CategoriesController(ProductService productService,
                                CategoryService categoryService,
                                ProductRepository productRepository,
                                SpecificationsDao specificationsDao,
                                BrandImageRepository brandImageRepository,
                                ProductImageRepository productImageRepository,
                                ProductCategoryRepository productCategoryRepository,
                                ProductSubCategoryRepository productSubCategoryRepository,
                                ProductParentCategoryRepository productParentCategoryRepository, ProductCategoryService productCategoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.specificationsDao = specificationsDao;
        this.brandImageRepository = brandImageRepository;
        this.productImageRepository = productImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.productParentCategoryRepository = productParentCategoryRepository;
        this.productCategoryService = productCategoryService;
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
                        productParentCategoryRepository.save(par);
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
                        productCategoryRepository.save(par);
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
            AtomicReference<ProductSubCategory> par = new AtomicReference<>(new ProductSubCategory());
            par.get().setSubCategoryName(jsonObject.getString("name"));
            par.get().setProductCategoryId(jsonObject.getLong("categoryId"));
            par.get().setCreatedDate(Timestamp.from(Instant.now()));
            par.get().setStatus(1);
            par.set(productSubCategoryRepository.save(par.get()));
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("details", categoryService.getSubCategoryInfo(par.get().getId()))
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
                        productSubCategoryRepository.save(par);
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
            brandImageRepository.findBrandImageByBrand(jsonObject.getString("name"))
                    .ifPresentOrElse(brandImage -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "Request failed. Brand Already Exist")
                                .put("statusMessage", "Request failed. Brand Already Exist");
                    }, () -> {
                        AtomicReference<BrandImage> brandImage = new AtomicReference<>(new BrandImage());
                        brandImage.get().setBrand(jsonObject.getString("name"));
                        brandImage.get().setParentCategoryId(jsonObject.getLong("parentCategoryId"));
                        brandImage.get().setStatus(1);
                        brandImage.get().setCreatedDate(Timestamp.from(Instant.now()));
                        brandImage.set(brandImageRepository.save(brandImage.get()));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("id", brandImage.get().getId())
                                .put("statusMessage", "Request Successful");
                    });
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", ex.getMessage())
                    .put("statusMessage", ex.getMessage());
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
                        par.setParentCategoryId(jsonObject.getLong("parentCategoryId"));
                        brandImageRepository.save(par);
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
    public ResponseEntity<?> GetBrands(@RequestParam("parentCat") Optional<String> parentCat) {
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

    @RequestMapping(value = {"/report"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ResponseEntity<?> getAllProductCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long filterByParentCategory,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by category Name in ascending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "categoryName";

            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = productCategoryService.getAllProductCategories(page, size, sort, filterByParentCategory, status, searchKeyword, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "PRODUCT CATEGORY REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }

    @GetMapping(value = {"/get-all"}, produces = {"application/json"})
    public ResponseEntity<?> getAllProductCategories(@RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, "categoryName");

            JSONObject response = productCategoryService.getAllProductCategories(sort);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "GET ALL PRODUCT CATEGORIES: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }
}
