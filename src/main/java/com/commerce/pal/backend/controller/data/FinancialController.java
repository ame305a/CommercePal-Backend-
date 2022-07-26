package com.commerce.pal.backend.controller.data;


import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.users.business.BusinessCollateralService;
import com.commerce.pal.backend.module.users.business.BusinessService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/data/financial"})
@SuppressWarnings("Duplicates")
public class FinancialController {
    private final BusinessService businessService;
    private final BusinessCollateralService businessCollateralService;

    public FinancialController(BusinessService businessService,
                               BusinessCollateralService businessCollateralService) {
        this.businessService = businessService;
        this.businessCollateralService = businessCollateralService;
    }

    @RequestMapping(value = {"/request"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> processRequest(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(request);
            switch (jsonObject.getString("Type")) {
                case "BUSINESS":
                    responseMap = businessService.getBusinessInfo(jsonObject.getLong("TypeId"));
                    break;
                case "BUSINESSES":
                    responseMap = businessService.getCollateralBusiness(jsonObject.getLong("TypeId"));
                    break;
                case "BUSINESS-LOAN-LIMIT":
                    responseMap = businessCollateralService.getLoanLimit(jsonObject.getLong("TypeId"));
                    break;
                case "APPROVE-LOAN-LIMIT":
                    responseMap = businessCollateralService.reviewLoanLimit(jsonObject);
                    break;
                case "BUSINESS-COLLATERAL":
                    responseMap = businessCollateralService.getBusinessCollateral(jsonObject.getLong("TypeId"));
                    break;
                case "REVIEW-COLLATERAL":
                    responseMap = businessCollateralService.getBusinessCollateral(jsonObject.getLong("TypeId"));
                    break;
                case "APPROVE-COLLATERAL":
                    responseMap = businessCollateralService.getBusinessCollateral(jsonObject.getLong("TypeId"));
                    break;
            }
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
}
