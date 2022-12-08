package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.repo.product.categories.BrandImageRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductParentCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<JSONObject> dynamicCategories() {
        List<JSONObject> parents = new ArrayList<>();
        productParentCategoryRepository.findAll().forEach(parentCat -> {
            JSONObject parent = new JSONObject();
            parent.put("parentCategoryID", parentCat.getId());
            parent.put("parentCategoryName", parentCat.getParentCategoryName());
            parent.put("parentMobileImage", parentCat.getMobileImage() != null ? parentCat.getMobileImage() : "");
            parent.put("parentWebImage", parentCat.getWebImage() != null ? parentCat.getWebImage() : "");
            parent.put("parentWebThumbnail", parentCat.getWebThumbnail() != null ? parentCat.getWebThumbnail() : "");
            List<JSONObject> categories = new ArrayList<>();
            productCategoryRepository.findProductCategoriesByParentCategoryId(parentCat.getId())
                    .forEach(cat -> {
                        JSONObject category = new JSONObject();
                        category.put("categoryID", cat.getId());
                        category.put("categoryName", cat.getCategoryName());
                        List<JSONObject> subCategories = new ArrayList<>();
                        productSubCategoryRepository.findProductSubCategoriesByProductCategoryId(cat.getId())
                                .forEach(subCat -> {
                                    JSONObject subCategory = new JSONObject();
                                    subCategory.put("subCategoryID", subCat.getId());
                                    subCategory.put("subCategoryName", subCat.getSubCategoryName());
                                    subCategories.add(subCategory);
                                });
                        category.put("subCategories", subCategories);
                        categories.add(category);
                    });
            parent.put("categories", categories);
            parents.add(parent);
        });
        return parents;
    }

    public JSONObject getBrandInfo(Long id) {
        JSONObject detail = new JSONObject();
        brandImageRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("name", data.getBrand());
                    detail.put("parentCategoryId", data.getParentCategoryId());
                    detail.put("mobileImage", data.getMobileImage() != null ? data.getMobileImage() : "");
                    detail.put("webImage", data.getWebImage() != null ? data.getWebImage() : "");
                    detail.put("webThumbnail", data.getWebThumbnail() != null ? data.getWebThumbnail() : "");
                    detail.put("mobileThumbnail", data.getMobileThumbnail() != null ? data.getMobileThumbnail() : "");
                });
        return detail;
    }

    public List<JSONObject> getParentCategories() {
        List<JSONObject> details = new ArrayList<>();
        productParentCategoryRepository.findAll().forEach(cat -> {
            JSONObject detail = getParentCatInfo(cat.getId());
            details.add(detail);
        });
        return details;
    }


    public JSONObject getParentCatInfo(Long id) {
        JSONObject detail = new JSONObject();
        productParentCategoryRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("name", data.getParentCategoryName());
                    detail.put("mobileImage", data.getMobileImage() != null ? data.getMobileImage() : "");
                    detail.put("webImage", data.getWebImage() != null ? data.getWebImage() : "");
                    detail.put("webThumbnail", data.getWebThumbnail() != null ? data.getWebThumbnail() : "");
                    detail.put("mobileThumbnail", data.getMobileThumbnail() != null ? data.getMobileThumbnail() : "");
                    detail.put("unique_name", data.getParentCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                });
        return detail;
    }

    public JSONObject getCategoryInfo(Long id) {
        JSONObject detail = new JSONObject();
        productCategoryRepository.findById(id)
                .ifPresent(data -> {
                    detail.put("id", data.getId());
                    detail.put("categoryId", data.getId());
                    detail.put("parentCategoryId", data.getParentCategoryId());
                    detail.put("name", data.getCategoryName());
                    detail.put("mobileImage", data.getCategoryMobileImage() != null ? data.getCategoryMobileImage() : "");
                    detail.put("webImage", data.getCategoryWebImage() != null ? data.getCategoryWebImage() : "");
                    detail.put("webThumbnail", data.getWebThumbnail() != null ? data.getWebThumbnail() : "");
                    detail.put("mobileThumbnail", data.getMobileThumbnail() != null ? data.getMobileThumbnail() : "");
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
                    detail.put("unique_name", data.getSubCategoryName().replaceAll(" ", "_").toLowerCase().trim());
                    detail.put("mobileImage", data.getMobileImage() != null ? data.getMobileImage() : "");
                    detail.put("webImage", data.getWebImage() != null ? data.getWebImage() : "");
                    detail.put("webThumbnail", data.getWebThumbnail() != null ? data.getWebThumbnail() : "");
                    detail.put("mobileThumbnail", data.getMobileThumbnail() != null ? data.getMobileThumbnail() : "");
                });
        return detail;
    }
}
