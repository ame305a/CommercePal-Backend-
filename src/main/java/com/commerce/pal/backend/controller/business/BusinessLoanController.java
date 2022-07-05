package com.commerce.pal.backend.controller.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.users.business.BusinessCollateralService;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/business/loan"})
@SuppressWarnings("Duplicates")
public class BusinessLoanController {
    private final GlobalMethods globalMethods;
    private final BusinessRepository businessRepository;
    private final BusinessCollateralService businessCollateralService;

    @Autowired
    public BusinessLoanController(GlobalMethods globalMethods,
                                  BusinessRepository businessRepository,
                                  BusinessCollateralService businessCollateralService) {
        this.globalMethods = globalMethods;
        this.businessRepository = businessRepository;
        this.businessCollateralService = businessCollateralService;
    }

    @RequestMapping(value = "/get-business-collateral", method = RequestMethod.GET)
    public ResponseEntity<?> getBusinessCollateral() {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        responseMap.set(businessCollateralService.getBusinessCollateral(business.getBusinessId()));
                        responseMap.get().put("CollateralLimit" , businessCollateralService.collateralLoanLimit(business.getBusinessId()));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Business Does not exists")
                                .put("statusMessage", "Business Does not exists");
                    });
        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-loan-limit", method = RequestMethod.POST)
    public ResponseEntity<?> getLoanLimit() {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(business -> {
                        responseMap.set(businessCollateralService.getLoanLimit(business.getBusinessId()));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Business Does not exists")
                                .put("statusMessage", "Business Does not exists");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
