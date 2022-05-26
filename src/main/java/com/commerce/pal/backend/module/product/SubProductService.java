package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.order.Order;
import com.commerce.pal.backend.models.product.ProductFeatureValue;
import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.repo.product.*;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class SubProductService {

    private final SubProductRepository subProductRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final SubProductImageRepository subProductImageRepository;
    private final ProductFeatureValueRepository productFeatureValueRepository;

    @Autowired
    public SubProductService(SubProductRepository subProductRepository,
                             ProductFeatureRepository productFeatureRepository,
                             SubProductImageRepository subProductImageRepository,
                             ProductFeatureValueRepository productFeatureValueRepository) {
        this.subProductRepository = subProductRepository;
        this.productFeatureRepository = productFeatureRepository;
        this.subProductImageRepository = subProductImageRepository;
        this.productFeatureValueRepository = productFeatureValueRepository;
    }

    public JSONObject getSubProductInfo(Long subProduct) {
        JSONObject detail = new JSONObject();
        try {
            subProductRepository.findById(subProduct)
                    .ifPresent(sub -> {
                        detail.put("SubProductId", sub.getSubProductId());
                        detail.put("ShortDescription", sub.getShortDescription() != null ? sub.getShortDescription() : "");
                        detail.put("mobileImage", sub.getProductMobileImage() != null ? sub.getProductMobileImage() : "");
                        detail.put("mobileVideo", sub.getProductMobileVideo() != null ? sub.getProductMobileVideo() : "");
                        detail.put("webImage", sub.getProductImage() != null ? sub.getProductImage() : "");
                        detail.put("webVideo", sub.getProductWebVideo() != null ? sub.getProductWebVideo() : "");
                        detail.put("webThumbnail", sub.getWebThumbnail() != null ? sub.getWebThumbnail() : "");
                        detail.put("mobileThumbnail", sub.getMobileThumbnail() != null ? sub.getMobileThumbnail() : "");
                        detail.put("IsDiscounted", sub.getIsDiscounted());
                        detail.put("UnitPrice", sub.getUnitPrice());
                        if (sub.getIsDiscounted().equals(1)) {
                            detail.put("DiscountType", sub.getDiscountType());
                            Double discountAmount = 0D;
                            if (sub.getDiscountType().equals("FIXED")) {
                                detail.put("DiscountValue", sub.getDiscountValue());
                                detail.put("DiscountAmount", sub.getDiscountValue());
                                detail.put("discountDescription", sub.getDiscountValue() + " " + "ETB");
                            } else {
                                discountAmount = sub.getUnitPrice().doubleValue() * sub.getDiscountValue().doubleValue() / 100;
                                detail.put("DiscountValue", sub.getDiscountValue());
                                detail.put("DiscountAmount", new BigDecimal(discountAmount));
                                detail.put("discountDescription", sub.getDiscountValue() + "% Discount");
                            }
                            detail.put("offerPrice", sub.getUnitPrice().doubleValue() - discountAmount);
                        } else {
                            detail.put("DiscountType", "NotDiscounted");
                            detail.put("DiscountValue", new BigDecimal(0));
                            detail.put("DiscountAmount", new BigDecimal(0));
                            detail.put("offerPrice", sub.getUnitPrice());
                            detail.put("discountDescription", sub.getDiscountValue() + " " + "ETB");
                        }
                        List<JSONObject> features = new ArrayList<>();
                        productFeatureValueRepository.findAllByProductId(sub.getSubProductId())
                                .forEach(feaValue -> {
                                    JSONObject featureValue = new JSONObject();
                                    featureValue.put("ValueId", feaValue.getId());
                                    featureValue.put("Value", feaValue.getValue());
                                    featureValue.put("ValueUnitOfMeasure", feaValue.getUnitOfMeasure());
                                    productFeatureRepository.findById(feaValue.getProductFeatureId())
                                            .ifPresent(productFeature -> {
                                                featureValue.put("FeatureId", productFeature.getId());
                                                featureValue.put("FeatureName", productFeature.getFeatureName());
                                                featureValue.put("FeatureVariableType", productFeature.getVariableType());
                                                featureValue.put("FeatureUnitOfMeasure", productFeature.getUnitOfMeasure());
                                            });
                                    features.add(featureValue);
                                });
                        detail.put("features", features);
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return detail;
    }

    public List<JSONObject> getSubByProduct(Long product) {
        List<JSONObject> subProducts = new ArrayList<>();
        subProductRepository.findSubProductsByProductId(product)
                .forEach(subProduct -> {
                    subProducts.add(getSubProductInfo(subProduct.getSubProductId()));
                });
        return subProducts;
    }

    public List<Long> getSubProductByProductId(Long product) {
        List<Long> subProducts = new ArrayList<>();
        subProductRepository.findSubProductsByProductId(product)
                .forEach(subProduct -> {
                    subProducts.add(subProduct.getSubProductId());
                });
        return subProducts;
    }

    public List<Long> getGroupProductFeature(List<Long> subs) {
        List<Long> features = new ArrayList<>();
        try {
            features = productFeatureValueRepository.findProductFeatureValuesByProductId(subs);
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }
        return features;
    }

    public List<String> getGroupProductFeature(Long featureId) {
        List<String> featuresValues = new ArrayList<>();
        try {
            featuresValues = productFeatureValueRepository.findProductFeatureValuesByProductFeatureId(featureId);
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }
        return featuresValues;
    }

    public Integer validateFeature(Long subCategory, JSONArray features) {
        AtomicReference<Integer> validated = new AtomicReference<>(1);
        features.forEach(feature -> {
            JSONObject featureValue = new JSONObject(feature.toString());
            productFeatureRepository.findByIdAndSubCategoryId(featureValue.getLong("FeatureId"), subCategory)
                    .ifPresentOrElse(productFeature -> {
                    }, () -> {
                        validated.set(0);
                    });
        });
        return validated.get();
    }

    public void updateInsertFeatures(Long subProductId, JSONArray features) {
        subProductRepository.findById(subProductId)
                .ifPresent(subProduct -> {
                    features.forEach(feature -> {
                        JSONObject featureValue = new JSONObject(feature.toString());
                        productFeatureValueRepository.findProductFeatureValuesByProductFeatureIdAndProductId(
                                featureValue.getLong("FeatureId"), subProductId
                        ).ifPresentOrElse(productFeatureValue -> {
                            productFeatureValue.setValue(featureValue.getString("FeatureValue"));
                            productFeatureValue.setUnitOfMeasure(featureValue.getString("UnitOfMeasure"));
                            productFeatureValueRepository.save(productFeatureValue);
                        }, () -> {
                            ProductFeatureValue productFeatureValue = new ProductFeatureValue();
                            productFeatureValue.setProductId(subProductId);
                            productFeatureValue.setProductFeatureId(featureValue.getLong("FeatureId"));
                            productFeatureValue.setValue(featureValue.getString("FeatureValue"));
                            productFeatureValue.setUnitOfMeasure(featureValue.getString("UnitOfMeasure"));
                            productFeatureValue.setStatus(1);
                            productFeatureValue.setCreatedDate(Timestamp.from(Instant.now()));
                            productFeatureValueRepository.save(productFeatureValue);
                        });
                    });
                });
    }

    public JSONObject addSubProduct(JSONObject subPayload) {
        JSONObject response = new JSONObject();
        try {
            AtomicReference<SubProduct> subProduct = new AtomicReference<>(new SubProduct());
            subProduct.get().setProductId(subPayload.getLong("ProductId"));
            subProduct.get().setShortDescription(subPayload.getString("shortDescription"));
            subProduct.get().setUnitPrice(new BigDecimal(subPayload.getString("unitPrice")));
            subProduct.get().setIsDiscounted(Integer.valueOf(subPayload.getString("isDiscounted")));
            subProduct.get().setDiscountType(subPayload.getString("discountType"));
            subProduct.get().setDiscountValue(new BigDecimal(subPayload.getString("discountValue")));
            subProduct.get().setIsPromoted(0);
            subProduct.get().setIsPrioritized(0);
            subProduct.get().setStatus(1);
            subProduct.get().setCreatedDate(Timestamp.from(Instant.now()));
            subProduct.get().setCreatedBy(subPayload.getString("createdBy"));
            subProduct.set(subProductRepository.save(subProduct.get()));

            response.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("productId", subPayload.getLong("ProductId"))
                    .put("subProductId", subProduct.get().getSubProductId())
                    .put("statusMessage", "Sub Product successful");

            updateInsertFeatures(subProduct.get().getSubProductId(), subPayload.getJSONArray("productFeature"));
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            response.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
            log.log(Level.WARNING, ex.getMessage());
        }
        return response;
    }

    public JSONObject updateSubProduct(JSONObject subPayload) {
        JSONObject response = new JSONObject();
        try {
            subProductRepository.findSubProductsByProductIdAndSubProductId(subPayload.getLong("ProductId"), subPayload.getLong("subProductId"))
                    .ifPresentOrElse(subProduct -> {
                        subProduct.setShortDescription(subPayload.has("shortDescription") ? subPayload.getString("shortDescription") : subProduct.getShortDescription());
                        subProduct.setUnitPrice(subPayload.has("unitPrice") ? new BigDecimal(subPayload.getString("unitPrice")) : subProduct.getUnitPrice());
                        subProduct.setIsDiscounted(subPayload.has("isDiscounted") ? Integer.valueOf(subPayload.getString("isDiscounted")) : subProduct.getIsDiscounted());
                        subProduct.setDiscountType(subPayload.has("discountType") ? subPayload.getString("discountType") : subProduct.getDiscountType());
                        subProduct.setDiscountValue(subPayload.has("discountValue") ? new BigDecimal(subPayload.getString("discountValue")) : subProduct.getDiscountValue());
                        if (subPayload.has("productFeature")) {
                            updateInsertFeatures(subProduct.getSubProductId(), subPayload.getJSONArray("productFeature"));
                        }
                        subProductRepository.save(subProduct);
                        response.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("productId", subPayload.getLong("ProductId"))
                                .put("subProductId", subProduct.getSubProductId())
                                .put("statusMessage", "Sub Product successful");
                    }, () -> {
                        response.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "The SubProduct does not exist")
                                .put("statusMessage", "The SubProduct does not exist");
                    });
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            response.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return response;
    }

}
