package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.user.MessengerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class MessengerService {
    private final GlobalMethods globalMethods;
    private final MessengerRepository messengerRepository;

    @Autowired
    public MessengerService(GlobalMethods globalMethods,
                            MessengerRepository messengerRepository) {
        this.globalMethods = globalMethods;
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
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        messengerRepository.findMessengerByMessengerId(messengerId)
                .ifPresent(messenger -> {
                    try {
                        payload.get() .put("userId", messenger.getMessengerId());
                        payload.get().put("ownerPhoneNumber", messenger.getOwnerPhoneNumber());
                        payload.get().put("email", messenger.getEmailAddress());
                        payload.get().put("ownerType", messenger.getOwnerType());
                        payload.get().put("idNumber", messenger.getIdNumber());
                        payload.get().put("idNumberImage", messenger.getIdNumberImage());
                        payload.get().put("drivingLicenceNumber", messenger.getDrivingLicenceNumber());
                        payload.get().put("drivingLicenceImage", messenger.getDrivingLicenceImage());
                        payload.get().put("insuranceExpiry", messenger.getInsuranceExpiry());
                        payload.get().put("policeClearanceImage", messenger.getPoliceClearanceImage());
                        payload.get().put("messengerPhoto", messenger.getMessengerPhoto());
                        payload.get().put("carrierType", messenger.getCarrierType());
                        payload.get().put("carrierLicencePlate", messenger.getCarrierLicencePlate());
                        payload.get().put("carrierImage", messenger.getCarrierImage());
                        payload.get().put("language", messenger.getLanguage());
                        payload.get().put("country", messenger.getCountry());
                        payload.get().put("city", messenger.getCity());
                        payload.get().put("district", messenger.getDistrict());
                        payload.get().put("location", messenger.getLocation());
                        payload.get().put("longitude", messenger.getLongitude());
                        payload.get().put("latitude", messenger.getLatitude());
                        JSONObject nextOfKin = new JSONObject();
                        nextOfKin.put("nextKinNames", messenger.getNextKinNames());
                        nextOfKin.put("nextKinPhone", messenger.getNextKinPhone());
                        nextOfKin.put("nextKinEmail", messenger.getNextKinEmail());
                        nextOfKin.put("nextKinId", messenger.getNextKinId());
                        nextOfKin.put("nextKinPhoto", messenger.getNextKinPhoto());
                        payload.get().put("nextOfKin", nextOfKin);
                        JSONObject customer = globalMethods.getMultiUserCustomer(messenger.getEmailAddress());
                        payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
                        payload.get().put("Status", messenger.getStatus().toString());
                    } catch (Exception ex) {
                        log.log(Level.WARNING, ex.getMessage());
                    }
                });
        return payload.get();
    }

}
