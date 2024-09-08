package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.app_settings.TargetBannerRepository;
import com.commerce.pal.backend.repo.app_settings.TargetSectionChildrenRepository;
import com.commerce.pal.backend.repo.app_settings.TargetSectionRepository;
import com.commerce.pal.backend.repo.app_settings.TargetSettingRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/app/dashboard"})
@SuppressWarnings("Duplicates")
public class AppDashboardController {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final TargetBannerRepository targetBannerRepository;
    private final TargetSettingRepository targetSettingRepository;
    private final TargetSectionRepository targetSectionRepository;
    private final TargetSectionChildrenRepository targetSectionChildrenRepository;

    @Autowired
    public AppDashboardController(GlobalMethods globalMethods,
                                  ProductService productService,
                                  CategoryService categoryService,
                                  TargetBannerRepository targetBannerRepository,
                                  TargetSettingRepository targetSettingRepository,
                                  TargetSectionRepository targetSectionRepository,
                                  TargetSectionChildrenRepository targetSectionChildrenRepository) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.categoryService = categoryService;
        this.targetBannerRepository = targetBannerRepository;
        this.targetSettingRepository = targetSettingRepository;
        this.targetSectionRepository = targetSectionRepository;
        this.targetSectionChildrenRepository = targetSectionChildrenRepository;
    }

    @RequestMapping(value = {"/get-schema-settings"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getSchemaSettings() {
        JSONObject responseMap = new JSONObject();
        JSONObject settings = new JSONObject();
        settings.put("language", "en");
        List<JSONObject> schemas = new ArrayList<>();

        targetSettingRepository.findAll().forEach(targetSetting -> {
            JSONObject oneTarget = new JSONObject();
            oneTarget.put("targetId", targetSetting.getId());
            oneTarget.put("target", targetSetting.getTarget());
            oneTarget.put("display_name", targetSetting.getTargetDisplay());
            ArrayList<String> banners = new ArrayList<String>();

            targetBannerRepository.findTargetBannersBySchemaSettingIdAndType(targetSetting.getId(), "Web").forEach(tagBanner -> {
                banners.add(tagBanner.getBannerUrl());
            });
            oneTarget.put("banners", banners);
            ArrayList<String> mobileBanners = new ArrayList<String>();
            targetBannerRepository.findTargetBannersBySchemaSettingIdAndType(targetSetting.getId(), "Mobile").forEach(tagBanner -> {
                mobileBanners.add(tagBanner.getBannerUrl());
            });
            oneTarget.put("mobileBanners", mobileBanners);
            List<JSONObject> sections = new ArrayList<>();
            targetSectionRepository.findTargetSectionByTargetId(targetSetting.getId())
                    .forEach(tarSec -> {
                        JSONObject section = new JSONObject();
                        section.put("key", tarSec.getSectionKey());
                        section.put("display_name", tarSec.getDisplayName());
                        section.put("sectionId", tarSec.getId());
                        section.put("catalogueType", tarSec.getCatalogueType());
                        section.put("template", tarSec.getTemplate());
                        section.put("description", tarSec.getDescription());
                        sections.add(section);
                    });
            oneTarget.put("sections", sections);
            schemas.add(oneTarget);
        });
        settings.put("schemas", schemas);
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("settings", settings)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }


    @RequestMapping(value = {"/get-setting-mobile-catalogue"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getSectionMobileCatalogue(@RequestParam("target") String target) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> catalogues = new ArrayList<>();
        targetSectionRepository.findTargetSectionByTargetId(Integer.valueOf(target))
                .forEach(targetSection -> {
                    JSONObject insideCatalogue = new JSONObject();
                    insideCatalogue.put("display_name", targetSection.getDisplayName());
                    insideCatalogue.put("catalogueType", targetSection.getCatalogueType());
                    insideCatalogue.put("template", targetSection.getTemplate());
                    insideCatalogue.put("key", targetSection.getSectionKey());
                    List<JSONObject> items;
                                /*
                                Brand
                                Product above 1000
                                Product under 1000
                                ParentCategory
                                Category
                                SubCategory
                                 */
                    if (targetSection.getSectionKey().equals("under_1000")) {
                        items = productService.getRandomProductsUnder1000();
                    } else if (targetSection.getSectionKey().equals("above_1000")) {
                        items = productService.getRandomProductsAbove1000();
                    } else {
                        items = new ArrayList<>();
                        targetSectionChildrenRepository.findTargetSectionChildrenByTargetSectionId(targetSection.getId())
                                .forEach(targetSectionChildren -> {
                                    JSONObject item = new JSONObject();
                                    item.put("sectionType", targetSectionChildren.getType());
                                    item.put("sectionDescription", targetSectionChildren.getDescription());
                                    if (targetSectionChildren.getType().equals("Brand")) {
                                        JSONObject response = categoryService.getBrandInfo(targetSectionChildren.getItemId());
                                        items.add(globalMethods.mergeJSONObjects(response, item));
                                    }
//                                    else if (targetSectionChildren.getType().equals("Product")) {
//                                        JSONObject response = productService.getProductListDetails(Long.valueOf(targetSectionChildren.getItemId()));
//                                        items.add(globalMethods.mergeJSONObjects(response, item));
//                                    }
                                    else if (targetSectionChildren.getType().equals("ParentCategory")) {
                                        JSONObject response = categoryService.getParentCatInfo(Long.valueOf(targetSectionChildren.getItemId()));
                                        items.add(globalMethods.mergeJSONObjects(response, item));
                                    } else if (targetSectionChildren.getType().equals("Category")) {
                                        JSONObject response = categoryService.getCategoryInfo(Long.valueOf(targetSectionChildren.getItemId()));
                                        items.add(globalMethods.mergeJSONObjects(response, item));
                                    } else if (targetSectionChildren.getType().equals("SubCategory")) {
                                        JSONObject response = categoryService.getSubCategoryInfo(Long.valueOf(targetSectionChildren.getItemId()));
                                        items.add(globalMethods.mergeJSONObjects(response, item));
                                    }

                                });
                    }

                    insideCatalogue.put("items", items);
                    catalogues.add(insideCatalogue);
                });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("catalogue", catalogues)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }
}
