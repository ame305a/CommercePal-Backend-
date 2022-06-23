package com.commerce.pal.backend.controller.agent;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.transaction.OrderProcessingService;
import com.commerce.pal.backend.repo.user.AgentRepository;
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
@RequestMapping({"/prime/api/v1/agent/order"})
@SuppressWarnings("Duplicates")
public class AgentOrderController {

    private final GlobalMethods globalMethods;
    private final SpecificationsDao specificationsDao;
    private final AgentRepository agentRepository;
    private final OrderProcessingService orderProcessingService;

    @Autowired
    public AgentOrderController(GlobalMethods globalMethods,
                                SpecificationsDao specificationsDao,
                                AgentRepository agentRepository,
                                OrderProcessingService orderProcessingService) {
        this.globalMethods = globalMethods;
        this.specificationsDao = specificationsDao;
        this.agentRepository = agentRepository;
        this.orderProcessingService = orderProcessingService;
    }

    @RequestMapping(value = {"/order-summary"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> orderSummary() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(agent -> {
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
