package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleStatus;
import com.commerce.pal.backend.models.user.Merchant;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.repo.product.*;
import com.commerce.pal.backend.repo.product.flashSale.FlashSaleProductRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;


@Log
@Service
@SuppressWarnings("Duplicates")
public class ProductService {

    private final GlobalMethods globalMethods;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final SubProductService subProductService;
    private final SubProductRepository subProductRepository;
    private final ProductMoreRepository productMoreRepository;
    private final ProductDatabaseService productDatabaseService;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryService productCategoryService;
    private final ProductMoreTemplateRepository productMoreTemplateRepository;
    private final MerchantRepository merchantRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;

    @Autowired
    public ProductService(GlobalMethods globalMethods,
                          CategoryService categoryService,
                          ProductRepository productRepository,
                          SubProductService subProductService,
                          SubProductRepository subProductRepository,
                          ProductMoreRepository productMoreRepository,
                          ProductDatabaseService productDatabaseService,
                          ProductImageRepository productImageRepository,
                          ProductCategoryService productCategoryService,
                          ProductMoreTemplateRepository productMoreTemplateRepository, MerchantRepository merchantRepository, FlashSaleProductRepository flashSaleProductRepository) {
        this.globalMethods = globalMethods;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.subProductService = subProductService;
        this.subProductRepository = subProductRepository;
        this.productMoreRepository = productMoreRepository;
        this.productDatabaseService = productDatabaseService;
        this.productImageRepository = productImageRepository;
        this.productCategoryService = productCategoryService;
        this.productMoreTemplateRepository = productMoreTemplateRepository;
        this.merchantRepository = merchantRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
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
                        product.setProductType(reqBody.has("productType") ? reqBody.getString("productType") : product.getProductType());
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
                        reqBody.put("ProductId", String.valueOf(product.getProductId()));
                        reqBody.put("subProductId", String.valueOf(product.getPrimarySubProduct()));
                        subProductService.updateSubProduct(reqBody);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The Product does not exist")
                                .put("statusMessage", "The Product does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
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
                        detail.put("unique_id", globalMethods.generateUniqueString(pro.getProductId().toString()));
                        detail.put("ProductId", pro.getProductId());
                        detail.put("status", pro.getStatus());
                        detail.put("ownerType", pro.getOwnerType());
                        detail.put("merchantId", pro.getMerchantId());
                        detail.put("productName", pro.getProductName());
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

                        detail.put("ProductParentCategoryIdName", categoryService.getParentCatInfo(pro.getProductParentCateoryId()).getString("name"));
                        detail.put("ProductCategoryIdName", categoryService.getCategoryInfo(pro.getProductCategoryId()).getString("name"));
                        detail.put("ProductSubCategoryIdName", categoryService.getSubCategoryInfo(pro.getProductSubCategoryId()).getString("name"));

                        detail.put("ProductDescription", pro.getProductDescription());
                        detail.put("SpecialInstruction", pro.getSpecialInstruction());
                        detail.put("IsDiscounted", pro.getIsDiscounted());
                        detail.put("manufacturer", Integer.valueOf(pro.getManufucturer()));
                        detail.put("manufacturer", pro.getManufucturer());
                        detail.put("ShipmentType", pro.getShipmentType());
                        detail.put("unitOfMeasure", pro.getUnitOfMeasure());
                        detail.put("discountType", pro.getDiscountType());
                        detail.put("UnitPrice", pro.getUnitPrice());
                        detail.put("quantity", pro.getQuantity());
                        detail.put("productType", pro.getProductType());
                        detail.put("actualPrice", pro.getUnitPrice());
                        detail.put("maxOrder", pro.getMaxOrder() != null ? pro.getMaxOrder() : "0");
                        detail.put("minOrder", pro.getMinOrder() != null ? pro.getMinOrder() : "0");
                        detail.put("moq_value", pro.getMinOrder());
                        detail.put("PrimarySubProduct", pro.getPrimarySubProduct());

                        if (pro.getIsProductOnFlashSale() == 1) {
                            Optional<FlashSaleProduct> optionalFlashSaleProduct = flashSaleProductRepository
                                    .findBySubProductProductIdAndStatus(pro.getProductId(), FlashSaleStatus.ACTIVE);
                            if (optionalFlashSaleProduct.isPresent()) {
                                FlashSaleProduct flashSaleProduct = optionalFlashSaleProduct.get();
                                JSONObject flashSale = new JSONObject();
                                flashSale.put("flashSalePrice", flashSaleProduct.getFlashSalePrice())
                                        .put("flashSaleStartDate", flashSaleProduct.getFlashSaleStartDate())
                                        .put("flashSaleEndDate", flashSaleProduct.getFlashSaleEndDate())
                                        .put("flashSaleInventoryQuantity", flashSaleProduct.getFlashSaleInventoryQuantity())
                                        .put("flashSaleTotalQuantitySold", flashSaleProduct.getFlashSaleTotalQuantitySold())
                                        .put("isQuantityRestrictedPerCustomer", flashSaleProduct.getIsQuantityRestrictedPerCustomer())
                                        .put("flashSaleMinQuantityPerCustomer", flashSaleProduct.getFlashSaleMinQuantityPerCustomer())
                                        .put("flashSaleMaxQuantityPerCustomer", flashSaleProduct.getFlashSaleMaxQuantityPerCustomer());

                                detail.put("flashSale", flashSale);
                            }
                        }

                        List<JSONObject> subProducts = subProductService.getSubByProduct(pro.getProductId());
                        detail.put("subProducts", subProducts);
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

                        subProductRepository.findSubProductsByProductIdAndSubProductId(
                                        pro.getProductId(), pro.getPrimarySubProduct())
                                .ifPresent(subProduct -> {
                                    Double discountAmount = 0D;
                                    if (subProduct.getIsDiscounted().equals(1)) {
                                        detail.put("DiscountType", subProduct.getDiscountType());
                                        if (pro.getDiscountType().equals("FIXED")) {
                                            detail.put("DiscountValue", subProduct.getDiscountValue());
                                            detail.put("DiscountAmount", subProduct.getDiscountValue());
                                            detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                                        } else {
                                            discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                                            detail.put("DiscountValue", subProduct.getDiscountValue());
                                            detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                            detail.put("discountDescription", subProduct.getDiscountValue() + "% Discount");
                                        }
                                        detail.put("offerPrice", subProduct.getUnitPrice().doubleValue() - discountAmount);
                                    } else {
                                        detail.put("DiscountType", "NotDiscounted");
                                        detail.put("DiscountValue", new BigDecimal(0));
                                        detail.put("DiscountAmount", new BigDecimal(0));
                                        detail.put("offerPrice", subProduct.getUnitPrice());
                                        detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                                    }
                                    BigDecimal subProductPrice = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount);
                                    JSONObject chargeBdy = productDatabaseService.calculateProductPrice(subProductPrice);
                                    detail.put("offerPrice", chargeBdy.getBigDecimal("FinalPrice").doubleValue());
                                });

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
                        List<JSONObject> featureDetails = new ArrayList<>();
                        List<Long> subProductId = subProductService.getSubProductByProductId(pro.getProductId());
                        subProductService.getGroupProductFeature(subProductId)
                                .forEach(featureId -> {
                                    JSONObject featureIdValues = new JSONObject();
                                    featureIdValues.put("FeatureId", featureId);
                                    featureIdValues.put("Available", subProductService.getGroupProductFeature(featureId));
                                    featureDetails.add(featureIdValues);
                                });
                        detail.put("featureDetails", featureDetails);
                        // Product Reviews
                        List<JSONObject> reviews = new ArrayList<>();
                        for (int i = 1; i < 3; i++) {
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


    public JSONObject getProductDetail(Product pro) {
        JSONObject detail = new JSONObject();
        detail.put("unique_id", globalMethods.generateUniqueString(pro.getProductId().toString()));
        detail.put("ProductId", pro.getProductId());
        detail.put("status", pro.getStatus());
        detail.put("ownerType", pro.getOwnerType());
        detail.put("merchantId", pro.getMerchantId());
        detail.put("productName", pro.getProductName());
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

        detail.put("ProductParentCategoryIdName", categoryService.getParentCatInfo(pro.getProductParentCateoryId()).getString("name"));
        detail.put("ProductCategoryIdName", categoryService.getCategoryInfo(pro.getProductCategoryId()).getString("name"));
        detail.put("ProductSubCategoryIdName", categoryService.getSubCategoryInfo(pro.getProductSubCategoryId()).getString("name"));

        detail.put("ProductDescription", pro.getProductDescription());
        detail.put("SpecialInstruction", pro.getSpecialInstruction());
        detail.put("IsDiscounted", pro.getIsDiscounted());
        detail.put("manufacturer", Integer.valueOf(pro.getManufucturer()));
        detail.put("manufacturer", pro.getManufucturer());
        detail.put("ShipmentType", pro.getShipmentType());
        detail.put("unitOfMeasure", pro.getUnitOfMeasure());
        detail.put("discountType", pro.getDiscountType());
        detail.put("UnitPrice", pro.getUnitPrice());
        detail.put("quantity", pro.getQuantity());
        detail.put("productType", pro.getProductType());
        detail.put("actualPrice", pro.getUnitPrice());
        detail.put("maxOrder", pro.getMaxOrder() != null ? pro.getMaxOrder() : "0");
        detail.put("minOrder", pro.getMinOrder() != null ? pro.getMinOrder() : "0");
        detail.put("moq_value", pro.getMinOrder());
        detail.put("PrimarySubProduct", pro.getPrimarySubProduct());

        if (pro.getIsProductOnFlashSale() == 1) {
            Optional<FlashSaleProduct> optionalFlashSaleProduct = flashSaleProductRepository
                    .findBySubProductProductIdAndStatus(pro.getProductId(), FlashSaleStatus.ACTIVE);
            if (optionalFlashSaleProduct.isPresent()) {
                FlashSaleProduct flashSaleProduct = optionalFlashSaleProduct.get();
                JSONObject flashSale = new JSONObject();
                flashSale.put("flashSalePrice", flashSaleProduct.getFlashSalePrice())
                        .put("flashSaleStartDate", flashSaleProduct.getFlashSaleStartDate())
                        .put("flashSaleEndDate", flashSaleProduct.getFlashSaleEndDate())
                        .put("flashSaleInventoryQuantity", flashSaleProduct.getFlashSaleInventoryQuantity())
                        .put("flashSaleTotalQuantitySold", flashSaleProduct.getFlashSaleTotalQuantitySold())
                        .put("isQuantityRestrictedPerCustomer", flashSaleProduct.getIsQuantityRestrictedPerCustomer())
                        .put("flashSaleMinQuantityPerCustomer", flashSaleProduct.getFlashSaleMinQuantityPerCustomer())
                        .put("flashSaleMaxQuantityPerCustomer", flashSaleProduct.getFlashSaleMaxQuantityPerCustomer());

                detail.put("flashSale", flashSale);
            }
        }

        List<JSONObject> subProducts = subProductService.getSubByProduct(pro.getProductId());
        detail.put("subProducts", subProducts);
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

        subProductRepository.findSubProductsByProductIdAndSubProductId(
                        pro.getProductId(), pro.getPrimarySubProduct())
                .ifPresent(subProduct -> {
                    Double discountAmount = 0D;
                    if (subProduct.getIsDiscounted().equals(1)) {
                        detail.put("DiscountType", subProduct.getDiscountType());
                        if (pro.getDiscountType().equals("FIXED")) {
                            detail.put("DiscountValue", subProduct.getDiscountValue());
                            detail.put("DiscountAmount", subProduct.getDiscountValue());
                            detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                        } else {
                            discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                            detail.put("DiscountValue", subProduct.getDiscountValue());
                            detail.put("DiscountAmount", new BigDecimal(discountAmount));
                            detail.put("discountDescription", subProduct.getDiscountValue() + "% Discount");
                        }
                        detail.put("offerPrice", subProduct.getUnitPrice().doubleValue() - discountAmount);
                    } else {
                        detail.put("DiscountType", "NotDiscounted");
                        detail.put("DiscountValue", new BigDecimal(0));
                        detail.put("DiscountAmount", new BigDecimal(0));
                        detail.put("offerPrice", subProduct.getUnitPrice());
                        detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                    }
                    BigDecimal subProductPrice = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount);
                    JSONObject chargeBdy = productDatabaseService.calculateProductPrice(subProductPrice);
                    detail.put("offerPrice", chargeBdy.getBigDecimal("FinalPrice").doubleValue());
                });

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
        List<JSONObject> featureDetails = new ArrayList<>();
        List<Long> subProductId = subProductService.getSubProductByProductId(pro.getProductId());
        subProductService.getGroupProductFeature(subProductId)
                .forEach(featureId -> {
                    JSONObject featureIdValues = new JSONObject();
                    featureIdValues.put("FeatureId", featureId);
                    featureIdValues.put("Available", subProductService.getGroupProductFeature(featureId));
                    featureDetails.add(featureIdValues);
                });
        detail.put("featureDetails", featureDetails);
        // Product Reviews
        List<JSONObject> reviews = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
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

        return detail;
    }

    public JSONObject getProductListDetailsAlready(Product pro) {
        JSONObject detail = new JSONObject();
        try {
            detail.put("unique_id", globalMethods.generateUniqueString(pro.getProductId().toString()));
            detail.put("ProductId", pro.getProductId());
            detail.put("ownerType", pro.getOwnerType());
            detail.put("merchantId", pro.getMerchantId());
            detail.put("productName", pro.getProductName());
            detail.put("mobileImage", pro.getProductMobileImage() != null ? pro.getProductMobileImage() : "");
            detail.put("webImage", pro.getProductImage() != null ? pro.getProductImage() : "");
            detail.put("webThumbnail", pro.getWebThumbnail() != null ? pro.getWebThumbnail() : "");
            detail.put("mobileThumbnail", pro.getMobileThumbnail() != null ? pro.getMobileThumbnail() : "");
            detail.put("isDiscounted", pro.getIsDiscounted().toString());
            detail.put("unitPrice", pro.getUnitPrice().toString());
            detail.put("manufacturer", pro.getManufucturer());
            detail.put("actualPrice", pro.getUnitPrice().toString());
            detail.put("maxOrder", pro.getMaxOrder().toString());
            detail.put("minOrder", pro.getMinOrder().toString());
            detail.put("primarySubProduct", pro.getPrimarySubProduct());
            detail.put("createdDate", pro.getCreatedDate());
            detail.put("isOnFlashSale", pro.getIsProductOnFlashSale() != null ? pro.getIsProductOnFlashSale() : 0);
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

            detail.put("subProductId", pro.getPrimarySubProduct());
            subProductRepository.findSubProductsByProductIdAndSubProductId(
                            pro.getProductId(), pro.getPrimarySubProduct())
                    .ifPresent(subProduct -> {
                        Double discountAmount = 0D;
                        if (subProduct.getIsDiscounted().equals(1)) {
                            detail.put("DiscountType", subProduct.getDiscountType());
                            if (pro.getDiscountType().equals("FIXED")) {
                                detail.put("DiscountValue", subProduct.getDiscountValue());
                                detail.put("DiscountAmount", subProduct.getDiscountValue());
                                detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                            } else {
                                discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                                detail.put("DiscountValue", subProduct.getDiscountValue());
                                detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                detail.put("discountDescription", subProduct.getDiscountValue() + "% Discount");
                            }
                            detail.put("offerPrice", subProduct.getUnitPrice().doubleValue() - discountAmount);
                        } else {
                            detail.put("DiscountType", "NotDiscounted");
                            detail.put("DiscountValue", new BigDecimal(0));
                            detail.put("DiscountAmount", new BigDecimal(0));
                            detail.put("offerPrice", subProduct.getUnitPrice());
                            detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                        }

                        BigDecimal subProductPrice = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount);
                        JSONObject chargeBdy = productDatabaseService.calculateProductPrice(subProductPrice);
                        detail.put("offerPrice", chargeBdy.getBigDecimal("FinalPrice").doubleValue());
                    });

            detail.put("currency", pro.getCurrency());
            detail.put("productRating", 4.2);
            detail.put("ratingCount", 30);
            detail.put("quantity", pro.getQuantity());
            detail.put("soldQuantity", pro.getSoldQuantity());
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return detail;
    }

    public JSONObject getProductListDetails(Long product) {
        JSONObject detail = new JSONObject();
        try {
            productRepository.findProductByProductId(product)
                    .ifPresent(pro -> {
                        detail.put("unique_id", globalMethods.generateUniqueString(pro.getProductId().toString()));
                        detail.put("ProductId", pro.getProductId());
                        detail.put("ownerType", pro.getOwnerType());
                        detail.put("merchantId", pro.getMerchantId());
                        detail.put("productName", pro.getProductName());
                        detail.put("mobileImage", pro.getProductMobileImage() != null ? pro.getProductMobileImage() : "");
                        detail.put("webImage", pro.getProductImage() != null ? pro.getProductImage() : "");
                        detail.put("webThumbnail", pro.getWebThumbnail() != null ? pro.getWebThumbnail() : "");
                        detail.put("mobileThumbnail", pro.getMobileThumbnail() != null ? pro.getMobileThumbnail() : "");
                        detail.put("isDiscounted", pro.getIsDiscounted().toString());
                        detail.put("unitPrice", pro.getUnitPrice().toString());
                        detail.put("manufacturer", pro.getManufucturer());
                        detail.put("actualPrice", pro.getUnitPrice().toString());
                        detail.put("maxOrder", pro.getMaxOrder().toString());
                        detail.put("minOrder", pro.getMinOrder().toString());
                        detail.put("primarySubProduct", pro.getPrimarySubProduct());
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

                        subProductRepository.findSubProductsByProductIdAndSubProductId(
                                        pro.getProductId(), pro.getPrimarySubProduct())
                                .ifPresent(subProduct -> {
                                    Double discountAmount = 0D;
                                    if (subProduct.getIsDiscounted().equals(1)) {
                                        detail.put("DiscountType", subProduct.getDiscountType());
                                        if (pro.getDiscountType().equals("FIXED")) {
                                            detail.put("DiscountValue", subProduct.getDiscountValue());
                                            detail.put("DiscountAmount", subProduct.getDiscountValue());
                                            detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                                        } else {
                                            discountAmount = pro.getUnitPrice().doubleValue() * pro.getDiscountValue().doubleValue() / 100;
                                            detail.put("DiscountValue", subProduct.getDiscountValue());
                                            detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                            detail.put("discountDescription", subProduct.getDiscountValue() + "% Discount");
                                        }
                                        detail.put("offerPrice", subProduct.getUnitPrice().doubleValue() - discountAmount);
                                    } else {
                                        detail.put("DiscountType", "NotDiscounted");
                                        detail.put("DiscountValue", new BigDecimal(0));
                                        detail.put("DiscountAmount", new BigDecimal(0));
                                        detail.put("offerPrice", subProduct.getUnitPrice());
                                        detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                                    }
                                    BigDecimal subProductPrice = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount);
                                    JSONObject chargeBdy = productDatabaseService.calculateProductPrice(subProductPrice);
                                    detail.put("offerPrice", chargeBdy.getBigDecimal("FinalPrice").doubleValue());
                                });

                        detail.put("currency", pro.getCurrency());
                        detail.put("productRating", 4.2);
                        detail.put("ratingCount", 30);
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
                        detail.put("productName", pro.getProductName());
                        detail.put("ownerType", pro.getOwnerType());
                        detail.put("merchantId", pro.getMerchantId());
                        detail.put("shortDescription", pro.getShortDescription() != null ? pro.getShortDescription() : "");
                        detail.put("mobileImage", pro.getProductMobileImage() != null ? pro.getProductMobileImage() : "");
                        detail.put("mobileVideo", pro.getProductMobileVideo() != null ? pro.getProductMobileVideo() : "");
                        detail.put("productImage", pro.getProductImage() != null ? pro.getProductImage() : "");
                        detail.put("webImage", pro.getProductImage() != null ? pro.getProductImage() : "");
                        detail.put("webVideo", pro.getProductWebVideo() != null ? pro.getProductWebVideo() : "");
                        detail.put("webThumbnail", pro.getWebThumbnail() != null ? pro.getWebThumbnail() : "");
                        detail.put("mobileThumbnail", pro.getMobileThumbnail() != null ? pro.getMobileThumbnail() : "");
                        detail.put("productParentCateoryId", pro.getProductParentCateoryId().toString());
                        detail.put("productCategoryId", pro.getProductCategoryId().toString());
                        detail.put("productSubCategoryId", pro.getProductSubCategoryId().toString());
                        detail.put("productDescription", pro.getProductDescription());
                        detail.put("specialInstruction", pro.getSpecialInstruction());
                        detail.put("isDiscounted", pro.getIsDiscounted().toString());
                        detail.put("ShipmentType", pro.getShipmentType());
                        detail.put("unitPrice", pro.getUnitPrice().toString());
                        detail.put("unitOfMeasure", pro.getUnitOfMeasure());
                        detail.put("countryOfOrigin", pro.getCountryOfOrigin());
                        detail.put("manufucturer", pro.getManufucturer());
                        detail.put("soldQuantity", pro.getSoldQuantity().toString());
                        detail.put("tax", "0.00");
                        detail.put("productType", pro.getProductType());
                        detail.put("actualPrice", pro.getUnitPrice().toString());
                        detail.put("maxOrder", pro.getMaxOrder().toString());
                        detail.put("minOrder", pro.getMinOrder().toString());
                        detail.put("moq_value", pro.getMinOrder().toString());
                        detail.put("quantity", pro.getQuantity().toString());
                        detail.put("primarySubProduct", pro.getPrimarySubProduct());

                        detail.put("discountType", pro.getDiscountType());
                        detail.put("discountValue", pro.getDiscountValue().toString());
                        detail.put("DiscountAmount", new BigDecimal(0).toString());
                        detail.put("offerPrice", pro.getUnitPrice());
                        detail.put("discountDescription", pro.getDiscountValue() + " " + pro.getCurrency());


                        subProductRepository.findSubProductsByProductIdAndSubProductId(
                                        pro.getProductId(), pro.getPrimarySubProduct())
                                .ifPresent(subProduct -> {
                                    Double discountAmount = 0D;
                                    if (subProduct.getIsDiscounted().equals(1)) {
                                        detail.put("DiscountType", subProduct.getDiscountType());
                                        if (subProduct.getDiscountType().equals("FIXED")) {
                                            detail.put("DiscountValue", subProduct.getDiscountValue());
                                            detail.put("DiscountAmount", subProduct.getDiscountValue());
                                            detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                                        } else {
                                            discountAmount = subProduct.getUnitPrice().doubleValue() * subProduct.getDiscountValue().doubleValue() / 100;
                                            detail.put("DiscountValue", subProduct.getDiscountValue());
                                            detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                            detail.put("discountDescription", subProduct.getDiscountValue() + "% Discount");
                                        }
                                        detail.put("offerPrice", subProduct.getUnitPrice().doubleValue() - discountAmount);
                                    } else {
                                        detail.put("DiscountType", "NotDiscounted");
                                        detail.put("DiscountValue", new BigDecimal(0));
                                        detail.put("DiscountAmount", new BigDecimal(0));
                                        detail.put("offerPrice", subProduct.getUnitPrice());
                                        detail.put("discountDescription", subProduct.getDiscountValue() + " " + pro.getCurrency());
                                    }
                                    BigDecimal subProductPrice = new BigDecimal(subProduct.getUnitPrice().doubleValue() - discountAmount);
                                    JSONObject chargeBdy = productDatabaseService.calculateProductPrice(subProductPrice);
                                    detail.put("offerPrice", chargeBdy.getBigDecimal("FinalPrice").doubleValue());
                                });

                        // detail.put("discountExpiry", pro.getDiscountExpiryDate().toString());
                        detail.put("currency", pro.getCurrency());
                        detail.put("productRating", 4.2);
                        detail.put("ratingCount", 30);
                        detail.put("createdBy", "username");

                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return detail;
    }

    public JSONObject getSubProductInfo(Long product, Long subProduct) {
        JSONObject detail = new JSONObject();
        try {
            productRepository.findProductByProductId(product)
                    .ifPresent(pro -> {
                        detail.put("ProductId", pro.getProductId());
                        detail.put("productName", pro.getProductName());
                        detail.put("ShortDescription", pro.getShortDescription() != null ? pro.getShortDescription() : "");
                        detail.put("mobileImage", pro.getProductMobileImage() != null ? pro.getProductMobileImage() : "");
                        detail.put("webImage", pro.getProductImage() != null ? pro.getProductImage() : "");
                        detail.put("webThumbnail", pro.getWebThumbnail() != null ? pro.getWebThumbnail() : "");
                        detail.put("mobileThumbnail", pro.getMobileThumbnail() != null ? pro.getMobileThumbnail() : "");
                        detail.put("ProductParentCategoryId", pro.getProductParentCateoryId());
                        detail.put("ProductDescription", pro.getProductDescription());
                        detail.put("subProductInfo", subProductService.getSubProductInfo(subProduct, pro.getCurrency()));
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return detail;
    }

    public List<JSONObject> getRandomProductsUnder1000() {
        List<Product> products = productRepository.findRandomProductsUnderPrice(BigDecimal.valueOf(1000), 24);

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = getProductListDetailsAlready(pro);
            details.add(detail);
        });

        return details;
    }

    public List<JSONObject> getRandomProductsAbove1000() {
        List<Product> products = productRepository.findRandomProductsAbovePrice(BigDecimal.valueOf(1000), 24);

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = getProductListDetailsAlready(pro);
            details.add(detail);
        });

        return details;
    }

    //Retrieves a paginated list of products with support for sorting, filtering, searching, and date range.
    public JSONObject getAllProducts(int page, int size, Sort sort, Long category, Long parentCategory, String productType, Integer status, Long merchantId, Integer city, String searchKeyword, Timestamp startDate, Timestamp endDate) {


        List<Long> cityMerchantIds = new ArrayList<>();
        if (city != null) {
            merchantRepository.findMerchantByCity(city)
                    .forEach(merchant -> cityMerchantIds.add(merchant.getMerchantId())
                    );
        }

        // Create a PageRequest with page number, size, and sorting
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository
                .findByFilterAndMerchantAndDateAndStatus(category, parentCategory, productType, searchKeyword, startDate, endDate, status, merchantId, city, cityMerchantIds, pageable);

        List<JSONObject> products = new ArrayList<>();
        productPage.getContent()
                .forEach(pro -> {
                    JSONObject detail = new JSONObject();

                    String merchantName = "";
                    Optional<Merchant> optionalMerchant = merchantRepository.findMerchantByMerchantId(pro.getMerchantId());
                    if (optionalMerchant.isPresent()) {
                        Merchant merchant = optionalMerchant.get();
                        merchantName = merchant.getMerchantName();
                    }

                    String categoryName = categoryService.getCategoryName(pro.getProductCategoryId());
                    detail.put("productId", pro.getProductId());
                    detail.put("productName", pro.getProductName());
                    detail.put("ownerType", pro.getOwnerType());
                    detail.put("merchantName", merchantName);
                    detail.put("shortDescription", pro.getShortDescription() != null ? pro.getShortDescription() : "");
                    detail.put("productType", pro.getProductType());
                    detail.put("status", pro.getStatus());
                    detail.put("unitPrice", pro.getUnitPrice());
                    detail.put("quantity", pro.getQuantity());
                    detail.put("unitPrice", pro.getUnitPrice());
                    detail.put("unitOfMeasure", pro.getUnitOfMeasure());
                    detail.put("createdBy", pro.getCreatedBy());
                    detail.put("createdDate", pro.getCreatedDate());
                    detail.put("verifiedBy", pro.getVerifiedBy());
                    detail.put("productCategory", categoryName);

                    products.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", productPage.getNumber())
                .put("pageSize", productPage.getSize())
                .put("totalElements", productPage.getTotalElements())
                .put("totalPages", productPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("products", products).put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed")
                .put("data", data);

        return response;
    }

//    //Retrieves a paginated list of products with support for sorting, filtering, searching, and date range.
//    public JSONObject getAllProducts1(Pageable pageable, Long filterByCategory, Integer status, Long merchantId, Integer city, Timestamp startDate, Timestamp endDate) {
//
//        List<Long> cityMerchantIds = new ArrayList<>();
//        if (city != null) {
//            merchantRepository.findMerchantByCity(city)
//                    .forEach(merchant -> cityMerchantIds.add(merchant.getMerchantId())
//                    );
//        }
//
//        Page<Product> productPage = productRepository
//                .findByFilterAndMerchantAndDateAndStatus(filterByCategory, searchKeyword, startDate, endDate, status, merchantId, city, cityMerchantIds, pageable);
//
//        List<JSONObject> products = new ArrayList<>();
//        productPage.getContent()
//                .forEach(pro -> {
//                    JSONObject detail = new JSONObject();
//
//                    String merchantName = "";
//                    Optional<Merchant> optionalMerchant = merchantRepository.findMerchantByMerchantId(pro.getMerchantId());
//                    if (optionalMerchant.isPresent()) {
//                        Merchant merchant = optionalMerchant.get();
//                        merchantName = merchant.getMerchantName();
//                    }
//
//                    String categoryName = categoryService.getCategoryName(pro.getProductCategoryId());
//                    detail.put("productName", pro.getProductName());
//                    detail.put("ownerType", pro.getOwnerType());
//                    detail.put("merchantName", merchantName);
//                    detail.put("shortDescription", pro.getShortDescription() != null ? pro.getShortDescription() : "");
//                    detail.put("productType", pro.getProductType());
//                    detail.put("status", pro.getStatus());
//                    detail.put("unitPrice", pro.getUnitPrice());
//                    detail.put("quantity", pro.getQuantity());
//                    detail.put("unitPrice", pro.getUnitPrice());
//                    detail.put("unitOfMeasure", pro.getUnitOfMeasure());
//                    detail.put("createdBy", pro.getCreatedBy());
//                    detail.put("createdDate", pro.getCreatedDate());
//                    detail.put("verifiedBy", pro.getVerifiedBy());
//                    detail.put("productCategory", categoryName);
//
//                    products.add(detail);
//                });
//
//        JSONObject paginationInfo = new JSONObject();
//        paginationInfo.put("pageNumber", productPage.getNumber())
//                .put("pageSize", productPage.getSize())
//                .put("totalElements", productPage.getTotalElements())
//                .put("totalPages", productPage.getTotalPages());
//
//        JSONObject data = new JSONObject();
//        data.put("products", products).put("paginationInfo", paginationInfo);
//
//        JSONObject response = new JSONObject();
//        response.put("statusCode", ResponseCodes.SUCCESS)
//                .put("statusDescription", "Product Passed")
//                .put("statusMessage", "Product Passed")
//                .put("data", data);
//
//        return response;
//    }


}
