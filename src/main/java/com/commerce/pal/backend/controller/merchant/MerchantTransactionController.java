package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.transaction.TransactionProcessingService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/merchant/transaction"})
@SuppressWarnings("Duplicates")
public class MerchantTransactionController {

    private final GlobalMethods globalMethods;
    private final MerchantRepository merchantRepository;
    private final TransactionProcessingService transactionProcessingService;

    @Autowired
    public MerchantTransactionController(GlobalMethods globalMethods,
                                         MerchantRepository merchantRepository,
                                         TransactionProcessingService transactionProcessingService) {
        this.globalMethods = globalMethods;
        this.merchantRepository = merchantRepository;
        this.transactionProcessingService = transactionProcessingService;
    }

    @RequestMapping(value = {"/payment-summary"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getPaymentSummary() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
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
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
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

    @RequestMapping(value = {"/account-balance"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getAccountBalance() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    String balance = "0.00";
                    balance = globalMethods.getAccountBalance(merchant.getTillNumber());
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("balance", new BigDecimal(balance))
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Merchant Does not exists")
                            .put("statusMessage", "Merchant Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }
}
