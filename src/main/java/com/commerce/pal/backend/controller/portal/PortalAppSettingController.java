package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.app_settings.*;
import com.commerce.pal.backend.repo.app_settings.*;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
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
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal/app"})
@SuppressWarnings("Duplicates")
public class PortalAppSettingController {
    @Autowired
    private UploadService uploadService;


    private final TargetSchemaRepository targetSchemaRepository;
    private final TargetBannerRepository targetBannerRepository;
    private final TargetSettingRepository targetSettingRepository;
    private final TargetSectionRepository targetSectionRepository;
    private final SchemaCatalogueRepository schemaCatalogueRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final TargetSectionChildrenRepository targetSectionChildrenRepository;

    @Autowired
    public PortalAppSettingController(TargetSchemaRepository targetSchemaRepository,
                                      TargetBannerRepository targetBannerRepository,
                                      TargetSettingRepository targetSettingRepository,
                                      TargetSectionRepository targetSectionRepository,
                                      SchemaCatalogueRepository schemaCatalogueRepository,
                                      ProductCategoryRepository productCategoryRepository,
                                      ProductSubCategoryRepository productSubCategoryRepository,
                                      TargetSectionChildrenRepository targetSectionChildrenRepository) {
        this.targetSchemaRepository = targetSchemaRepository;
        this.targetBannerRepository = targetBannerRepository;
        this.targetSettingRepository = targetSettingRepository;
        this.targetSectionRepository = targetSectionRepository;
        this.schemaCatalogueRepository = schemaCatalogueRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.targetSectionChildrenRepository = targetSectionChildrenRepository;
    }

    @RequestMapping(value = "/add-target-setting", method = RequestMethod.POST)
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

    @RequestMapping(value = "/add-target-banner", method = RequestMethod.POST)
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

    @RequestMapping(value = "/add-target-section", method = RequestMethod.POST)
    public ResponseEntity<?> addTargetSection(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqJson = new JSONObject(req);
            if (targetSettingRepository.existsById(reqJson.getInt("target"))) {
                targetSectionRepository.findTargetSectionBySectionKeyAndTargetId(reqJson.getString("key"), reqJson.getInt("target"))
                        .ifPresentOrElse(targetSection -> {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "The Section with the target id already exist")
                                    .put("statusMessage", "The Section with the target id already exist");
                        }, () -> {
                            TargetSection targetSec = new TargetSection();
                            targetSec.setTargetId(reqJson.getInt("target"));
                            targetSec.setSectionKey(reqJson.getString("key"));
                            targetSec.setDisplayName(reqJson.getString("displayName"));
                            //targetSec.setCatalogueType(reqJson.getString("catalogueType"));
                            targetSec.setCatalogueType("catalogueType");
                            targetSec.setDescription(reqJson.getString("description"));
                            targetSec.setTemplate(reqJson.getString("template"));
                            targetSec.setOrderNumber(1);
                            targetSec.setStatus(1);
                            targetSec.setCreatedDate(Timestamp.from(Instant.now()));
                            targetSectionRepository.save(targetSec);
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

    @RequestMapping(value = "/add-section-catalogue", method = RequestMethod.POST)
    public ResponseEntity<?> addSectionsCatalogue(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqJson = new JSONObject(req);
            targetSectionRepository.findTargetSectionById(reqJson.getInt("section"))
                    .ifPresentOrElse(targetSection -> {
                        targetSectionChildrenRepository.findTargetSectionChildrenByTargetSectionIdAndItemId(
                                reqJson.getInt("section"), reqJson.getLong("itemId")
                        ).ifPresentOrElse(targetSectionChildren -> {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "The Item Already esists")
                                    .put("statusMessage", "he Item Already esists");
                        }, () -> {
                            TargetSectionChildren tarSec = new TargetSectionChildren();
                            tarSec.setTargetSectionId(targetSection.getId());
                            tarSec.setType(reqJson.getString("type"));
                            tarSec.setItemId(reqJson.getInt("itemId"));
                            tarSec.setDescription(reqJson.getString("description"));
                            tarSec.setStatus(1);
                            tarSec.setCreatedDate(Timestamp.from(Instant.now()));
                            targetSectionChildrenRepository.save(tarSec);
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "success")
                                    .put("statusMessage", "Request Successful");
                        });
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The Section with the section id does not exist")
                                .put("statusMessage", "The Section with the section id does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "Add Section Catalogue Error : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
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
                                } else if (schemaCatalogue.getCategoryType().equals("Category")) {
                                    productCategoryRepository.findById(Long.valueOf(schemaCatalogue.getCategoryId()))
                                            .ifPresent(sub -> {
                                                item.put("type", schemaCatalogue.getCategoryType());
                                                item.put("id", sub.getId());
                                                item.put("name", sub.getCategoryName());
                                                item.put("image_url", sub.getCategoryWebImage());
                                                item.put("mobile_image_url", sub.getCategoryMobileImage());
                                                item.put("description", schemaCatalogue.getDescription());
                                                items.add(item);
                                            });

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


}
