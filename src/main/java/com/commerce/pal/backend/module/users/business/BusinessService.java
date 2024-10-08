package com.commerce.pal.backend.module.users.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.user.business.Business;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
                        business.setCity(payload.has("city") ? payload.getInt("city") : business.getCity());
                        business.setLongitude(payload.has("longitude") ? payload.getString("longitude") : business.getLongitude());
                        business.setLatitude(payload.has("latitude") ? payload.getString("latitude") : business.getLatitude());
                        business.setBusinessSector(payload.has("businessSector") ? payload.getInt("businessSector") : business.getBusinessSector());
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
            List<JSONObject> businesses = new ArrayList<>();
            businessRepository.findBusinessByOwnerIdAndOwnerType(req.getInt("ownerId"), req.getString("ownerType"))
                    .forEach(business -> {
                        JSONObject payload = getBusinessInfo(business.getBusinessId());
                        businesses.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", businesses)
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
                        payload.get().put("businessSector", business.getBusinessSector());
                        payload.get().put("businessLicense", business.getCommercialCertNo());
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

    public JSONObject getCollateralBusiness(Integer finance) {
        JSONObject responseMap = new JSONObject();
        try {
            List<JSONObject> businesss = new ArrayList<>();
            businessRepository.findBusinessByFinancialInstitution(finance)
                    .forEach(business -> {
                        JSONObject payload = getBusinessInfo(business.getBusinessId());
                        businesss.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", businesss)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", e.getMessage());
        }
        return responseMap;
    }


    //Retrieves a paginated list of Business for report with support for sorting, filtering, searching, and date range.
    public JSONObject getAllBusiness(
            int page,
            int size,
            Sort sort,
            Integer status,
            Integer city,
            String searchKeyword,
            Timestamp startDate,
            Timestamp endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Business> businessPage = businessRepository.findByFilterAndDateAndStatus(searchKeyword, startDate, endDate, status, city, pageable);

        List<JSONObject> businesses = new ArrayList<>();
        businessPage.getContent().stream()
                .forEach(business -> {
                    JSONObject detail = new JSONObject();

                    detail.put("OwnerType", business.getOwnerType());
                    detail.put("BusinessName", business.getBusinessName());
                    detail.put("BusinessSector", business.getBusinessSector());
                    detail.put("Status", business.getStatus());
                    detail.put("City", business.getCity());
                    detail.put("Location", business.getLocation());
                    detail.put("PhysicalAddress", business.getPhysicalAddress());
                    detail.put("VerifiedBy", business.getVerifiedBy());
                    detail.put("VerifiedDate", business.getVerifiedDate());
                    detail.put("ActivatedBy", business.getActivatedBy());
                    detail.put("ActivatedDate", business.getActivatedDate());
                    businesses.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", businessPage.getNumber())
                .put("pageSize", businessPage.getSize())
                .put("totalElements", businessPage.getTotalElements())
                .put("totalPages", businessPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("business", businesses).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Business Passed")
                .put("statusMessage", "Business Passed")
                .put("data", data);

        return response;
    }

}
