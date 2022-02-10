package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.app_settings.SchemaCatalogue;
import com.commerce.pal.backend.models.app_settings.TargetBanner;
import com.commerce.pal.backend.models.app_settings.TargetSchema;
import com.commerce.pal.backend.models.app_settings.TargetSetting;
import com.commerce.pal.backend.repo.app_settings.SchemaCatalogueRepository;
import com.commerce.pal.backend.repo.app_settings.TargetBannerRepository;
import com.commerce.pal.backend.repo.app_settings.TargetSchemaRepository;
import com.commerce.pal.backend.repo.app_settings.TargetSettingRepository;
import com.commerce.pal.backend.repo.product.ProductSubCategoryRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/app"})
@SuppressWarnings("Duplicates")
public class PortalAppSettingController {
    @Autowired
    private UploadService uploadService;

    private final TargetSchemaRepository targetSchemaRepository;
    private final TargetBannerRepository targetBannerRepository;
    private final TargetSettingRepository targetSettingRepository;
    private final SchemaCatalogueRepository schemaCatalogueRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;

    @Autowired
    public PortalAppSettingController(TargetSchemaRepository targetSchemaRepository,
                                      TargetBannerRepository targetBannerRepository,
                                      TargetSettingRepository targetSettingRepository,
                                      SchemaCatalogueRepository schemaCatalogueRepository,
                                      ProductSubCategoryRepository productSubCategoryRepository) {
        this.targetSchemaRepository = targetSchemaRepository;
        this.targetBannerRepository = targetBannerRepository;
        this.targetSettingRepository = targetSettingRepository;
        this.schemaCatalogueRepository = schemaCatalogueRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
    }

