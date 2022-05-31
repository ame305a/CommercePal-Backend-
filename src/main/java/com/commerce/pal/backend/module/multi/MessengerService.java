package com.commerce.pal.backend.module.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.repo.user.MessengerRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Log
@Service
@SuppressWarnings("Duplicates")
public class MessengerService {

    private final MessengerRepository messengerRepository;

    @Autowired
    public MessengerService(MessengerRepository messengerRepository) {
        this.messengerRepository = messengerRepository;
    }

    public JSONObject uploadDocs(String messengerId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            messengerRepository.findMessengerByMessengerId(Long.valueOf(messengerId))
                    .ifPresentOrElse(messenger -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                        switch (fileType) {
                            case "MessengerPhoto":
                                messenger.setMessengerPhoto(imageFileUrl);
                                break;
                            case "IdNumberImage":
                                messenger.setIdNumberImage(imageFileUrl);
                                break;
                            case "DrivingLicenceImage":
                                messenger.setDrivingLicenceImage(imageFileUrl);
                                break;
                            case "PoliceClearanceImage":
                                messenger.setPoliceClearanceImage(imageFileUrl);
                                break;
                            case "CarrierImage":
                                messenger.setCarrierImage(imageFileUrl);
                                break;
                            case "NextKinPhoto":
                                messenger.setNextKinPhoto(imageFileUrl);
                                break;
                            case "OwnerPhoto":
                                messenger.setOwnerPhoto(imageFileUrl);
                                break;
                            default:
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "The file type does not exist")
                                        .put("statusMessage", "The file type does not exist");
                                break;
                        }
                        messengerRepository.save(messenger);
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

    public JSONObject updateMessenger(String merchantId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            messengerRepository.findMessengerByMessengerId(Long.valueOf(merchantId))
                    .ifPresentOrElse(messenger -> {
                        messenger.setLocation(payload.has("location") ? payload.getString("location") : messenger.getLocation());
                        messenger.setDistrict(payload.has("district") ? payload.getString("district") : messenger.getDistrict());
                        messenger.setCarrierType(payload.has("carrierType") ? payload.getString("carrierType") : messenger.getCarrierType());
                        messenger.setCarrierLicencePlate(payload.has("carrierLicencePlate") ? payload.getString("carrierLicencePlate") : messenger.getCarrierType());
                        messenger.setInsuranceExpiry(payload.has("insuranceExpiry") ? payload.getString("insuranceExpiry") : messenger.getCarrierLicencePlate());
                        messenger.setDrivingLicenceNumber(payload.has("drivingLicenceNumber") ? payload.getString("drivingLicenceNumber") : messenger.getDrivingLicenceNumber());
                        messenger.setOwnerPhoneNumber(payload.has("ownerPhoneNumber") ? payload.getString("ownerPhoneNumber") : messenger.getOwnerPhoneNumber());
                        messenger.setIdNumber(payload.has("idNumber") ? payload.getString("idNumber") : messenger.getIdNumber());
                        messenger.setLanguage(payload.has("language") ? payload.getString("language") : messenger.getLanguage());
                        messenger.setCountry(payload.has("country") ? payload.getString("country") : messenger.getCountry());
                        messenger.setCity(payload.has("city") ? payload.getString("city") : messenger.getCity());
                        messenger.setLongitude(payload.has("longitude") ? payload.getString("longitude") : messenger.getLongitude());
                        messenger.setLatitude(payload.has("latitude") ? payload.getString("latitude") : messenger.getLatitude());
                        messenger.setNextKinNames(payload.has("nextKinNames") ? payload.getString("nextKinNames") : messenger.getNextKinNames());
                        messenger.setNextKinPhone(payload.has("nextKinPhone") ? payload.getString("nextKinPhone") : messenger.getNextKinPhone());
                        messenger.setNextKinEmail(payload.has("nextKinEmail") ? payload.getString("nextKinEmail") : messenger.getNextKinEmail());
                        messenger.setNextKinId(payload.has("nextKinId") ? payload.getString("nextKinId") : messenger.getNextKinId());
                        messenger.setAvailabilityStatus(payload.has("availabilityStatus") ? payload.getInt("availabilityStatus") : messenger.getAvailabilityStatus());
                        messenger.setAvailabilityComment(payload.has("availabilityComment") ? payload.getString("availabilityComment") : messenger.getAvailabilityComment());

                        messenger.setAvailabityUpdateDate(payload.has("availabilityStatus") ? Timestamp.from(Instant.now()) : messenger.getCreatedDate());
                        messengerRepository.save(messenger);
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
            List<JSONObject> messengers = new ArrayList<>();
            messengerRepository.findMessengersByOwnerIdAndOwnerType(req.getInt("ownerId"), req.getString("ownerType"))
                    .forEach(messenger -> {
                        JSONObject payload = getMessengerInfo(messenger.getMessengerId());
                        messengers.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", messengers)
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
            List<JSONObject> messengers = new ArrayList<>();
            messengerRepository.findAll()
                    .forEach(messenger -> {
                        JSONObject payload = getMessengerInfo(messenger.getMessengerId());
                        messengers.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", messengers)
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
            messengerRepository.findMessengerByOwnerIdAndOwnerTypeAndMessengerId(
                            req.getInt("ownerId"), req.getString("ownerType"), req.getLong("userId"))
                    .ifPresentOrElse(messenger -> {
                        JSONObject payload = getMessengerInfo(messenger.getMessengerId());
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

    public JSONObject getMessengerInfo(Long messengerId) {
        JSONObject payload = new JSONObject();
        messengerRepository.findMessengerByMessengerId(messengerId)
                .ifPresent(messenger -> {
                    payload.put("userId", messenger.getMessengerId());
                    payload.put("ownerPhoneNumber", messenger.getOwnerPhoneNumber());
                    payload.put("email", messenger.getEmailAddress());
                    payload.put("ownerType", messenger.getOwnerType());
                    payload.put("idNumber", messenger.getIdNumber());
                    payload.put("idNumberImage", messenger.getIdNumberImage());
                    payload.put("drivingLicenceNumber", messenger.getDrivingLicenceNumber());
                    payload.put("drivingLicenceImage", messenger.getDrivingLicenceImage());
                    payload.put("insuranceExpiry", messenger.getInsuranceExpiry());
                    payload.put("policeClearanceImage", messenger.getPoliceClearanceImage());
                    payload.put("messengerPhoto", messenger.getMessengerPhoto());
                    payload.put("carrierType", messenger.getCarrierType());
                    payload.put("carrierLicencePlate", messenger.getCarrierLicencePlate());
                    payload.put("carrierImage", messenger.getCarrierImage());
                    payload.put("language", messenger.getLanguage());
                    payload.put("country", messenger.getCountry());
                    payload.put("city", messenger.getCity());
                    payload.put("district", messenger.getDistrict());
                    payload.put("location", messenger.getLocation());
                    payload.put("longitude", messenger.getLongitude());
                    payload.put("latitude", messenger.getLatitude());
                    JSONObject nextOfKin = new JSONObject();
                    nextOfKin.put("nextKinNames", messenger.getNextKinNames());
                    nextOfKin.put("nextKinPhone", messenger.getNextKinPhone());
                    nextOfKin.put("nextKinEmail", messenger.getNextKinEmail());
                    nextOfKin.put("nextKinId", messenger.getNextKinId());
                    nextOfKin.put("nextKinPhoto", messenger.getNextKinPhoto());
                    payload.put("nextOfKin", nextOfKin);
                    payload.put("Status", messenger.getStatus().toString());
                });
        return payload;
    }

}
