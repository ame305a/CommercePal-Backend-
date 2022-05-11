package com.commerce.pal.backend.controller.agent;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.multi.AgentService;
import com.commerce.pal.backend.repo.user.AgentRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

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
}
