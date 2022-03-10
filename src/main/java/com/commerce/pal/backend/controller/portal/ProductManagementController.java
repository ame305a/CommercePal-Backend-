package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.multi.MerchantService;
import com.commerce.pal.backend.repo.product.ProductImageRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal/product"})
@SuppressWarnings("Duplicates")
public class ProductManagementController {

    private final ProductService productService;
    private final MerchantService merchantService;
    private final SpecificationsDao specificationsDao;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Autowired
    public ProductManagementController(ProductService productService,
                                       MerchantService merchantService,
                                       SpecificationsDao specificationsDao,
                                       ProductRepository productRepository,
                                       ProductImageRepository productImageRepository) {
        this.productService = productService;
        this.merchantService = merchantService;
        this.specificationsDao = specificationsDao;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
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

    @RequestMapping(value = {"/GetProductById"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetProductById(@RequestParam("product") String product) {
        JSONObject responseMap = new JSONObject();
        productRepository.findById(Long.valueOf(product))
                .ifPresentOrElse(pro -> {
                    JSONObject detail = productService.getProductDetail(pro.getProductId());
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

    @RequestMapping(value = {"/approved"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> approvedProducts(@RequestParam("parent") Optional<String> parent,
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
                    JSONObject detail = productService.getProductDetail(pro.getProductId());
                    details.add(detail);
                });

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/pending"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> pendingApproval(@RequestParam("parent") Optional<String> parent,
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

        params.add(new SearchCriteria("status", ":", 0));

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

    @RequestMapping(value = {"/approve-product"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> approvedProduct(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);

            productRepository.findById(jsonObject.getLong("productId"))
                    .ifPresentOrElse(product -> {
                        product.setStatus(1);
                        productRepository.save(product);
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

    @RequestMapping(value = {"/delete-product"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteProductImage(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            Long res = productImageRepository.removeProductImageByProductIdAndFilePath(
                    jsonObject.getLong("productId"), jsonObject.getString("imageName")
            );

            log.log(Level.INFO, "Del Res: " + String.valueOf(res));
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
//            if (res.equals(1)) {
//
//            } else {
//                responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
//                        .put("statusDescription", "failed")
//                        .put("statusMessage", "Request failed");
//            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Delete Product Image : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("errorDescription", ex.getMessage())
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-merchant-products"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getMerchantProducts(@RequestParam("userId") String userId) {
        JSONObject responseMap = new JSONObject();
        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
        params.add(new SearchCriteria("merchantId", ":", userId));
        List<JSONObject> details = new ArrayList<>();
        JSONObject merchantInfo = new JSONObject();
        merchantInfo = merchantService.getMerchantInfo(Long.valueOf(userId));
        specificationsDao.getProducts(params)
                .forEach(pro -> {
                    JSONObject detail = productService.getProductDetail(pro.getProductId());
                    details.add(detail);
                });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("data", details)
                .put("merchantInfo", merchantInfo)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/GetProducts"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> GetProducts(@RequestParam("parent") Optional<String> parent,
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
