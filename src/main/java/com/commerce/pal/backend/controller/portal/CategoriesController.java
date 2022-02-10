package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.ProductCategory;
import com.commerce.pal.backend.models.product.ProductParentCategory;
import com.commerce.pal.backend.models.product.ProductSubCategory;
import com.commerce.pal.backend.module.ProductService;
import com.commerce.pal.backend.repo.product.*;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/product"})
@SuppressWarnings("Duplicates")
public class CategoriesController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final SpecificationsDao specificationsDao;
    private final ProductImageRepository productImageRepository;

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final ProductParentCategoryRepository productParentCategoryRepository;

    @Autowired
    public CategoriesController(ProductService productService,
                                ProductRepository productRepository,
                                SpecificationsDao specificationsDao,
                                ProductImageRepository productImageRepository,
                                ProductCategoryRepository productCategoryRepository,
                                ProductSubCategoryRepository productSubCategoryRepository,
                                ProductParentCategoryRepository productParentCategoryRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.specificationsDao = specificationsDao;
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
        List<JSONObject> details = new ArrayList<>();
        productParentCategoryRepository.findAll().forEach(cat -> {
            JSONObject detail = new JSONObject();
            detail.put("id", cat.getId());
            detail.put("name", cat.getParentCategoryName());
            detail.put("mobileImage", "" + cat.getMobileImage());
            detail.put("webImage", cat.getWebImage());
            detail.put("unique_name", cat.getParentCategoryName().replaceAll(" ", "_").toLowerCase().trim());
            details.add(detail);
        });
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
                        JSONObject detail = new JSONObject();
                        detail.put("id", cat.getId());
                        detail.put("parentCategoryId", cat.getParentCategoryId());
                        detail.put("name", cat.getCategoryName());
                        detail.put("mobileImage", "" + cat.getCategoryMobileImage());
                        detail.put("webImage", cat.getCategoryWebImage());
                        detail.put("unique_name", cat.getCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                        details.add(detail);
                    });
        }, () -> {
            productCategoryRepository.findAll()
                    .forEach(cat -> {
                        JSONObject detail = new JSONObject();
                        detail.put("id", cat.getId());
                        detail.put("name", cat.getCategoryName());
                        detail.put("parentCategoryId", cat.getParentCategoryId());
                        detail.put("mobileImage", "" + cat.getCategoryMobileImage());
                        detail.put("webImage", cat.getCategoryWebImage());
                        detail.put("unique_name", cat.getCategoryName().replaceAll(" ", "_").toLowerCase().trim());
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
                        JSONObject detail = new JSONObject();
                        detail.put("id", cat.getId());
                        detail.put("categoryId", cat.getProductCategoryId());
                        detail.put("name", cat.getSubCategoryName());
                        detail.put("mobileImage", "" + cat.getMobileImage());
                        detail.put("webImage", cat.getWebImage());
                        detail.put("unique_name", cat.getSubCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                        details.add(detail);
                    });
        }, () -> {
            productSubCategoryRepository.findAll()
                    .forEach(cat -> {
                        JSONObject detail = new JSONObject();
                        detail.put("id", cat.getId());
                        detail.put("name", cat.getSubCategoryName());
                        detail.put("categoryId", cat.getProductCategoryId());
                        detail.put("mobileImage", "" + cat.getMobileImage());
                        detail.put("webImage", cat.getWebImage());
                        detail.put("unique_name", cat.getSubCategoryName().replaceAll(" ", "_").toLowerCase().trim());
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

    @RequestMapping(value = {"/GetProducts"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetProducts(@RequestParam("parent") Optional<String> parent,
                                         @RequestParam("category ") Optional<String> category,
                                         @RequestParam("subCat") Optional<String> subCat,
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
        product.ifPresent(value -> {
            params.add(new SearchCriteria("productId", ":", value));
        });

        params.add(new SearchCriteria("status", ":", 1));

        List<JSONObject> details = new ArrayList<>();

        specificationsDao.getProducts(params)
                .forEach(pro -> {
                    JSONObject detail = new JSONObject();
                    detail.put("ProductId", pro.getProductId());
                    detail.put("ProductName", pro.getProductName());
                    detail.put("mobileImage", "" + pro.getProductMobileImage());
                    detail.put("mobileVideo", "" + pro.getProductMobileVideo());
                    detail.put("webImage", pro.getProductImage());
                    detail.put("webVideo", pro.getProductWebVideo());
                    detail.put("ProductParentCategoryId", pro.getProductParentCateoryId());
                    detail.put("ProductCategoryId", pro.getProductCategoryId());
                    detail.put("ProductSubCategoryId", pro.getProductSubCategoryId());
                    detail.put("ProductDescription", pro.getProductDescription());
                    detail.put("SpecialInstruction", pro.getSpecialInstruction());
                    detail.put("IsDiscounted", pro.getIsDiscounted());
                    detail.put("ShipmentType", pro.getShipmentType());
                    detail.put("UnitPrice", pro.getUnitPrice());
                    if (pro.getIsDiscounted().equals(1)) {
                        detail.put("DiscountType", pro.getDiscountType());

                        Double discountAmount = 0D;
                        if (pro.getDiscountType().equals("FIXED")) {
                            detail.put("DiscountValue", pro.getDiscountValue());
                            detail.put("DiscountAmount", pro.getDiscountValue());
                        } else {
                            discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                            detail.put("DiscountValue", pro.getDiscountValue());
                            detail.put("DiscountAmount", new BigDecimal(discountAmount));
                        }
                    } else {
                        detail.put("DiscountType", "NotDiscounted");
                        detail.put("DiscountValue", new BigDecimal(0));
                        detail.put("DiscountAmount", new BigDecimal(0));
                    }

                    ArrayList<String> images = new ArrayList<String>();
                    productImageRepository.findProductImagesByProductId(pro.getProductId()).forEach(
                            image -> {
                                images.add(image.getFilePath());
                            }
                    );
                    detail.put("ProductImages", images);
                    details.add(detail);
                });

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }


    @RequestMapping(value = {"/GetProductById"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetProductById(@RequestParam("product") String product) {
        JSONObject responseMap = new JSONObject();
        JSONObject detail = new JSONObject();
        productRepository.findById(Long.valueOf(product))
                .ifPresentOrElse(pro -> {
                    detail.put("ProductId", pro.getProductId());
                    detail.put("ProductName", pro.getProductName());
                    detail.put("mobileImage", "" + pro.getProductMobileImage());
                    detail.put("mobileVideo", "" + pro.getProductMobileVideo());
                    detail.put("webImage", pro.getProductImage());
                    detail.put("webVideo", pro.getProductWebVideo());
                    detail.put("ProductParentCategoryId", pro.getProductParentCateoryId());
                    detail.put("ProductCategoryId", pro.getProductCategoryId());
                    detail.put("ProductSubCategoryId", pro.getProductSubCategoryId());
                    detail.put("ProductDescription", pro.getProductDescription());
                    detail.put("SpecialInstruction", pro.getSpecialInstruction());
                    detail.put("IsDiscounted", pro.getIsDiscounted());
                    detail.put("ShipmentType", pro.getShipmentType());
                    detail.put("UnitPrice", pro.getUnitPrice());
                    if (pro.getIsDiscounted().equals(1)) {
                        detail.put("DiscountType", pro.getDiscountType());

                        Double discountAmount = 0D;
                        if (pro.getDiscountType().equals("FIXED")) {
                            detail.put("DiscountValue", pro.getDiscountValue());
                            detail.put("DiscountAmount", pro.getDiscountValue());
                        } else {
                            discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                            detail.put("DiscountValue", pro.getDiscountValue());
                            detail.put("DiscountAmount", new BigDecimal(discountAmount));
                        }
                    } else {
                        detail.put("DiscountType", "NotDiscounted");
                        detail.put("DiscountValue", new BigDecimal(0));
                        detail.put("DiscountAmount", new BigDecimal(0));
                    }
                    ArrayList<String> images = new ArrayList<String>();
                    productImageRepository.findProductImagesByProductId(pro.getProductId()).forEach(
                            image -> {
                                images.add(image.getFilePath());
                            }
                    );
                    detail.put("ProductImages", images);
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("detail", detail)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The product is not found")
                            .put("statusMessage", "The product is not found");
                });

        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = "/add-product", method = RequestMethod.POST)
    public ResponseEntity<?> addProduct(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);

            request.put("merchantId", "0");
            request.put("productImage", "defaultImage.png");
            request.put("isPromoted", "0");
            request.put("isPrioritized", "0");
            request.put("ownerType", "WAREHOUSE");
            JSONObject retDet = productService.doAddProduct(request);
            int returnValue = retDet.getInt("returnValue");
            if (returnValue == 1) {
                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                        .put("statusDescription", "failed to process request")
                        .put("statusMessage", "internal system error");
            } else {
                int exists = retDet.getInt("exists");
                if (exists == 1) {
                    responseMap.put("statusCode", ResponseCodes.REGISTERED)

                            .put("statusDescription", "Product already added")
                            .put("statusMessage", "Product already added");
                } else {
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("productId", retDet.getInt("exists"))
                            .put("statusMessage", "Product successful");
                }
            }

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
