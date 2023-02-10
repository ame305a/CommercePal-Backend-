package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.ProductImage;
import com.commerce.pal.backend.module.MultiUserService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.repo.product.ProductImageRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.SubProductImageRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping({"/prime/api/v1/portal/product"})
@SuppressWarnings("Duplicates")
public class ProductManagementController {
    /*
    0 - Pending
    1 - Approved
    3 - Disable
    5 - Delete Product
    10 - Freeze
     */

    private final ProductService productService;
    private final MerchantService merchantService;
    private final MultiUserService multiUserService;
    private final SubProductService subProductService;
    private final SpecificationsDao specificationsDao;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final SubProductImageRepository subProductImageRepository;

    @Autowired
    public ProductManagementController(ProductService productService,
                                       MerchantService merchantService,
                                       MultiUserService multiUserService, SubProductService subProductService,
                                       SpecificationsDao specificationsDao,
                                       ProductRepository productRepository,
                                       ProductImageRepository productImageRepository,
                                       SubProductImageRepository subProductImageRepository) {
        this.productService = productService;
        this.merchantService = merchantService;
        this.multiUserService = multiUserService;
        this.subProductService = subProductService;
        this.specificationsDao = specificationsDao;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.subProductImageRepository = subProductImageRepository;
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
            if (subProductService.validateFeature(Long.valueOf(request.getString("productSubCategoryId")), request.getJSONArray("productFeature")).equals(1)) {
                JSONObject retDet = productService.doAddProduct(request);
                int returnValue = retDet.getInt("productId");
                if (returnValue == 0) {
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                } else {
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("productId", retDet.getInt("productId"))
                            .put("subProductId", retDet.getInt("subProductId"))
                            .put("statusMessage", "Product successful");
                    subProductService.updateInsertFeatures(Long.valueOf(retDet.getInt("subProductId")), request.getJSONArray("productFeature"));
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "Product features not defined well")
                        .put("statusMessage", "Product features not defined well");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-product", method = RequestMethod.POST)
    public ResponseEntity<?> updateProduct(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        log.log(Level.INFO, req);
        try {
            JSONObject request = new JSONObject(req);
            responseMap = productService.updateProduct(request);
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/add-sub-product", method = RequestMethod.POST)
    public ResponseEntity<?> addSubProduct(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            productRepository.findProductByProductId(Long.valueOf(request.getString("ProductId")))
                    .ifPresentOrElse(product -> {
                        if (subProductService.validateFeature(product.getProductSubCategoryId(), request.getJSONArray("productFeature")).equals(1)) {
                            responseMap.set(subProductService.addSubProduct(request));
                            product.setStatus(0);
                            product.setStatusComment("Added SubProduct - " + request.getString("shortDescription"));
                            product.setStatusUpdatedDate(Timestamp.from(Instant.now()));
                            productRepository.save(product);
                        } else {
                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Product features not defined well")
                                    .put("statusMessage", "Product features not defined well");
                        }
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Product Does not exists")
                                .put("statusMessage", "Product Does not exists");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.get().toString());
    }

    @RequestMapping(value = "/add-multi-sub-product", method = RequestMethod.POST)
    public ResponseEntity<?> addMultipleProduct(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONArray arrayReq = new JSONArray(req);
            arrayReq.forEach(jsonBody -> {
                JSONObject request = new JSONObject(jsonBody.toString());
                productRepository.findProductByProductId(Long.valueOf(request.getString("ProductId")))
                        .ifPresentOrElse(product -> {
                            if (subProductService.validateFeature(product.getProductSubCategoryId(), request.getJSONArray("productFeature")).equals(1)) {
                                responseMap.set(subProductService.addSubProduct(request));
                                product.setStatus(0);
                                product.setStatusComment("Added SubProduct - " + request.getString("shortDescription"));
                                product.setStatusUpdatedDate(Timestamp.from(Instant.now()));
                                productRepository.save(product);
                            } else {
                                responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                        .put("statusDescription", "Product features not defined well")
                                        .put("statusMessage", "Product features not defined well");
                            }
                        }, () -> {
                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Product Does not exists")
                                    .put("statusMessage", "Product Does not exists");
                        });

            });


        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-sub-product", method = RequestMethod.POST)
    public ResponseEntity<?> updateSubProduct(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            productRepository.findProductByProductId(Long.valueOf(request.getString("ProductId")))
                    .ifPresentOrElse(product -> {
                        if (subProductService.validateFeature(product.getProductSubCategoryId(), request.getJSONArray("productFeature")).equals(1)) {
                            responseMap.set(subProductService.updateSubProduct(request));
                        } else {
                            responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Product features not defined well")
                                    .put("statusMessage", "Product features not defined well");
                        }
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Product Does not exists")
                                .put("statusMessage", "Product Does not exists");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.get().toString());
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

    @RequestMapping(value = {"/GetSubProductById"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getSubByProductId(@RequestParam("product") String product) {
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

    public ResponseEntity<?> getProductOwner(@RequestParam("product") String product) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        productRepository.findById(Long.valueOf(product))
                .ifPresentOrElse(pro -> {
                    if (pro.getOwnerType().equals("MERCHANT")) {
                        JSONObject payload = new JSONObject();
                        payload.put("userType", "MERCHANT");
                        payload.put("userId", pro.getMerchantId().toString());
                        responseMap.set(multiUserService.getAllUser(payload));
                    } else {
                        responseMap.get().put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("detail", "WareHouse Product")
                                .put("statusMessage", "Request Successful");
                    }
                }, () -> {
                    responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
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

    @RequestMapping(value = {"/de-approve-product"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> deApproveProduct(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);

            productRepository.findById(jsonObject.getLong("productId"))
                    .ifPresentOrElse(product -> {
                        product.setStatus(0);
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
    public ResponseEntity<?> deleteProduct(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            productRepository.findById(jsonObject.getLong("productId"))
                    .ifPresentOrElse(product -> {
                        product.setStatus(5);
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

    @RequestMapping(value = {"/link-to-merchant"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> linkProductToMerchant(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            productRepository.findById(jsonObject.getLong("ProductId"))
                    .ifPresentOrElse(product -> {
                        product.setOwnerType("MERCHANT");
                        product.setMerchantId(jsonObject.getLong("MerchantId"));
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

    @RequestMapping(value = {"/reuse-to-merchant"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> reuseProductToMerchant(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            productRepository.findById(jsonObject.getLong("ProductId"))
                    .ifPresentOrElse(product -> {
                        JSONObject proBdy = productService.getProductLimitedDetails(product.getProductId());
                        proBdy.put("ownerType", "MERCHANT");
                        proBdy.put("merchantId", jsonObject.getString("MerchantId"));
                        proBdy.put("isPromoted", "0");
                        proBdy.put("isPrioritized", "0");
                        JSONObject retDet = productService.doAddProduct(proBdy);
                        int returnValue = retDet.getInt("productId");
                        if (returnValue == 0) {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "failed to process request")
                                    .put("statusMessage", "internal system error");
                        } else {
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("productId", retDet.getInt("productId"))
                                    .put("subProductId", retDet.getInt("subProductId"))
                                    .put("statusMessage", "Product successful");

                            // Replicate Images

                            productRepository.findProductByProductId(jsonObject.getLong("ProductId"))
                                    .ifPresent(originalProduct -> {
                                        productRepository.findProductByProductId(retDet.getLong("productId"))
                                                .ifPresent(newProduct -> {
                                                    newProduct.setStatus(1);
                                                    newProduct.setProductMobileImage(originalProduct.getProductMobileImage() != null ? originalProduct.getProductMobileImage() : "");
                                                    newProduct.setProductImage(originalProduct.getProductImage() != null ? originalProduct.getProductImage() : "");
                                                    newProduct.setProductWebVideo(originalProduct.getProductWebVideo() != null ? originalProduct.getProductWebVideo() : "");
                                                    newProduct.setMobileThumbnail(originalProduct.getWebThumbnail() != null ? originalProduct.getWebThumbnail() : "");
                                                    newProduct.setWebThumbnail(originalProduct.getMobileThumbnail() != null ? originalProduct.getMobileThumbnail() : "");
                                                });
                                        productImageRepository.findProductImagesByProductId(jsonObject.getLong("ProductId"))
                                                .forEach(imageList -> {
                                                    ProductImage productImage = new ProductImage();
                                                    productImage.setProductId(retDet.getLong("productId"));
                                                    productImage.setType(imageList.getType());
                                                    productImage.setFilePath(imageList.getFilePath());
                                                    productImage.setMobileImage(imageList.getMobileImage());
                                                    productImage.setStatus(imageList.getStatus());
                                                    productImage.setCreatedDate(Timestamp.from(Instant.now()));
                                                    productImageRepository.save(productImage);
                                                });
                                    });

//                            subProductService.replicateSubFeatures(Long.valueOf(retDet.getInt("subProductId")), proBdy.getLong("primarySubProduct"));
                            subProductService.replicateSubProducts(jsonObject.getLong("ProductId"), retDet.getLong("productId"));
                        }
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

    @RequestMapping(value = {"/replicate-product-image"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> replicateProductImage(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            productRepository.findById(jsonObject.getLong("productId"))
                    .ifPresentOrElse(product -> {

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

    @RequestMapping(value = {"/delete-product-image"}, method = {RequestMethod.POST}, produces = {"application/json"})
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
        } catch (Exception ex) {
            log.log(Level.WARNING, "Delete Product Image : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("errorDescription", ex.getMessage())
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/delete-sub-product-image"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteSubImage(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            Long res = subProductImageRepository.removeSubProductImageBySubProductIdAndImageUrl(
                    jsonObject.getLong("subProductId"), jsonObject.getString("imageName")
            );

            log.log(Level.INFO, "Del Res: " + String.valueOf(res));
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception ex) {
            log.log(Level.WARNING, "Delete Product Image : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("errorDescription", ex.getMessage())
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/approve-product-image"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> approveProductImage(@RequestBody String proBody) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(proBody);
            productImageRepository.findProductImageById(jsonObject.getLong("imageId"))
                    .ifPresentOrElse(productImage -> {
                        productImage.setStatus(1);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request Failed");
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
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
