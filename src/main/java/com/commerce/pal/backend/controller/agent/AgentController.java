package com.commerce.pal.backend.controller.agent;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.users.AgentService;
import com.commerce.pal.backend.repo.user.AgentRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/agent"})
@SuppressWarnings("Duplicates")
public class AgentController {

    @Autowired
    private EmailClient emailClient;
    @Autowired
    private UploadService uploadService;
    private final GlobalMethods globalMethods;
    private final AgentService agentService;
    private final AgentRepository agentRepository;

    @Autowired
    public AgentController(GlobalMethods globalMethods,
                           AgentService agentService,
                           AgentRepository agentRepository) {
        this.globalMethods = globalMethods;
        this.agentService = agentService;
        this.agentRepository = agentRepository;
    }

    @RequestMapping(value = "/accept-terms", method = RequestMethod.POST)
    public ResponseEntity<?> acceptTerms(@RequestBody String req) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        if (agent.getTermsOfServiceStatus().equals(1)) {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", "Merchant has already accepted terms of service")
                                    .put("statusMessage", "Merchant has already accepted terms of service");
                        } else {
                            agent.setTermsOfServiceStatus(1);
                            agent.setTermsOfServiceDate(Timestamp.from(Instant.now()));
                            agent.setStatus(1);
                            agentRepository.save(agent);
                            JSONObject emailBody = new JSONObject();
                            emailBody.put("email", agent.getEmailAddress());
                            emailBody.put("subject", "Terms of Service Agreement");
                            emailBody.put("template", "agent-service-agreement.ftl");
                            emailClient.emailTemplateSender(emailBody);
                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Successful")
                                    .put("statusMessage", "Successful");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Agent Does not exists")
                                .put("statusMessage", "Agent Does not exists");
                    });

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-detail", method = RequestMethod.POST)
    public ResponseEntity<?> updateDetails(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        request.put("ownerType", agent.getOwnerType());
                        request.put("ownerId", agent.getOwnerId().toString());
                        responseMap.set(agentService.updateAgent(String.valueOf(agent.getAgentId()), request));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Agent Does not exists")
                                .put("statusMessage", "Agent Does not exists");
                    });

        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/upload-docs", method = RequestMethod.POST)
    public ResponseEntity<?> uploadUserDocs(@RequestPart(value = "file") MultipartFile multipartFile,
                                            @RequestPart(value = "fileType") String fileType) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        String imageFileUrl = uploadService.uploadFileAlone(multipartFile, "Web", "AGENT");
                        responseMap.set(agentService.uploadDocs(String.valueOf(agent.getAgentId()), fileType, imageFileUrl));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "agent Does not exists")
                                .put("statusMessage", "agent Does not exists");
                    });
        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> updateDeliveryAddress(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();

            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        agent.setRegionId(reqBody.has("regionId") ? reqBody.getInt("regionId") : agent.getRegionId());
                        agent.setCountry(reqBody.has("country") ? reqBody.getString("country") : agent.getCountry());
                        agent.setCity(reqBody.has("city") ? reqBody.getInt("city") : agent.getCity());
                        agent.setServiceCodeId(reqBody.has("serviceCodeId") ? reqBody.getInt("serviceCodeId") : agent.getServiceCodeId());
                        agent.setPhysicalAddress(reqBody.has("physicalAddress") ? reqBody.getString("physicalAddress") : agent.getPhysicalAddress());
                        agent.setLatitude(reqBody.has("latitude") ? reqBody.getString("latitude") : agent.getLatitude());
                        agent.setLongitude(reqBody.has("longitude") ? reqBody.getString("longitude") : agent.getLongitude());
                        agentRepository.save(agent);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> getDeliveryAddress() {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            agentRepository.findAgentByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(agent -> {
                        JSONObject payload = new JSONObject();
                        payload.put("country", agent.getCountry());
                        payload.put("city", agent.getCity());
                        payload.put("regionId", agent.getRegionId());
                        payload.put("serviceCodeId", agent.getServiceCodeId());
                        payload.put("physicalAddress", agent.getPhysicalAddress());
                        payload.put("latitude", agent.getLatitude());
                        payload.put("longitude", agent.getLongitude());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("data", payload)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }


    @GetMapping(value = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer city,
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by agent name in ascending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "AgentName";

            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = agentService.getAllAgents(page, size, sort, status, city, regionId, searchKeyword, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "AGENT REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }

}
