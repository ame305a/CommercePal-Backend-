package com.commerce.pal.backend.controller.messenger;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.users.MessengerService;
import com.commerce.pal.backend.repo.user.MessengerRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
@RequestMapping({"/prime/api/v1/messenger"})
@SuppressWarnings("Duplicates")
public class MessengerController {

    @Autowired
    private UploadService uploadService;

    private final GlobalMethods globalMethods;
    private final MessengerService messengerService;
    private final MessengerRepository messengerRepository;

    @Autowired
    public MessengerController(GlobalMethods globalMethods,
                               MessengerService messengerService,
                               MessengerRepository messengerRepository) {
        this.globalMethods = globalMethods;
        this.messengerService = messengerService;
        this.messengerRepository = messengerRepository;
    }

    @RequestMapping(value = "/update-detail", method = RequestMethod.POST)
    public ResponseEntity<?> updateDetails(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            messengerRepository.findMessengerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(messenger -> {
                        request.put("ownerType", messenger.getOwnerType());
                        request.put("ownerId", messenger.getOwnerId().toString());
                        responseMap.set(messengerService.updateMessenger(String.valueOf(messenger.getMessengerId()), request));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Messenger Does not exists")
                                .put("statusMessage", "Messenger Does not exists");
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
            messengerRepository.findMessengerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(messenger -> {
                        String imageFileUrl = uploadService.uploadFileAlone(multipartFile, "Web", "MESSENGER");
                        responseMap.set(messengerService.uploadDocs(String.valueOf(messenger.getMessengerId()), fileType, imageFileUrl));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Messenger Does not exists")
                                .put("statusMessage", "Messenger Does not exists");
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

            messengerRepository.findMessengerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(messenger -> {
                        messenger.setRegionId(reqBody.has("regionId") ? reqBody.getInt("regionId") : messenger.getRegionId());
                        messenger.setCountry(reqBody.has("country") ? reqBody.getString("country") : messenger.getCountry());
                        messenger.setCity(reqBody.has("city") ? reqBody.getString("city") : messenger.getCity());
                        messenger.setServiceCodeId(reqBody.has("serviceCodeId") ? reqBody.getInt("serviceCodeId") : messenger.getServiceCodeId());
                        messenger.setPhysicalAddress(reqBody.has("physicalAddress") ? reqBody.getString("physicalAddress") : messenger.getPhysicalAddress());
                        messenger.setLatitude(reqBody.has("latitude") ? reqBody.getString("latitude") : messenger.getLatitude());
                        messenger.setLongitude(reqBody.has("longitude") ? reqBody.getString("longitude") : messenger.getLongitude());
                        messengerRepository.save(messenger);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "MERCHANT DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-delivery-address", method = RequestMethod.GET)
    public ResponseEntity<?> getDeliveryAddress() {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            messengerRepository.findMessengerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(messenger -> {
                        JSONObject payload = new JSONObject();
                        payload.put("country", messenger.getCountry());
                        payload.put("city", messenger.getCity());
                        payload.put("regionId", messenger.getRegionId());
                        payload.put("serviceCodeId", messenger.getServiceCodeId());
                        payload.put("physicalAddress", messenger.getPhysicalAddress());
                        payload.put("latitude", messenger.getLatitude());
                        payload.put("longitude", messenger.getLongitude());
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
            log.log(Level.WARNING, "MERCHANT DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = {"/report"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ResponseEntity<?> getAllMessengers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by messenger Id in ascending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "messengerId";

            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = messengerService.getAllMessengers(page, size, sort, status, city, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "MESSENGER REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }
}
