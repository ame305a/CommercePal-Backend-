package com.commerce.pal.backend.controller.data;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.users.*;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.module.users.business.BusinessService;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/data"})
@SuppressWarnings("Duplicates")
public class DataAccessController {
    private final AgentService agentService;
    private final ProductService productService;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final MerchantService merchantService;
    private final MessengerService messengerService;
    private final SubProductService subProductService;
    private final LoginValidationRepository loginValidationRepository;

    @Autowired
    public DataAccessController(AgentService agentService,
                                ProductService productService,
                                BusinessService businessService,
                                CustomerService customerService,
                                MerchantService merchantService,
                                MessengerService messengerService,
                                SubProductService subProductService,
                                LoginValidationRepository loginValidationRepository) {
        this.agentService = agentService;
        this.productService = productService;
        this.businessService = businessService;
        this.customerService = customerService;
        this.merchantService = merchantService;
        this.messengerService = messengerService;
        this.subProductService = subProductService;
        this.loginValidationRepository = loginValidationRepository;
    }

    @RequestMapping(value = {"/request"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getDataRequest(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(request);
            switch (jsonObject.getString("Type")) {
                case "PRODUCT":
                    responseMap = productService.getProductLimitedDetails(jsonObject.getLong("TypeId"));
                    break;
                case "SUB-PRODUCT":
                    responseMap = subProductService.getSubProductInfo(jsonObject.getLong("TypeId"));
                    break;
                case "PRODUCT-AND-SUB":
                    responseMap = productService.getSubProductInfo(jsonObject.getLong("TypeId"), jsonObject.getLong("SubProductId"));
                    break;
                case "AGENT":
                    responseMap = agentService.getAgentInfo(jsonObject.getLong("TypeId"));
                    break;
                case "CUSTOMER":
                    responseMap = customerService.getCustomerInfo(jsonObject.getLong("TypeId"));
                    break;
                case "MERCHANT":
                    responseMap = merchantService.getMerchantInfo(jsonObject.getLong("TypeId"));
                    break;
                case "MERCHANT-ADDRESS":
                    responseMap = merchantService.getMerchantAddressInfo(jsonObject.getLong("TypeId"));
                    break;
                case "CUSTOMER-ADDRESS":
                    responseMap = customerService.getCustomerAddressById(jsonObject.getLong("TypeId"));
                    break;
                case "MESSENGER":
                    responseMap = messengerService.getMessengerInfo(jsonObject.getLong("TypeId"));
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
