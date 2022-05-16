package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.repo.product.*;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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


}
