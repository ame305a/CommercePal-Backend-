package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.categories.ProductCategory;
import com.commerce.pal.backend.models.product.categories.ProductParentCategory;
import com.commerce.pal.backend.models.product.categories.ProductSubCategory;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.categories.BrandImageRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductParentCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Log
@Service
@SuppressWarnings("Duplicates")
public class ProductCategoryService {
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final BrandImageRepository brandImageRepository;
    private final ProductParentCategoryRepository productParentCategoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;

    @Autowired
    public ProductCategoryService(CategoryService categoryService,
                                  ProductRepository productRepository,
                                  BrandImageRepository brandImageRepository,
                                  ProductCategoryRepository productCategoryRepository,
                                  ProductSubCategoryRepository productSubCategoryRepository,
                                  ProductParentCategoryRepository productParentCategoryRepository, ProductSubCategoryRepository productSubCategoryRepository1) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.brandImageRepository = brandImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productParentCategoryRepository = productParentCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository1;
    }


    public List<JSONObject> getRandomParentCategories(Integer count) {
        List<JSONObject> items = new ArrayList<>();
        List<ProductParentCategory> data = productParentCategoryRepository.findAll();
        for (int i = 0; i < count; i++) {
            Random rand = new Random();
            ProductParentCategory randomData = data.get(rand.nextInt(data.size()));
            JSONObject item = categoryService.getParentCatInfo(randomData.getId());
            item.put("sectionType", "ParentCategory");
            items.add(item);
        }
        return items;
    }

    public List<JSONObject> getRandomCategoriesByParentId(Long parentCategory, Integer count) {
        List<JSONObject> items = new ArrayList<>();
        List<ProductCategory> data = productCategoryRepository.findProductCategoriesByParentCategoryId(parentCategory);
        for (int i = 0; i < count; i++) {
            Random rand = new Random();
            ProductCategory randomData = data.get(rand.nextInt(data.size()));
            JSONObject item = categoryService.getCategoryInfo(randomData.getId());
            item.put("sectionType", "Category");
            items.add(item);
        }
        return items;
    }

    public List<JSONObject> getRandomSubCategoriesByCategoryId(Long category, Integer count) {
        List<JSONObject> items = new ArrayList<>();
        List<ProductSubCategory> data = productSubCategoryRepository.findProductSubCategoriesByProductCategoryId(category);
        for (int i = 0; i < count; i++) {
            Random rand = new Random();
            ProductSubCategory randomData = data.get(rand.nextInt(data.size()));
            JSONObject item = categoryService.getSubCategoryInfo(randomData.getId());
            item.put("sectionType", "SubCategory");
            items.add(item);
        }
        return items;
    }


    public List<JSONObject> getRandomProductsByParentCategory(Long parent, Integer count) {
        List<JSONObject> items = new ArrayList<>();

        return items;
    }

    //Retrieves a paginated list of product categories with support for sorting, filtering, searching, and date range.
    public JSONObject getAllProductCategories(
            int page,
            int size,
            Sort sort,
            Long filterByParentCategory,
            Integer status,
            String searchKeyword,
            Timestamp startDate,
            Timestamp endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductCategory> productParentCategoryPage
                = productCategoryRepository.findByFilterAndDateAndStatus(filterByParentCategory, searchKeyword, startDate, endDate, status, pageable);

        List<JSONObject> productParentCategories = new ArrayList<>();
        productParentCategoryPage.getContent().stream()
                .forEach(category -> {
                    JSONObject detail = new JSONObject();

                    String parentName = "";
                    Optional<ProductParentCategory> optionalProductParentCategory = productParentCategoryRepository.findById(category.getParentCategoryId());
                    if (optionalProductParentCategory.isPresent()) {
                        ProductParentCategory parentCategory = optionalProductParentCategory.get();
                        parentName = parentCategory.getParentCategoryName();
                    }

                    detail.put("parentName", parentName);
                    detail.put("categoryType", category.getCategoryType() != null ? category.getCategoryType() : "");
                    detail.put("categoryName", category.getCategoryName());
                    detail.put("status", category.getStatus());
                    detail.put("createdDate", category.getCreatedDate());

                    productParentCategories.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", productParentCategoryPage.getNumber())
                .put("pageSize", productParentCategoryPage.getSize())
                .put("totalElements", productParentCategoryPage.getTotalElements())
                .put("totalPages", productParentCategoryPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("parentCategories", productParentCategories)
                .put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Product Parent Category Passed")
                .put("statusMessage", "Product Parent Category Passed")
                .put("data", data);

        return response;
    }

    public JSONObject getAllProductCategories(Sort sort) {
        List<ProductCategory> productCategoryList = productCategoryRepository.findAll(sort);

        List<JSONObject> productCategories = new ArrayList<>();
        productCategoryList.stream()
                .forEach(category -> {
                    JSONObject detail = new JSONObject();

                    detail.put("categoryId", category.getId());
                    detail.put("categoryName", category.getCategoryName());
                    detail.put("status", category.getStatus());

                    productCategories.add(detail);
                });

        JSONObject data = new JSONObject();
        data.put("productCategories", productCategories);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Product Category Passed")
                .put("statusMessage", "Product Category Passed")
                .put("data", data);

        return response;
    }
}
