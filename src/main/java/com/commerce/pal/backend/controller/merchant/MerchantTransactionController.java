package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.transaction.MerchantWithdrawal;
import com.commerce.pal.backend.module.transaction.TransactionProcessingService;
import com.commerce.pal.backend.repo.transaction.MerchantWithdrawalRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/merchant/transaction"})
@SuppressWarnings("Duplicates")
public class MerchantTransactionController {

    private final GlobalMethods globalMethods;
    private final MerchantRepository merchantRepository;
    private final MerchantWithdrawalRepository merchantWithdrawalRepository;
    private final TransactionProcessingService transactionProcessingService;

    @Autowired
    public MerchantTransactionController(GlobalMethods globalMethods,
                                         MerchantRepository merchantRepository,
                                         MerchantWithdrawalRepository merchantWithdrawalRepository,
                                         TransactionProcessingService transactionProcessingService) {
        this.globalMethods = globalMethods;
        this.merchantRepository = merchantRepository;
        this.merchantWithdrawalRepository = merchantWithdrawalRepository;
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

    @RequestMapping(value = "/request-withdrawal", method = RequestMethod.POST)
    public ResponseEntity<?> requestForWithdrawal(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(merchant -> {
                        String balance = "0.00";
                        balance = globalMethods.getAccountBalance(merchant.getTillNumber());

                        if (Double.valueOf(balance) >= request.getBigDecimal("Amount").doubleValue()) {
                            merchantWithdrawalRepository.findMerchantWithdrawalByMerchantIdAndStatus(
                                            merchant.getMerchantId(), 0)
                                    .ifPresentOrElse(merchantWithdrawal -> {
                                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                                .put("transRef", merchantWithdrawal.getTransRef())
                                                .put("statusDescription", "There is a pending request")
                                                .put("statusMessage", "There is a pending request");
                                    }, () -> {
                                        String validationCode = globalMethods.generateValidationCode();
                                        String transRef = globalMethods.generateTrans();
                                        AtomicReference<MerchantWithdrawal> withdrawal = new AtomicReference<>(new MerchantWithdrawal());
                                        withdrawal.get().setMerchantId(merchant.getMerchantId());
                                        withdrawal.get().setTransRef(transRef);
                                        withdrawal.get().setWithdrawalMethod(request.getString("WithdrawalMethod"));
                                        withdrawal.get().setWithdrawalType(request.getString("WithdrawalType"));
                                        withdrawal.get().setAccount(request.getString("Account"));
                                        withdrawal.get().setAmount(request.getBigDecimal("Amount"));
                                        withdrawal.get().setValidationCode(globalMethods.encryptCode(validationCode));
                                        withdrawal.get().setValidationDate(Timestamp.from(Instant.now()));
                                        withdrawal.get().setStatus(0);
                                        withdrawal.get().setRequestDate(Timestamp.from(Instant.now()));
                                        withdrawal.get().setResponseStatus(0);
                                        withdrawal.get().setBillTransRef("Failed");
                                        withdrawal.get().setResponsePayload("Pending");
                                        withdrawal.get().setResponseStatus(0);
                                        withdrawal.get().setResponseDescription("Pending");
                                        withdrawal.get().setResponseDate(Timestamp.from(Instant.now()));
                                        withdrawal.set(merchantWithdrawalRepository.save(withdrawal.get()));

                                        JSONObject merchWth = new JSONObject();
                                        merchWth.put("TransRef", transRef);

                                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                                .put("statusDescription", "success")
                                                .put("transRef", transRef)
                                                .put("statusMessage", "Request Successful");
                                    });

                        } else {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Insufficient Balance")
                                    .put("statusMessage", "Insufficient Balance");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Merchant Does not exists")
                                .put("statusMessage", "Merchant Does not exists");
                    });

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
