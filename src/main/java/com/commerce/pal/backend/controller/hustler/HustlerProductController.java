package com.commerce.pal.backend.controller.hustler;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.ProductImage;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.repo.product.ProductImageRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping("/prime/api/v1/hustler/products")
public class HustlerProductController {

    private final ProductService productService;
    private final SubProductService subProductService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public HustlerProductController(ProductService productService, SubProductService subProductService, ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productService = productService;
        this.subProductService = subProductService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @PostMapping(value = "/clone-to-retail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cloneProduct(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            productRepository.findById(reqBody.getLong("productId"))
                    .ifPresentOrElse(product -> {
                        JSONObject proBdy = productService.getProductLimitedDetails(product.getProductId());
                        proBdy.put("ownerType", "HUSTLER");
                        proBdy.put("merchantId", reqBody.getString("hustlerId"));
                        proBdy.put("minOrder", reqBody.getString("minOrder"));
                        proBdy.put("maxOrder", reqBody.getString("maxOrder"));
                        proBdy.put("quantity", reqBody.getString("quantity"));
                        proBdy.put("productType", "RETAIL");
                        proBdy.put("unitPrice", reqBody.getString("unitResalePrice"));
                        proBdy.put("isPromoted", "0");
                        proBdy.put("isPrioritized", "0");
                        proBdy.put("isDiscounted", reqBody.has("isDiscounted") ? reqBody.getString("isDiscounted") : "0");
                        proBdy.put("discountType", reqBody.has("discountType") ? reqBody.getString("discountType") : "FIXED");
                        proBdy.put("discountValue", reqBody.has("discountValue") ? reqBody.getString("discountValue") : "0");
                        proBdy.put("createdBy", "hustler-app");

                        JSONObject retDet = productService.doAddProduct(proBdy);
                        int returnValue = retDet.getInt("productId");
                        if (returnValue == 0) {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "Failed to process request")
                                    .put("statusMessage", "internal system error");
                        } else {
                            // Replicate Images
                            productRepository.findProductByProductId(reqBody.getLong("productId"))
                                    .ifPresent(originalProduct -> {
                                        productRepository.findProductByProductId(retDet.getLong("productId"))
                                                .ifPresent(newProduct -> {
                                                    newProduct.setStatus(1);
                                                    newProduct.setStatusComment("ok");
                                                    newProduct.setProductMobileImage(originalProduct.getProductMobileImage() != null ? originalProduct.getProductMobileImage() : "");
                                                    newProduct.setProductImage(originalProduct.getProductImage() != null ? originalProduct.getProductImage() : "");
                                                    newProduct.setProductWebVideo(originalProduct.getProductWebVideo() != null ? originalProduct.getProductWebVideo() : "");
                                                    newProduct.setMobileThumbnail(originalProduct.getWebThumbnail() != null ? originalProduct.getWebThumbnail() : "");
                                                    newProduct.setWebThumbnail(originalProduct.getMobileThumbnail() != null ? originalProduct.getMobileThumbnail() : "");
                                                });
                                        productImageRepository.findProductImagesByProductId(reqBody.getLong("productId"))
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

                            subProductService.replicateSubFeatures(retDet.getLong("subProductId"), proBdy.getLong("primarySubProduct"));
                            subProductService.replicateSubProducts(reqBody.getLong("productId"), retDet.getLong("productId"));

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Success")
                                    .put("clonedProductId", retDet.getInt("productId"))
                                    .put("clonedSubProductId", retDet.getInt("subProductId"))
                                    .put("statusMessage", "Product successful Cloned");
                        }
                    }, () -> responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                            .put("statusDescription", "Request failed")
                            .put("statusMessage", "Product not found"));
        } catch (Exception ex) {
            ex.printStackTrace();
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

}
