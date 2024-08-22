package com.commerce.pal.backend.controller.customer;

import com.commerce.pal.backend.dto.CommercePalCoinTransferRequest;
import com.commerce.pal.backend.module.users.referral_codes.ReferralCodeService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/customer/referrals"})
@SuppressWarnings("Duplicates")
public class CustomerReferralController {

    private final ReferralCodeService referralCodeService;

    public CustomerReferralController(ReferralCodeService referralCodeService) {
        this.referralCodeService = referralCodeService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> retrieveCustomerReferralsHistory() {
        JSONObject referralHistory = referralCodeService.retrieveCustomerReferralsHistory();
        return ResponseEntity.ok(referralHistory.toString());
    }


//    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> retrieveReferralTransactions() {
//        JSONObject response = referralCodeService.retrieveReferralTransactions();
//        return ResponseEntity.ok(response.toString());
//    }
//
}
