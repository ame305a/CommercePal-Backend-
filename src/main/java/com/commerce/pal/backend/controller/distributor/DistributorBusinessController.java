package com.commerce.pal.backend.controller.distributor;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.users.business.BusinessCollateralService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/distributor/business/collateral"})
@SuppressWarnings("Duplicates")
public class DistributorBusinessController {
    private final GlobalMethods globalMethods;
    private final BusinessCollateralService businessCollateralService;

    @Autowired
    public DistributorBusinessController(GlobalMethods globalMethods,
                                         BusinessCollateralService businessCollateralService) {
        this.globalMethods = globalMethods;
        this.businessCollateralService = businessCollateralService;
    }

    @RequestMapping(value = "/add-collateral", method = RequestMethod.POST)
    public ResponseEntity<?> addCollateral(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        "BUSINESS", String.valueOf(reqBody.getLong("BusinessId")))) {
                    responseMap = businessCollateralService.addCollateral(reqBody);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to distributor")
                            .put("statusMessage", "The User does not belong to distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-collateral", method = RequestMethod.POST)
    public ResponseEntity<?> updateCollateral(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        "BUSINESS", String.valueOf(reqBody.getLong("BusinessId")))) {
                    responseMap = businessCollateralService.updateCollateral(reqBody);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to distributor")
                            .put("statusMessage", "The User does not belong to distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/upload-document", method = RequestMethod.POST)
    public ResponseEntity<?> uploadCollateralDocument(@RequestPart(value = "file") MultipartFile file,
                                                      @RequestPart(value = "collateralId") String collateralId) {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                responseMap = businessCollateralService.addCollateralDocument(file, Integer.valueOf(collateralId));
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-business-collateral", method = RequestMethod.GET)
    public ResponseEntity<?> getBusinessCollateral(@RequestParam("businessId") String businessId) {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        "BUSINESS", String.valueOf(businessId))) {
                    responseMap = businessCollateralService.getBusinessCollateral(Long.valueOf(businessId));
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User is not a distributor")
                            .put("statusMessage", "The User is not a distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-loan-limit", method = RequestMethod.POST)
    public ResponseEntity<?> updateBusinessLoanLimit(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        "BUSINESS", String.valueOf(reqBody.getLong("BusinessId")))) {
                    responseMap = businessCollateralService.updateLoanLimit(reqBody);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to distributor")
                            .put("statusMessage", "The User does not belong to distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
