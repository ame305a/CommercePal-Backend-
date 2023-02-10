package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.ProductFeature;
import com.commerce.pal.backend.models.product.UoM;
import com.commerce.pal.backend.repo.product.ProductFeatureRepository;
import com.commerce.pal.backend.repo.product.UoMRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal/product/features"})
@SuppressWarnings("Duplicates")
public class ProductFeaturesManagementController {
    private final UoMRepository uoMRepository;
    private final ProductFeatureRepository productFeatureRepository;

    @Autowired
    public ProductFeaturesManagementController(UoMRepository uoMRepository,
                                               ProductFeatureRepository productFeatureRepository) {
        this.uoMRepository = uoMRepository;
        this.productFeatureRepository = productFeatureRepository;
    }

    @RequestMapping(value = {"/add-feature"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> addProductFeature(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(request);
            ProductFeature feature = new ProductFeature();
            feature.setSubCategoryId(Long.valueOf(jsonObject.getString("subCategoryId")));
            feature.setFeatureName(jsonObject.getString("featureName"));
            feature.setUnitOfMeasure(jsonObject.getString("unitOfMeasure"));
            feature.setVariableType(jsonObject.getString("variableType"));
            feature.setCreatedDate(Timestamp.from(Instant.now()));
            feature.setStatus(1);
            productFeatureRepository.save(feature);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/update-feature"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> updateProductFeature(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            productFeatureRepository.findById(jsonObject.getLong("id"))
                    .ifPresentOrElse(feature -> {
                        feature.setSubCategoryId(Long.valueOf(jsonObject.getString("subCategoryId")));
                        feature.setFeatureName(jsonObject.getString("featureName"));
                        feature.setUnitOfMeasure(jsonObject.getString("unitOfMeasure"));
                        feature.setVariableType(jsonObject.getString("variableType"));
                        productFeatureRepository.save(feature);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request failed");
                    });
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/delete-feature"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> deleteProductFeature(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);

            Long res = productFeatureRepository.removeProductFeatureById(jsonObject.getLong("id"));

            log.log(Level.INFO, "Del Res: " + String.valueOf(res));
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");

        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-features"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getProductFeatures(@RequestParam("sub-category") Optional<String> sub) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        sub.ifPresentOrElse(subCat -> {
            productFeatureRepository.findProductFeaturesBySubCategoryId(Long.valueOf(subCat))
                    .forEach(productFeature -> {
                        JSONObject detail = new JSONObject();
                        detail.put("featureId", productFeature.getId());
                        detail.put("subCategoryId", productFeature.getSubCategoryId());
                        detail.put("featureName", productFeature.getFeatureName());
                        detail.put("unitOfMeasure", productFeature.getUnitOfMeasure());
                        detail.put("variableType", productFeature.getVariableType());
                        details.add(detail);
                    });
        }, () -> {
            productFeatureRepository.findAll().forEach(productFeature -> {
                JSONObject detail = new JSONObject();
                detail.put("featureId", productFeature.getId());
                detail.put("subCategoryId", productFeature.getSubCategoryId());
                detail.put("featureName", productFeature.getFeatureName());
                detail.put("unitOfMeasure", productFeature.getUnitOfMeasure());
                detail.put("variableType", productFeature.getVariableType());
                details.add(detail);
            });
        });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/add-uom"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> addUnitOfMeasure(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(request);
            UoM uom = new UoM();
            uom.setUoM(jsonObject.getString("UoM"));
            uom.setDescription(jsonObject.getString("Description"));
            uom.setCreatedDate(Timestamp.from(Instant.now()));
            uoMRepository.save(uom);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/update-uom"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> updateUnitOfMeasure(@RequestBody String parent) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(parent);
            uoMRepository.findById(jsonObject.getInt("Id"))
                    .ifPresentOrElse(uom -> {
                        uom.setUoM(jsonObject.getString("UoM"));
                        uom.setDescription(jsonObject.getString("Description"));
                        uoMRepository.save(uom);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                                .put("statusDescription", "failed")
                                .put("statusMessage", "Request failed");
                    });
        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/get-uom"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getUnitOfMeasure() {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> details = new ArrayList<>();
        uoMRepository.findAll().forEach(uom -> {
            JSONObject detail = new JSONObject();
            detail.put("Id", uom.getId());
            detail.put("UoM", uom.getUoM());
            detail.put("Description", uom.getDescription());
            details.add(detail);
        });
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }
}
