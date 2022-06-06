package com.commerce.pal.backend.module.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.user.BusinessRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class BusinessService {
    private final GlobalMethods globalMethods;
    private final BusinessRepository businessRepository;

    @Autowired
    public BusinessService(GlobalMethods globalMethods,
                           BusinessRepository businessRepository) {
        this.globalMethods = globalMethods;
        this.businessRepository = businessRepository;
    }

    public JSONObject uploadDocs(String businessId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(Long.valueOf(businessId))
                    .ifPresentOrElse(business -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                        switch (fileType) {
                            case "ShopImage":
                                business.setShopImage(imageFileUrl);
                                break;
                            case "CommercialCertImage":
                                business.setCommercialCertImage(imageFileUrl);
                                break;
                            case "TillNumberImage":
                                business.setTillNumberImage(imageFileUrl);
                                break;
                            case "OwnerPhoto":
                                business.setOwnerPhoto(imageFileUrl);
                                break;
                            case "BusinessRegistrationPhoto":
                                business.setBusinessRegistrationPhoto(imageFileUrl);
                                break;
                            case "TaxPhoto":
                                business.setTaxPhoto(imageFileUrl);
                                break;
                            default:
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "The file type does not exist")
                                        .put("statusMessage", "The file type does not exist");
                                break;
                        }
                        businessRepository.save(business);
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

    public JSONObject updateBusiness(String businessId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(Long.valueOf(businessId))
                    .ifPresentOrElse(business -> {
                        business.setBusinessPhoneNumber(payload.has("businessPhoneNumber") ? payload.getString("businessPhoneNumber") : business.getBusinessPhoneNumber());
                        business.setBusinessName(payload.has("businessName") ? payload.getString("businessName") : business.getBusinessName());
                        business.setCommercialCertNo(payload.has("commercialCertNo") ? payload.getString("commercialCertNo") : business.getCommercialCertNo());
                        business.setDistrict(payload.has("district") ? payload.getString("district") : business.getDistrict());
                        business.setLanguage(payload.has("language") ? payload.getString("language") : business.getLanguage());
                        business.setCountry(payload.has("country") ? payload.getString("country") : business.getCountry());
                        business.setCity(payload.has("city") ? payload.getString("city") : business.getCity());
                        business.setLongitude(payload.has("longitude") ? payload.getString("longitude") : business.getLongitude());
                        business.setLatitude(payload.has("latitude") ? payload.getString("latitude") : business.getLatitude());
                        businessRepository.save(business);

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
            List<JSONObject> businesss = new ArrayList<>();
            businessRepository.findBusinessByOwnerIdAndOwnerType(req.getInt("ownerId"), req.getString("ownerType"))
                    .forEach(business -> {
                        JSONObject payload = getBusinessInfo(business.getBusinessId());
                        businesss.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", businesss)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }


    public JSONObject getAllUsers(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            List<JSONObject> businesss = new ArrayList<>();
            businessRepository.findAll()
                    .forEach(business -> {
                        JSONObject payload = getBusinessInfo(business.getBusinessId());
                        businesss.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", businesss)
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
            businessRepository.findBusinessByOwnerIdAndOwnerTypeAndBusinessId(
                            req.getInt("ownerId"), req.getString("ownerType"), req.getLong("userId"))
                    .ifPresentOrElse(business -> {
                        JSONObject payload = getBusinessInfo(business.getBusinessId());
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
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public Integer countUser(JSONObject payload) {
        Integer count = 0;
        count = businessRepository.countByOwnerIdAndOwnerType(payload.getInt("ownerId"), payload.getString("ownerType"));
        return count;
    }

    public JSONObject getBusinessInfo(Long businessId) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        businessRepository.findBusinessByBusinessId(businessId)
                .ifPresent(business -> {
                    try {
                        payload.get().put("userId", business.getBusinessId());
                        payload.get().put("ownerType", business.getOwnerType());
                        payload.get().put("ownerPhoneNumber", business.getOwnerPhoneNumber());
                        payload.get().put("businessPhoneNumber", business.getBusinessPhoneNumber());
                        payload.get().put("email", business.getEmailAddress());
                        payload.get().put("name", business.getBusinessName());
                        payload.get().put("commercialCertNo", business.getCommercialCertNo());
                        payload.get().put("tillNumber", business.getTillNumber());
                        payload.get().put("language", business.getLanguage());
                        payload.get().put("country", business.getCountry());
                        payload.get().put("city", business.getCity());
                        payload.get().put("longitude", business.getLongitude());
                        payload.get().put("latitude", business.getLatitude());

                        payload.get().put("OwnerPhoto", business.getOwnerPhoto());
                        payload.get().put("BusinessRegistrationPhoto", business.getBusinessRegistrationPhoto());
                        payload.get().put("TaxPhoto", business.getTaxPhoto());
                        payload.get().put("TillNumberImage", business.getTillNumberImage());
                        payload.get().put("CommercialCertImage", business.getCommercialCertImage());
                        payload.get().put("ShopImage", business.getShopImage());

                        payload.get().put("Status", business.getStatus().toString());
                        payload.get().put("termOfService", business.getTermsOfServiceStatus());
                        JSONObject customer = globalMethods.getMultiUserCustomer(business.getEmailAddress());
                        payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
                    } catch (Exception ex) {
                        log.log(Level.WARNING, ex.getMessage());
                    }
                });
        return payload.get();
    }

}
