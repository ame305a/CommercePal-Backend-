package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.transaction.AgentFloat;
import com.commerce.pal.backend.module.transaction.AccountService;
import com.commerce.pal.backend.module.transaction.AgentFloatService;
import com.commerce.pal.backend.repo.transaction.AgentFloatRepository;
import com.commerce.pal.backend.repo.user.AgentRepository;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal/transaction"})
@SuppressWarnings("Duplicates")
public class TransactionController {
    private final GlobalMethods globalMethods;
    private final AccountService accountService;
    private final AgentRepository agentRepository;
    private final AgentFloatRepository agentFloatRepository;
    private final AgentFloatService agentFloatService;

    @Autowired
    public TransactionController(GlobalMethods globalMethods,
                                 AccountService accountService,
                                 AgentRepository agentRepository,
                                 AgentFloatRepository agentFloatRepository, AgentFloatService agentFloatService) {
        this.globalMethods = globalMethods;
        this.accountService = accountService;
        this.agentRepository = agentRepository;
        this.agentFloatRepository = agentFloatRepository;
        this.agentFloatService = agentFloatService;
    }

    @RequestMapping(value = {"/float-request"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getFloatRequest(
            @RequestParam("status") Optional<String> status,
            @RequestParam("transRef") Optional<String> transRef) {
        JSONObject responseMap = new JSONObject();

        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
        transRef.ifPresent(value -> {
            params.add(new SearchCriteria("transRef", ":", value));
        });
        List<JSONObject> details = new ArrayList<>();
        agentFloatRepository.findAgentFloatsByStatusOrderByRequestDate(0)
                .forEach(agentFloat -> {
                    JSONObject detail = new JSONObject();
                    detail.put("AgentId", agentFloat.getAgentId());
                    agentRepository.findAgentByAgentId(agentFloat.getAgentId())
                            .ifPresentOrElse(agent -> {
                                detail.put("AgentName", agent.getAgentName());
                            }, () -> {
                                detail.put("AgentId", "Agent Names (InValid)");
                            });
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

        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = "/approve-decline-float", method = RequestMethod.POST)
    public ResponseEntity<?> approveDeclineFloat(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            agentFloatRepository.findById(request.getInt("RequestId"))
                    .ifPresentOrElse(agentFloat -> {
                        agentFloat.setStatus(request.getInt("Status"));
                        agentFloat.setReviewedBy(1);
                        agentFloat.setReview(request.getString("Review"));
                        agentFloat.setReviewDate(Timestamp.from(Instant.now()));
                        agentFloat.setTransRef("PENDING");
                        agentFloat.setProcessedDate(Timestamp.from(Instant.now()));
                        agentFloatRepository.save(agentFloat);
                        if (Integer.valueOf(request.getInt("Status")).equals(3)) {
                            agentRepository.findAgentByAgentId(agentFloat.getAgentId())
                                    .ifPresentOrElse(agent -> {
                                        JSONObject floatRq = new JSONObject();
                                        floatRq.put("TransRef", globalMethods.generateTrans());
                                        floatRq.put("Account", agent.getTillNumber());
                                        floatRq.put("Currency", "ETB");
                                        floatRq.put("Amount", request.getBigDecimal("Amount").toString());

                                        JSONObject payRes = accountService.processAgentFloat(floatRq);
                                        agentFloat.setTransRef(floatRq.getString("TransRef"));
                                        agentFloat.setProcessedDate(Timestamp.from(Instant.now()));
                                        agentFloatRepository.save(agentFloat);
                                        if (payRes.getString("TransactionStatus").equals("0")) {
                                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                                    .put("balance", payRes.getString("Balance"))
                                                    .put("statusDescription", "Successful")
                                                    .put("statusMessage", "Successful");
                                        } else {
                                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                                    .put("statusDescription", "Failed")
                                                    .put("statusMessage", "Failed");
                                        }
                                    }, () -> {
                                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                                .put("statusDescription", "Failed")
                                                .put("statusMessage", "Failed");
                                    });
                        } else {
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Success")
                                    .put("statusMessage", "Success");
                        }
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

    @RequestMapping(value = "/assign-float", method = RequestMethod.POST)
    public ResponseEntity<?> postAgentFloat(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            agentRepository.findAgentByAgentId(request.getLong("AgentId"))
                    .ifPresentOrElse(agent -> {
                        AgentFloat agentFloat = new AgentFloat();
                        agentFloat.setAgentId(agent.getAgentId());
                        agentFloat.setAmount(request.getDouble("Amount"));
                        agentFloat.setComment(request.getString("Comment"));
                        agentFloat.setStatus(3);
                        agentFloat.setRequestDate(Timestamp.from(Instant.now()));
                        agentFloat.setReviewedBy(1);
                        agentFloat.setReview(request.getString("Comment"));
                        agentFloat.setReviewDate(Timestamp.from(Instant.now()));
                        agentFloat.setTransRef("PENDING");
                        agentFloat.setProcessedDate(Timestamp.from(Instant.now()));
                        agentFloatRepository.save(agentFloat);

                        JSONObject floatRq = new JSONObject();
                        floatRq.put("TransRef", globalMethods.generateTrans());
                        floatRq.put("Account", agent.getTillNumber());
                        floatRq.put("Currency", "ETB");
                        floatRq.put("Amount", request.getBigDecimal("Amount").toString());

                        JSONObject payRes = accountService.processAgentFloat(floatRq);
                        agentFloat.setTransRef(floatRq.getString("TransRef"));
                        agentFloat.setProcessedDate(Timestamp.from(Instant.now()));
                        agentFloatRepository.save(agentFloat);

                        if (payRes.getString("TransactionStatus").equals("0")) {
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("balance", payRes.getString("Balance"))
                                    .put("statusDescription", "Successful")
                                    .put("statusMessage", "Successful");
                        } else {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Failed")
                                    .put("statusMessage", "Failed");
                        }
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

    @RequestMapping(value = {"/agent-float/report"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ResponseEntity<?> getAllAgentFloats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by requestDate in descending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "requestDate";

            // Default to descending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.DESC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
                direction = Sort.Direction.ASC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = agentFloatService.getAllAgentFloats(page, size, sort, status, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "AGENT FLOAT REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }

}
