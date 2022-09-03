package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.user.MerchantRepository;
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
public class MerchantService {
    private final GlobalMethods globalMethods;
    private final MerchantRepository merchantRepository;

    @Autowired
    public MerchantService(GlobalMethods globalMethods,
                           MerchantRepository merchantRepository) {
        this.globalMethods = globalMethods;
        this.merchantRepository = merchantRepository;
    }

    public JSONObject uploadDocs(String merchantId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            merchantRepository.findMerchantByMerchantId(Long.valueOf(merchantId))
                    .ifPresentOrElse(merchant -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                        switch (fileType) {
                            case "ShopImage":
                                merchant.setShopImage(imageFileUrl);
                                break;
                            case "CommercialCertImage":
                                merchant.setCommercialCertImage(imageFileUrl);
                                break;
                            case "TillNumberImage":
                                merchant.setTillNumberImage(imageFileUrl);
                                break;
                            case "OwnerPhoto":
                                merchant.setOwnerPhoto(imageFileUrl);
                                break;
                            case "BusinessRegistrationPhoto":
                                merchant.setBusinessRegistrationPhoto(imageFileUrl);
                                break;
                            case "TaxPhoto":
                                merchant.setTaxPhoto(imageFileUrl);
                                break;

                            default:
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "The file type does not exist")
                                        .put("statusMessage", "The file type does not exist");
                                break;
                        }
                        merchantRepository.save(merchant);
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

    public JSONObject updateMerchant(String merchantId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            merchantRepository.findMerchantByMerchantId(Long.valueOf(merchantId))
                    .ifPresentOrElse(merchant -> {
                        merchant.setOwnerPhoneNumber(payload.has("ownerPhoneNumber") ? payload.getString("ownerPhoneNumber") : merchant.getOwnerPhoneNumber());
                        merchant.setMerchantName(payload.has("merchantName") ? payload.getString("merchantName") : merchant.getMerchantName());
                        merchant.setBusinessType(payload.has("businessType") ? payload.getString("businessType") : merchant.getBusinessType());
                        merchant.setBusinessCategory(payload.has("businessCategory") ? payload.getString("businessCategory") : merchant.getBusinessCategory());
                        merchant.setBusinessLicense(payload.has("businessLicense") ? payload.getString("businessLicense") : merchant.getBusinessLicense());
                        merchant.setCommercialCertNo(payload.has("commercialCertNo") ? payload.getString("commercialCertNo") : merchant.getCommercialCertNo());
                        merchant.setTaxNumber(payload.has("taxNumber") ? payload.getString("taxNumber") : merchant.getTaxNumber());
                        merchant.setBankCode(payload.has("bankCode") ? payload.getString("bankCode") : merchant.getBankCode());
                        merchant.setBankAccountNumber(payload.has("bankAccountNumber") ? payload.getString("bankAccountNumber") : merchant.getBankAccountNumber());
                        merchant.setBranch(payload.has("branch") ? payload.getString("branch") : merchant.getBranch());
                        merchant.setLanguage(payload.has("language") ? payload.getString("language") : merchant.getLanguage());
                        merchant.setCountry(payload.has("country") ? payload.getString("country") : merchant.getCountry());
                        merchant.setCity(payload.has("city") ? payload.getInt("city") : merchant.getCity());
                        merchant.setLongitude(payload.has("longitude") ? payload.getString("longitude") : merchant.getLongitude());
                        merchant.setLatitude(payload.has("latitude") ? payload.getString("latitude") : merchant.getLatitude());
                        merchant.setLocation(payload.has("location") ? payload.getString("location") : merchant.getLocation());
                        merchant.setDistrict(payload.has("district") ? payload.getString("district") : merchant.getDistrict());
                        merchantRepository.save(merchant);

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
            List<JSONObject> merchants = new ArrayList<>();
            merchantRepository.findMerchantsByOwnerIdAndOwnerTypeOrderByCreatedDateDesc(req.getInt("ownerId"), req.getString("ownerType"))
                    .forEach(merchant -> {
                        JSONObject payload = getMerchantInfo(merchant.getMerchantId());
                        merchants.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", merchants)
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
            List<JSONObject> merchants = new ArrayList<>();
            merchantRepository.findAll()
                    .forEach(merchant -> {
                        JSONObject payload = getMerchantInfo(merchant.getMerchantId());
                        merchants.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", merchants)
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
            merchantRepository.findMerchantByOwnerIdAndOwnerTypeAndMerchantId(
                            req.getInt("ownerId"), req.getString("ownerType"), req.getLong("userId"))
                    .ifPresentOrElse(merchant -> {
                        JSONObject payload = getMerchantInfo(merchant.getMerchantId());
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

    public Integer countUser(JSONObject payload) {
        Integer count = 0;
        count = merchantRepository.countByOwnerIdAndOwnerType(payload.getInt("ownerId"), payload.getString("ownerType"));
        return count;
    }

    public JSONObject getMerchantInfo(Long merchantId) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        merchantRepository.findMerchantByMerchantId(merchantId)
                .ifPresent(merchant -> {
                    try {
                        payload.get().put("userId", merchant.getMerchantId());
                        payload.get().put("ownerPhoneNumber", merchant.getOwnerPhoneNumber());
                        payload.get().put("ownerType", merchant.getOwnerType());
                        payload.get().put("email", merchant.getEmailAddress());
                        payload.get().put("name", merchant.getMerchantName());
                        payload.get().put("businessType", merchant.getBusinessType());
                        payload.get().put("businessCategory", merchant.getBusinessCategory());
                        payload.get().put("businessLicense", merchant.getBusinessLicense());
                        payload.get().put("commercialCertNo", merchant.getCommercialCertNo());
                        payload.get().put("tillNumber", merchant.getTillNumber());
                        payload.get().put("taxNumber", merchant.getTaxNumber());
                        payload.get().put("bankCode", merchant.getBankCode());
                        payload.get().put("bankAccountNumber", merchant.getBankAccountNumber());
                        payload.get().put("branch", merchant.getBranch());
                        payload.get().put("language", merchant.getLanguage());
                        payload.get().put("country", merchant.getCountry());
                        payload.get().put("city", merchant.getCity());
                        payload.get().put("longitude", merchant.getLongitude());
                        payload.get().put("latitude", merchant.getLatitude());
                        payload.get().put("ownerPhoto", merchant.getOwnerPhoto());
                        payload.get().put("businessRegistrationPhoto", merchant.getBusinessRegistrationPhoto());
                        payload.get().put("taxPhoto", merchant.getTaxPhoto());

                        payload.get().put("OwnerPhoto", merchant.getOwnerPhoto());
                        payload.get().put("BusinessRegistrationPhoto", merchant.getBusinessRegistrationPhoto());
                        payload.get().put("TaxPhoto", merchant.getTaxPhoto());
                        payload.get().put("TillNumberImage", merchant.getTillNumberImage());
                        payload.get().put("CommercialCertImage", merchant.getCommercialCertImage());
                        payload.get().put("ShopImage", merchant.getShopImage());


                        payload.get().put("Status", merchant.getStatus().toString());
                        payload.get().put("termOfService", merchant.getTermsOfServiceStatus());
                        JSONObject customer = globalMethods.getMultiUserCustomer(merchant.getEmailAddress());
                        payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
                    } catch (Exception ex) {
                        log.log(Level.WARNING, ex.getMessage());
                    }
                });
        return payload.get();
    }

    public JSONObject getMerchantAddressInfo(Long merchantId) {
        JSONObject payload = new JSONObject();
        merchantRepository.findMerchantByMerchantId(merchantId)
                .ifPresent(merchant -> {
                    payload.put("phoneNumber", merchant.getOwnerPhoneNumber());
                    payload.put("email", merchant.getEmailAddress());
                    payload.put("name", merchant.getMerchantName());
                    payload.put("country", merchant.getCountry());
                    payload.put("city", merchant.getCity());
                    payload.put("regionId", merchant.getRegionId());
                    payload.put("serviceCodeId", merchant.getServiceCodeId());
                    payload.put("physicalAddress", merchant.getPhysicalAddress());
                    payload.put("latitude", merchant.getLatitude());
                    payload.put("longitude", merchant.getLongitude());
                });
        return payload;
    }


}
