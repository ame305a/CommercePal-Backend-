package com.commerce.pal.backend.module.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.user.AgentRepository;
import com.commerce.pal.backend.repo.user.BusinessRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class AgentService {

    private final AgentRepository agentRepository;

    @Autowired
    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public JSONObject uploadDocs(String agentId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            agentRepository.findAgentByAgentId(Long.valueOf(agentId))
                    .ifPresentOrElse(agent -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                        switch (fileType) {
                            case "ShopImage":
                                agent.setShopImage(imageFileUrl);
                                break;
                            case "CommercialCertImage":
                                agent.setCommercialCertImage(imageFileUrl);
                                break;
                            case "TillNumberImage":
                                agent.setTillNumberImage(imageFileUrl);
                                break;
                            case "OwnerPhoto":
                                agent.setOwnerPhoto(imageFileUrl);
                                break;
                            case "BusinessRegistrationPhoto":
                                agent.setBusinessRegistrationPhoto(imageFileUrl);
                                break;
                            case "TaxPhoto":
                                agent.setTaxPhoto(imageFileUrl);
                                break;
                            default:
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "The file type does not exist")
                                        .put("statusMessage", "The file type does not exist");
                                break;
                        }
                        agentRepository.save(agent);
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The User does not exist")
                                .put("statusMessage", "The User does not exist");
                    });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject updateAgent(String agentId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            agentRepository.findAgentByAgentId(Long.valueOf(agentId))
                    .ifPresentOrElse(business -> {
                        business.setBusinessPhoneNumber(payload.has("businessPhoneNumber") ? payload.getString("businessPhoneNumber") : business.getBusinessPhoneNumber());
                        business.setAgentName(payload.has("agentName") ? payload.getString("agentName") : business.getAgentName());
                        business.setCommercialCertNo(payload.has("commercialCertNo") ? payload.getString("commercialCertNo") : business.getCommercialCertNo());
                        business.setDistrict(payload.has("district") ? payload.getString("district") : business.getDistrict());
                        business.setLanguage(payload.has("language") ? payload.getString("language") : business.getLanguage());
                        business.setCountry(payload.has("country") ? payload.getString("country") : business.getCountry());
                        business.setCity(payload.has("city") ? payload.getString("city") : business.getCity());
                        business.setLongitude(payload.has("longitude") ? payload.getString("longitude") : business.getLongitude());
                        business.setLatitude(payload.has("latitude") ? payload.getString("latitude") : business.getLatitude());
                        agentRepository.save(business);

                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");

                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The User does not exist")
                                .put("statusMessage", "The User does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "ERROR IN UPDATING : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getUsers(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            List<JSONObject> agents = new ArrayList<>();
            agentRepository.findAgentsByOwnerIdAndOwnerType(req.getInt("ownerId"), req.getString("ownerType"))
                    .forEach(agent -> {
                        JSONObject payload = getAgentInfo(agent.getAgentId());
                        agents.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", agents)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            log.log(Level.WARNING, "ERROR IN GET USERS : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getUser(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            agentRepository.findAgentByOwnerIdAndOwnerTypeAndAgentId(
                            req.getInt("ownerId"), req.getString("ownerType"), req.getLong("userId"))
                    .ifPresentOrElse(agent -> {
                        JSONObject payload = getAgentInfo(agent.getAgentId());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("data", payload)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, "ERROR IN GET USER : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public Integer countUser(JSONObject payload) {
        Integer count = 0;
        count = agentRepository.countByOwnerIdAndOwnerType(payload.getInt("ownerId"), payload.getString("ownerType"));
        return count;
    }

    public JSONObject getAgentInfo(Long agentId) {
        JSONObject payload = new JSONObject();
        agentRepository.findAgentByAgentId(agentId)
                .ifPresent(agent -> {
                    payload.put("userId", agent.getAgentId());
                    payload.put("ownerType", agent.getOwnerType());
                    payload.put("ownerPhoneNumber", agent.getOwnerPhoneNumber());
                    payload.put("businessPhoneNumber", agent.getBusinessPhoneNumber());
                    payload.put("email", agent.getEmailAddress());
                    payload.put("name", agent.getAgentName());
                    payload.put("commercialCertNo", agent.getCommercialCertNo());
                    payload.put("tillNumber", agent.getTillNumber());
                    payload.put("language", agent.getLanguage());
                    payload.put("country", agent.getCountry());
                    payload.put("city", agent.getCity());
                    payload.put("longitude", agent.getLongitude());
                    payload.put("latitude", agent.getLatitude());
                    payload.put("ownerPhoto", agent.getOwnerPhoto());
                    payload.put("businessRegistrationPhoto", agent.getBusinessRegistrationPhoto());
                    payload.put("taxPhoto", agent.getTaxPhoto());
                    payload.put("Status", agent.getStatus().toString());
                    payload.put("termOfService", agent.getTermsOfServiceStatus());
                });
        return payload;
    }

}
