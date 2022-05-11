package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.models.product.categories.ProductCategory;
import com.commerce.pal.backend.models.product.categories.ProductParentCategory;
import com.commerce.pal.backend.models.product.categories.ProductSubCategory;
import com.commerce.pal.backend.repo.product.*;
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
import java.util.Random;

@Log
@Service
@SuppressWarnings("Duplicates")
public class ProductCategoryService {
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final BrandImageRepository brandImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final ProductParentCategoryRepository productParentCategoryRepository;

    @Autowired
    public ProductCategoryService(CategoryService categoryService,
                                  ProductRepository productRepository,
                                  BrandImageRepository brandImageRepository,
                                  ProductCategoryRepository productCategoryRepository,
                                  ProductSubCategoryRepository productSubCategoryRepository,
                                  ProductParentCategoryRepository productParentCategoryRepository) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.brandImageRepository = brandImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.productParentCategoryRepository = productParentCategoryRepository;
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


}