    @RequestMapping(value = "/portal/add-target-setting", method = RequestMethod.POST)
    public ResponseEntity<?> addTargetSetting(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqJson = new JSONObject(req);
            targetSettingRepository.findTargetSettingByTarget(reqJson.getString("target"))
                    .ifPresentOrElse(targetSetting -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The Target already exist")
                                .put("statusMessage", "The Target already exist");
                    }, () -> {
                        TargetSetting targetSetting = new TargetSetting();
                        targetSetting.setStatus(0);
                        targetSetting.setTarget(reqJson.getString("target"));
                        targetSetting.setTargetDisplay(reqJson.getString("targetDisplay"));
                        targetSetting.setCreatedDate(Timestamp.from(Instant.now()));
                        targetSettingRepository.save(targetSetting);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/portal/add-target-banner", method = RequestMethod.POST)
    public ResponseEntity<?> addTargetBanner(@RequestPart(value = "file") MultipartFile multipartFile,
                                             @RequestPart(value = "target") String target,
                                             @RequestPart(value = "type") String type) {
        JSONObject responseMap = new JSONObject();
        try {
            if (targetSettingRepository.existsById(Integer.valueOf(target))) {
                String bannerUrl = uploadService.uploadFileAlone(multipartFile, type, "Banners");
                TargetBanner targetBanner = new TargetBanner();
                targetBanner.setStatus(0);
                targetBanner.setType(type);
                targetBanner.setSchemaSettingId(Integer.valueOf(target));
                targetBanner.setBannerUrl(bannerUrl);
                targetBanner.setCreatedDate(Timestamp.from(Instant.now()));
                targetBannerRepository.save(targetBanner);
                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "success")
                        .put("statusMessage", "Request Successful");
            } else {
                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                        .put("statusDescription", "The Target does not exist")
                        .put("statusMessage", "The Target does not exist");
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/portal/add-target-sections", method = RequestMethod.POST)
    public ResponseEntity<?> addTargetSections(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqJson = new JSONObject(req);
            if (targetSettingRepository.existsById(reqJson.getInt("target"))) {
                targetSchemaRepository.findTargetSchemaByTargetKeyAndSchemaSettingId(reqJson.getString("key"), reqJson.getInt("target"))
                        .ifPresentOrElse(targetSchema -> {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "The Section with the target id already exist")
                                    .put("statusMessage", "The Section with the target id already exist");
                        }, () -> {
                            TargetSchema targetSchema = new TargetSchema();
                            targetSchema.setSchemaSettingId(reqJson.getInt("target"));
                            targetSchema.setTargetKey(reqJson.getString("key"));
                            targetSchema.setDisplayName(reqJson.getString("displayName"));
                            targetSchema.setStatus(0);
                            targetSchema.setCreatedDate(Timestamp.from(Instant.now()));
                            targetSchemaRepository.save(targetSchema);
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("statusMessage", "Request Successful");
                        });

            } else {
                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                        .put("statusDescription", "The Target does not exist")
                        .put("statusMessage", "The Target does not exist");
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/portal/add-section-catalogue", method = RequestMethod.POST)
    public ResponseEntity<?> addSectionsCatalogue(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqJson = new JSONObject(req);

            targetSchemaRepository.findTargetSchemaById(reqJson.getInt("section"))
                    .ifPresentOrElse(targetSchema -> {
                        SchemaCatalogue schemaCatalogue = new SchemaCatalogue();
                        schemaCatalogue.setSchemaSettingId(targetSchema.getSchemaSettingId());
                        schemaCatalogue.setTargetSchemaId(targetSchema.getId());
                        schemaCatalogue.setCategoryType(reqJson.getString("categoryType"));
                        schemaCatalogue.setCategoryId(reqJson.getInt("category"));
                        schemaCatalogue.setDescription(reqJson.getString("description"));
                        schemaCatalogue.setStatus(0);
                        schemaCatalogue.setCreatedDate(Timestamp.from(Instant.now()));
                        schemaCatalogueRepository.save(schemaCatalogue);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The Section with the section id does not exist")
                                .put("statusMessage", "The Section with the section id does not exist");
                    });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
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
            targetSchemaRepository.findTargetSchemaBySchemaSettingId(targetSetting.getId())
                    .forEach(tarSchema -> {
                        JSONObject schema = new JSONObject();
                        schema.put("key", tarSchema.getTargetKey());
                        schema.put("display_name", tarSchema.getDisplayName());
                        schema.put("sectionId", tarSchema.getId());
                        sections.add(schema);
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
    public ResponseEntity<?> getTargetMobileCatalogue(@RequestParam("target") String target) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> catalogues = new ArrayList<>();
        JSONObject catalogue = new JSONObject();
        targetSchemaRepository.findTargetSchemaBySchemaSettingId(Integer.valueOf(target))
                .forEach(tarSchema -> {
                    JSONObject insideCatalogue = new JSONObject();
                    insideCatalogue.put("display_name", tarSchema.getDisplayName());
                    insideCatalogue.put("type", tarSchema.getTargetKey());
                    List<JSONObject> items = new ArrayList<>();
                    schemaCatalogueRepository.findSchemaCataloguesByTargetSchemaId(tarSchema.getId())
                            .forEach(schemaCatalogue -> {
                                JSONObject item = new JSONObject();
                                // Check Category type - parent/category/subcategory
                                if (schemaCatalogue.getCategoryType().equals("subCategory")) {
                                    productSubCategoryRepository.findById(Long.valueOf(schemaCatalogue.getCategoryId()))
                                            .ifPresent(sub -> {
                                                item.put("type", schemaCatalogue.getCategoryType());
                                                item.put("id", sub.getId());
                                                item.put("name", sub.getSubCategoryName());
                                                item.put("image_url", sub.getWebImage());
                                                item.put("mobile_image_url", sub.getMobileImage());
                                                item.put("description", schemaCatalogue.getDescription());
                                                items.add(item);
                                            });
                                } else {

                                }

                            });
                    insideCatalogue.put("items", items);
                    // catalogue.put(tarSchema.getTargetKey(), insideCatalogue);
                    catalogues.add(insideCatalogue);
                });
        //catalogues.add(catalogue);
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("catalogue", catalogues)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-setting-catalogue"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getTargetCatalogue(@RequestParam("target") String target) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> catalogues = new ArrayList<>();
        JSONObject catalogue = new JSONObject();
        targetSchemaRepository.findTargetSchemaBySchemaSettingId(Integer.valueOf(target))
                .forEach(tarSchema -> {
                    JSONObject insideCatalogue = new JSONObject();
                    insideCatalogue.put("display_name", tarSchema.getDisplayName());
//                    insideCatalogue.put("type", tarSchema.getTargetKey());
                    List<JSONObject> items = new ArrayList<>();
                    schemaCatalogueRepository.findSchemaCataloguesByTargetSchemaId(tarSchema.getId())
                            .forEach(schemaCatalogue -> {
                                JSONObject item = new JSONObject();
                                // Check Category type - parent/category/subcategory
                                if (schemaCatalogue.getCategoryType().equals("subCategory")) {
                                    productSubCategoryRepository.findById(Long.valueOf(schemaCatalogue.getCategoryId()))
                                            .ifPresent(sub -> {
                                                item.put("type", schemaCatalogue.getCategoryType());
                                                item.put("id", sub.getId());
                                                item.put("name", sub.getSubCategoryName());
                                                item.put("image_url", sub.getWebImage());
                                                item.put("mobile_image_url", sub.getMobileImage());
                                                item.put("description", schemaCatalogue.getDescription());
                                                items.add(item);
                                            });
                                } else {

                                }

                            });
                    insideCatalogue.put("items", items);
                     catalogue.put(tarSchema.getTargetKey(), insideCatalogue);
//                    catalogues.add(insideCatalogue);
                });
        catalogues.add(catalogue);
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("catalogue", catalogues)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }
}
