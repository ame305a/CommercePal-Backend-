package com.commerce.pal.backend.controller.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.transaction.TransactionProcessingService;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/business/transaction"})
@SuppressWarnings("Duplicates")
public class BusinessTransactionController {

    private final GlobalMethods globalMethods;
    private final BusinessRepository businessRepository;
    private final TransactionProcessingService transactionProcessingService;

    @Autowired
    public BusinessTransactionController(GlobalMethods globalMethods,
                                         BusinessRepository businessRepository,
                                         TransactionProcessingService transactionProcessingService) {
        this.globalMethods = globalMethods;
        this.businessRepository = businessRepository;
        this.transactionProcessingService = transactionProcessingService;
    }

    @RequestMapping(value = {"/payment-summary"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getPaymentSummary() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(business -> {
                    List<JSONObject> details = transactionProcessingService.getPayment(responseMap);
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("details", details)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/commission-summary"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getCommissionSummary() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        businessRepository.findBusinessByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(business -> {
                    List<JSONObject> details = transactionProcessingService.getPayment(responseMap);
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("details", details)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }
}
