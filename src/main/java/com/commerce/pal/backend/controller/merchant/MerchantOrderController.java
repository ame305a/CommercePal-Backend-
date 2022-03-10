package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.transaction.OrderProcessingService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
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
@RequestMapping({"/prime/api/v1/merchant/order"})
@SuppressWarnings("Duplicates")
public class MerchantOrderController {

    private final GlobalMethods globalMethods;
    private final SpecificationsDao specificationsDao;
    private final MerchantRepository merchantRepository;
    private final OrderProcessingService orderProcessingService;

    @Autowired
    public MerchantOrderController(GlobalMethods globalMethods,
                                   SpecificationsDao specificationsDao,
                                   MerchantRepository merchantRepository,
                                   OrderProcessingService orderProcessingService) {
        this.globalMethods = globalMethods;
        this.specificationsDao = specificationsDao;
        this.merchantRepository = merchantRepository;
        this.orderProcessingService = orderProcessingService;
    }

    @RequestMapping(value = {"/order-summary"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> orderSummary() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    List<JSONObject> details = orderProcessingService.getOrderSummary(responseMap);
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
