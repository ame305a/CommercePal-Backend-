package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.repo.product.BrandImageRepository;
import com.commerce.pal.backend.repo.product.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.ProductParentCategoryRepository;
import com.commerce.pal.backend.repo.product.ProductSubCategoryRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
@SuppressWarnings("Duplicates")
public class CategoryService {
    private final BrandImageRepository brandImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final ProductParentCategoryRepository productParentCategoryRepository;

    @Autowired
    public CategoryService(BrandImageRepository brandImageRepository,
                           ProductCategoryRepository productCategoryRepository,
                           ProductSubCategoryRepository productSubCategoryRepository,
                           ProductParentCategoryRepository productParentCategoryRepository) {
        this.brandImageRepository = brandImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.productParentCategoryRepository = productParentCategoryRepository;
    }

    public JSONObject getBrandInfo(Long id) {
        JSONObject detail = new JSONObject();
        brandImageRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("name", data.getBrand());
                    detail.put("mobileImage", data.getMobileImage());
                    detail.put("webImage", data.getWebImage());
                    detail.put("webThumbnail", data.getWebThumbnail());
                    detail.put("mobileThumbnail", data.getMobileThumbnail());
                });
        return detail;
    }

    public JSONObject getParentCatInfo(Long id) {
        JSONObject detail = new JSONObject();
        productParentCategoryRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("name", data.getParentCategoryName());
                    detail.put("mobileImage", data.getMobileImage());
                    detail.put("webImage", data.getWebImage());
                    detail.put("webThumbnail", data.getWebThumbnail());
                    detail.put("mobileThumbnail", data.getMobileThumbnail());
                    detail.put("unique_name", data.getParentCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                });
        return detail;
    }

    public JSONObject getCategoryInfo(Long id) {
        JSONObject detail = new JSONObject();
        productCategoryRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("parentCategoryId", data.getParentCategoryId());
                    detail.put("name", data.getCategoryName());
                    detail.put("mobileImage", data.getCategoryMobileImage());
                    detail.put("webImage", data.getCategoryWebImage());
                    detail.put("webThumbnail", data.getWebThumbnail());
                    detail.put("mobileThumbnail", data.getMobileThumbnail());
                    detail.put("unique_name", data.getCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                });
        return detail;
    }

    public JSONObject getSubCategoryInfo(Long id) {
        JSONObject detail = new JSONObject();
        productSubCategoryRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("name", data.getSubCategoryName());
                    detail.put("categoryId", data.getProductCategoryId());
                    detail.put("mobileImage", "" + data.getMobileImage());
                    detail.put("webImage", data.getWebImage());
                    detail.put("unique_name", data.getSubCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                    detail.put("webThumbnail", data.getWebThumbnail());
                    detail.put("mobileThumbnail", data.getMobileThumbnail());
                });
        return detail;
    }
}
