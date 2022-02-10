package com.commerce.pal.backend.module;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.user.DistributorRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log
@Service
@SuppressWarnings("Duplicates")
public class DistributorService {
    private final DistributorRepository distributorRepository;

    @Autowired
    public DistributorService(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    public JSONObject uploadDocs(String distributorId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            distributorRepository.findDistributorByDistributorId(Long.valueOf(distributorId))
                    .ifPresentOrElse(distributor -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                        switch (fileType) {
                            case "IdImage":
                                distributor.setIdImage(imageFileUrl);
                                break;
                            case "PhotoImage":
                                distributor.setPhotoImage(imageFileUrl);
                                break;
                            default:
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "The file type does not exist")
                                        .put("statusMessage", "The file type does not exist");
                                break;
                        }
                        distributorRepository.save(distributor);
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

    public JSONObject updateDistributor(String distributorId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            distributorRepository.findDistributorByDistributorId(Long.valueOf(distributorId))
                    .ifPresentOrElse(distributor -> {
                        distributor.setDistributorName(payload.has("distributorName") ? payload.getString("distributorName") : distributor.getDistributorName());
                        distributor.setDistributorType(payload.has("distributorType") ? payload.getString("distributorType") : distributor.getDistributorType());
                        distributor.setDistrict(payload.has("district") ? payload.getString("district") : distributor.getDistrict());
                        distributor.setLocation(payload.has("location") ? payload.getString("location") : distributor.getLocation());
                        distributor.setCountry(payload.has("country") ? payload.getString("country") : distributor.getCountry());
                        distributor.setCity(payload.has("city") ? payload.getString("city") : distributor.getCity());
                        distributor.setIdNumber(payload.has("idNumber") ? payload.getString("idNumber") : distributor.getIdNumber());
                        distributorRepository.save(distributor);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");

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

    public JSONObject getUsers(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            List<JSONObject> distributors = new ArrayList<>();
            distributorRepository.findAll()
                    .forEach(distributor -> {
                        JSONObject payload = getDistributorInfo(distributor.getDistributorId());
                        distributors.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", distributors)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getUser(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            distributorRepository.findDistributorByDistributorId(req.getLong("userId"))
                    .ifPresentOrElse(distributor -> {
                        JSONObject payload = getDistributorInfo(distributor.getDistributorId());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("data", payload)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Merchant does not exist")
                                .put("statusMessage", "Merchant does not exist");
                    });

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getDistributorInfo(Long distributorId) {
        JSONObject payload = new JSONObject();
        distributorRepository.findDistributorByDistributorId(distributorId)
                .ifPresent(distributor -> {
                    payload.put("userId", distributor.getDistributorId());
                    payload.put("ownerPhoneNumber", distributor.getPhoneNumber());
                    payload.put("email", distributor.getEmailAddress());
                    payload.put("name", distributor.getDistributorName());
                    payload.put("distributorType", distributor.getDistributorType());
                    payload.put("idImage", distributor.getIdImage());
                    payload.put("photoImage", distributor.getPhotoImage());
                    payload.put("district", distributor.getDistrict());
                    payload.put("location", distributor.getLocation());
                    payload.put("idNumber", distributor.getIdNumber());
                    payload.put("country", distributor.getCountry());
                    payload.put("city", distributor.getCity());
                    payload.put("canRegAgent", distributor.getCanRegAgent());
                    payload.put("canRegMerchant", distributor.getCanRegMerchant());
                    payload.put("canRegBusiness", distributor.getCanRegBusiness());
                    payload.put("Status", distributor.getStatus().toString());
                });
        return payload;
    }
}
