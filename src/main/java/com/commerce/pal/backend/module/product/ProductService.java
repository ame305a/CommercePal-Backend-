package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.repo.product.*;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class ProductService {
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final ProductMoreRepository productMoreRepository;
    private final ProductDatabaseService productDatabaseService;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryService productCategoryService;
    private final ProductMoreTemplateRepository productMoreTemplateRepository;

    @Autowired
    public ProductService(CategoryService categoryService,
                          ProductRepository productRepository,
                          ProductMoreRepository productMoreRepository,
                          ProductDatabaseService productDatabaseService,
                          ProductImageRepository productImageRepository,
                          ProductCategoryService productCategoryService,
                          ProductMoreTemplateRepository productMoreTemplateRepository) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.productMoreRepository = productMoreRepository;
        this.productDatabaseService = productDatabaseService;
        this.productImageRepository = productImageRepository;
        this.productCategoryService = productCategoryService;
        this.productMoreTemplateRepository = productMoreTemplateRepository;
    }

    public JSONObject doAddProduct(JSONObject request) {
        JSONObject retDet = new JSONObject();
        try {
            retDet = productDatabaseService.doAddProduct(request);
        } catch (Exception ex) {
            retDet.put("productId", 0);
            log.log(Level.WARNING, "Error ProductDatabaseService doAddProduct : " + ex.getMessage());
        }
        return retDet;
    }

    public JSONObject enableDisableAccount(JSONObject request) {
        JSONObject retDet = new JSONObject();
        try {
            retDet = productDatabaseService.updateMerchantStatus(request);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Error ProductDatabaseService doAddProduct : " + ex.getMessage());
        }
        return retDet;
    }

    public JSONObject updateProduct(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            productRepository.findProductByProductId(Long.valueOf(reqBody.getLong("productId")))
                    .ifPresentOrElse(product -> {
                        product.setProductName(reqBody.has("productName") ? reqBody.getString("productName") : product.getProductName());
                        product.setShortDescription(reqBody.has("shortDescription") ? reqBody.getString("shortDescription") : product.getShortDescription());
                        product.setProductParentCateoryId(reqBody.has("productParentCateoryId") ? Long.valueOf(reqBody.getString("productParentCateoryId")) : product.getProductParentCateoryId());
                        product.setProductCategoryId(reqBody.has("productCategoryId") ? Long.valueOf(reqBody.getString("productCategoryId")) : product.getProductCategoryId());
                        product.setProductSubCategoryId(reqBody.has("productSubCategoryId") ? Long.valueOf(reqBody.getString("productSubCategoryId")) : product.getProductSubCategoryId());
                        product.setProductDescription(reqBody.has("productDescription") ? reqBody.getString("productDescription") : product.getProductDescription());
                        product.setSpecialInstruction(reqBody.has("specialInstruction") ? reqBody.getString("specialInstruction") : product.getSpecialInstruction());
                        product.setQuantity(reqBody.has("quantity") ? reqBody.getInt("quantity") : product.getQuantity());
                        product.setUnitOfMeasure(reqBody.has("unitOfMeasure") ? reqBody.getString("unitOfMeasure") : product.getUnitOfMeasure());
                        product.setUnitPrice(reqBody.has("unitPrice") ? new BigDecimal(reqBody.getString("unitPrice")) : product.getUnitPrice());
                        product.setCurrency(reqBody.has("currency") ? reqBody.getString("currency") : product.getCurrency());
                        product.setTax(reqBody.has("tax") ? new BigDecimal(reqBody.getString("tax")) : product.getTax());
                        product.setMinOrder(reqBody.has("minOrder") ? Integer.valueOf(reqBody.getString("minOrder")) : product.getMinOrder());
                        product.setMaxOrder(reqBody.has("maxOrder") ? Integer.valueOf(reqBody.getString("maxOrder")) : product.getMaxOrder());
                        product.setSoldQuantity(reqBody.has("soldQuantity") ? Integer.valueOf(reqBody.getString("soldQuantity")) : product.getSoldQuantity());
                        product.setCountryOfOrigin(reqBody.has("countryOfOrigin") ? reqBody.getString("countryOfOrigin") : product.getCountryOfOrigin());
                        product.setManufucturer(reqBody.has("manufucturer") ? reqBody.getString("manufucturer") : product.getManufucturer());
                        product.setIsDiscounted(reqBody.has("isDiscounted") ? Integer.valueOf(reqBody.getString("isDiscounted")) : product.getIsDiscounted());
                        productRepository.save(product);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The Product does not exist")
                                .put("statusMessage", "The Product does not exist");
                    });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject disableProduct(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            productRepository.findProductByProductId(Long.valueOf(reqBody.getLong("productId")))
                    .ifPresentOrElse(product -> {
                        product.setStatus(5);
                        product.setStatusComment(reqBody.getString("StatusComment"));
                        product.setStatusUpdatedDate(Timestamp.from(Instant.now()));
                        productRepository.save(product);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The Product does not exist")
                                .put("statusMessage", "The Product does not exist");
                    });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getProductDetail(Long product) {
        JSONObject detail = new JSONObject();
        try {
            productRepository.findProductByProductId(product)
                    .ifPresent(pro -> {
                        detail.put("ProductId", pro.getProductId());
                        detail.put("ProductName", pro.getProductName());
                        detail.put("ShortDescription", pro.getShortDescription() != null ? pro.getShortDescription() : "");
                        detail.put("mobileImage", pro.getProductMobileImage() != null ? pro.getProductMobileImage() : "");
                        detail.put("mobileVideo", pro.getProductMobileVideo() != null ? pro.getProductMobileVideo() : "");
                        detail.put("webImage", pro.getProductImage() != null ? pro.getProductImage() : "");
                        detail.put("webVideo", pro.getProductWebVideo() != null ? pro.getProductWebVideo() : "");
                        detail.put("webThumbnail", pro.getWebThumbnail() != null ? pro.getWebThumbnail() : "");
                        detail.put("mobileThumbnail", pro.getMobileThumbnail() != null ? pro.getMobileThumbnail() : "");
                        detail.put("ProductParentCategoryId", pro.getProductParentCateoryId());
                        detail.put("ProductCategoryId", pro.getProductCategoryId());
                        detail.put("ProductSubCategoryId", pro.getProductSubCategoryId());
                        detail.put("ProductDescription", pro.getProductDescription());
                        detail.put("SpecialInstruction", pro.getSpecialInstruction());
                        detail.put("IsDiscounted", pro.getIsDiscounted());
                        detail.put("ShipmentType", pro.getShipmentType());
                        detail.put("UnitPrice", pro.getUnitPrice());
                        detail.put("actualPrice", pro.getUnitPrice());
                        if (pro.getIsDiscounted().equals(1)) {
                            detail.put("DiscountType", pro.getDiscountType());

                            Double discountAmount = 0D;
                            if (pro.getDiscountType().equals("FIXED")) {
                                detail.put("DiscountValue", pro.getDiscountValue());
                                detail.put("DiscountAmount", pro.getDiscountValue());
                                detail.put("discountDescription", pro.getDiscountValue() + " " + pro.getCurrency());
                            } else {
                                discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                                detail.put("DiscountValue", pro.getDiscountValue());
                                detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                detail.put("discountDescription", pro.getDiscountValue() + "% Discount");
                            }
                            detail.put("offerPrice", pro.getUnitPrice().doubleValue() - discountAmount);
                        } else {
                            detail.put("DiscountType", "NotDiscounted");
                            detail.put("DiscountValue", new BigDecimal(0));
                            detail.put("DiscountAmount", new BigDecimal(0));
                            detail.put("offerPrice", pro.getUnitPrice());
                            detail.put("discountDescription", pro.getDiscountValue() + " " + pro.getCurrency());
                        }
                        ArrayList<String> images = new ArrayList<String>();
                        productImageRepository.findProductImagesByProductId(pro.getProductId()).forEach(
                                image -> {
                                    images.add(image.getFilePath());
                                }
                        );
                        detail.put("discountExpiry", pro.getDiscountExpiryDate());
                        detail.put("currency", pro.getCurrency());
                        detail.put("productRating", 4.2);
                        detail.put("ratingCount", 30);
                        detail.put("ProductImages", images);

                        List<Product> products = productRepository.findProductsByProductParentCateoryIdAndProductCategoryIdAndProductSubCategoryId(
                                pro.getProductParentCateoryId(), pro.getProductCategoryId(), pro.getProductSubCategoryId()
                        );

                        List<JSONObject> more = new ArrayList<>();

                        productMoreRepository.findAll()
                                .forEach(productMore -> {
                                    JSONObject item = new JSONObject();
                                    item.put("template", productMore.getTemplate());
                                    item.put("catalogueType", productMore.getCatalogueType());
                                    item.put("display_name", productMore.getDisplayName());
                                    item.put("key", productMore.getMoreKey());

                                    if (productMore.getPickType().equals("R")) {
                                        List<JSONObject> items = new ArrayList<>();
                                        if (productMore.getCatalogueType().equals("Brand")) {
                                            //items = productCategoryService.getRandomProductsByParentCategory(pro.getManufucturer());
                                        } else if (productMore.getCatalogueType().equals("Product")) {
                                        } else if (productMore.getCatalogueType().equals("ParentCategory")) {
                                            items = productCategoryService.getRandomParentCategories(productMore.getReturnNumber());
                                        } else if (productMore.getCatalogueType().equals("Category")) {
                                            items = productCategoryService.getRandomCategoriesByParentId(pro.getProductParentCateoryId(), productMore.getReturnNumber());
                                        } else if (productMore.getCatalogueType().equals("SubCategory")) {
                                            items = productCategoryService.getRandomSubCategoriesByCategoryId(pro.getProductCategoryId(), productMore.getReturnNumber());
                                        }
                                        item.put("items", items);
                                    } else {
                                        List<JSONObject> finalItems = new ArrayList<>();
                                        productMoreTemplateRepository.findProductMoreTemplateByProductMoreId(productMore.getId())
                                                .forEach(template -> {
                                                    if (template.getType().equals("Brand")) {
                                                        JSONObject response = categoryService.getBrandInfo(Long.valueOf(template.getTypeId()));
                                                        finalItems.add(response);
                                                    } else if (template.getType().equals("Product")) {
                                                        JSONObject response = getProductDetail(Long.valueOf(template.getId()));
                                                        finalItems.add(response);
                                                    } else if (template.getType().equals("ParentCategory")) {
                                                        JSONObject response = categoryService.getParentCatInfo(Long.valueOf(template.getId()));
                                                        finalItems.add(response);
                                                    } else if (template.getType().equals("Category")) {
                                                        JSONObject response = categoryService.getCategoryInfo(Long.valueOf(template.getId()));
                                                        finalItems.add(response);
                                                    } else if (template.getType().equals("SubCategory")) {
                                                        JSONObject response = categoryService.getSubCategoryInfo(Long.valueOf(template.getId()));
                                                        finalItems.add(response);
                                                    }
                                                });
                                        item.put("items", finalItems);
                                    }

                                    more.add(item);
                                });
                        detail.put("more", more);

                        // Product Reviews
                        List<JSONObject> reviews = new ArrayList<>();
                        for (int i = 1; i < 6; i++) {
                            JSONObject productReview = new JSONObject();
                            productReview.put("id", i);
                            productReview.put("title", "Great Product!");
                            productReview.put("description", "Purchased this product for my home office. Keyboard is smaller than expected. Mouse is average size. DOES NOT COME WITH AA BATTERIES, so be prepared with your own supply or buy some in addition to this purchase! Product is very shiny and sleek and functional for a home office workspace.");
                            productReview.put("rating", 4);
                            productReview.put("reviewerProfileImageUrl", "https://dwjzmw55yd4uj.cloudfront.net/Web/Images/product_imgs_1631562373357_912.jpg");
                            productReview.put("reviewerName", "Arlene McCoy");
                            productReview.put("date", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT).toString());
                            reviews.add(productReview);
                        }
                        detail.put("reviews", reviews);
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return detail;
    }

    public JSONObject getProductLimitedDetails(Long product) {
        JSONObject detail = new JSONObject();
        try {
            productRepository.findProductByProductId(product)
                    .ifPresent(pro -> {
                        detail.put("ProductId", pro.getProductId());
                        detail.put("ProductName", pro.getProductName());
                        detail.put("ShortDescription", pro.getShortDescription() != null ? pro.getShortDescription() : "");
                        detail.put("mobileImage", pro.getProductMobileImage() != null ? pro.getProductMobileImage() : "");
                        detail.put("mobileVideo", pro.getProductMobileVideo() != null ? pro.getProductMobileVideo() : "");
                        detail.put("webImage", pro.getProductImage() != null ? pro.getProductImage() : "");
                        detail.put("webVideo", pro.getProductWebVideo() != null ? pro.getProductWebVideo() : "");
                        detail.put("webThumbnail", pro.getWebThumbnail() != null ? pro.getWebThumbnail() : "");
                        detail.put("mobileThumbnail", pro.getMobileThumbnail() != null ? pro.getMobileThumbnail() : "");
                        detail.put("ProductParentCategoryId", pro.getProductParentCateoryId());
                        detail.put("ProductCategoryId", pro.getProductCategoryId());
                        detail.put("ProductSubCategoryId", pro.getProductSubCategoryId());
                        detail.put("ProductDescription", pro.getProductDescription());
                        detail.put("SpecialInstruction", pro.getSpecialInstruction());
                        detail.put("IsDiscounted", pro.getIsDiscounted());
                        detail.put("ShipmentType", pro.getShipmentType());
                        detail.put("UnitPrice", pro.getUnitPrice());
                        detail.put("actualPrice", pro.getUnitPrice());
                        if (pro.getIsDiscounted().equals(1)) {
                            detail.put("DiscountType", pro.getDiscountType());

                            Double discountAmount = 0D;
                            if (pro.getDiscountType().equals("FIXED")) {
                                detail.put("DiscountValue", pro.getDiscountValue());
                                detail.put("DiscountAmount", pro.getDiscountValue());
                                detail.put("discountDescription", pro.getDiscountValue() + " " + pro.getCurrency());
                            } else {
                                discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                                detail.put("DiscountValue", pro.getDiscountValue());
                                detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                detail.put("discountDescription", pro.getDiscountValue() + "% Discount");
                            }
                            detail.put("offerPrice", pro.getUnitPrice().doubleValue() - discountAmount);
                        } else {
                            detail.put("DiscountType", "NotDiscounted");
                            detail.put("DiscountValue", new BigDecimal(0));
                            detail.put("DiscountAmount", new BigDecimal(0));
                            detail.put("offerPrice", pro.getUnitPrice());
                            detail.put("discountDescription", pro.getDiscountValue() + " " + pro.getCurrency());
                        }
                        ArrayList<String> images = new ArrayList<String>();
                        productImageRepository.findProductImagesByProductId(pro.getProductId()).forEach(
                                image -> {
                                    images.add(image.getFilePath());
                                }
                        );
                        detail.put("discountExpiry", pro.getDiscountExpiryDate());
                        detail.put("currency", pro.getCurrency());
                        detail.put("productRating", 4.2);
                        detail.put("ratingCount", 30);
                        detail.put("ProductImages", images);
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return detail;
    }

}
