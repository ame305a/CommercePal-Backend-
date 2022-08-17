package com.commerce.pal.backend.controller.agent;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.transaction.AgentFloat;
import com.commerce.pal.backend.module.transaction.TransactionProcessingService;
import com.commerce.pal.backend.repo.transaction.AgentFloatRepository;
import com.commerce.pal.backend.repo.user.AgentRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/agent/transaction"})
@SuppressWarnings("Duplicates")
public class AgentTransactionController {

    private final GlobalMethods globalMethods;
    private final AgentRepository agentRepository;
    private final SpecificationsDao specificationsDao;
    private final AgentFloatRepository agentFloatRepository;
    private final TransactionProcessingService transactionProcessingService;

    @Autowired
    public AgentTransactionController(GlobalMethods globalMethods,
                                      AgentRepository agentRepository,
                                      SpecificationsDao specificationsDao,
                                      AgentFloatRepository agentFloatRepository,
                                      TransactionProcessingService transactionProcessingService) {
        this.globalMethods = globalMethods;
        this.agentRepository = agentRepository;
        this.specificationsDao = specificationsDao;
        this.agentFloatRepository = agentFloatRepository;
        this.transactionProcessingService = transactionProcessingService;
    }

    @RequestMapping(value = "/request-float", method = RequestMethod.POST)
    public ResponseEntity<?> requestFloat(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        AgentFloat agentFloat = new AgentFloat();
                        agentFloat.setAgentId(agent.getAgentId());
                        agentFloat.setAmount(request.getDouble("amount"));
                        agentFloat.setComment(request.getString("comment"));
                        agentFloat.setStatus(0);
                        agentFloat.setRequestDate(Timestamp.from(Instant.now()));
                        agentFloatRepository.save(agentFloat);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "Successful")
                                .put("statusMessage", "Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Agent Does not exists")
                                .put("statusMessage", "Agent Does not exists");
                    });
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }


    @RequestMapping(value = {"/float-request"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getFloatRequest(@RequestParam("transRef") Optional<String> transRef) {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(agent -> {
                    List<SearchCriteria> params = new ArrayList<SearchCriteria>();
                    transRef.ifPresent(value -> {
                        params.add(new SearchCriteria("transRef", ":", value));
                    });
                    params.add(new SearchCriteria("agentId", ":", agent.getAgentId()));
                    List<JSONObject> details = new ArrayList<>();
                    specificationsDao.getAgentRequest(params).forEach(agentFloat -> {
                        JSONObject detail = new JSONObject();
                        detail.put("TransRef", agentFloat.getTransRef());
                        detail.put("Amount", agentFloat.getAmount());
                        detail.put("Comment", agentFloat.getComment());
                        detail.put("Status", agentFloat.getStatus());
                        detail.put("ReviewComment", agentFloat.getReview());
                        detail.put("RequestDate", agentFloat.getRequestDate());
                        detail.put("ReviewDate", agentFloat.getReviewDate());
                        detail.put("ProcessedDate", agentFloat.getProcessedDate());
                        details.add(detail);
                    });
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "success")
                            .put("details", details)
                            .put("statusMessage", "Request Successful");
                }, () -> {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "Agent Does not exists")
                            .put("statusMessage", "Agent Does not exists");
                });
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/payment-summary"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getPaymentSummary() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(agent -> {
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
        agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(agent -> {
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

    @RequestMapping(value = {"/float-balance"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getFloatBalance() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();
        agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(agent -> {
                    String balance = "0.00";
                    balance = globalMethods.getAccountBalance(agent.getTillNumber());
                    List<JSONObject> details = transactionProcessingService.getPayment(responseMap);
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
