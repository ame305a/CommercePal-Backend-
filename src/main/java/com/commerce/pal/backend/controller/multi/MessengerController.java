package com.commerce.pal.backend.controller.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.multi.MessengerService;
import com.commerce.pal.backend.repo.user.MessengerRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.atomic.AtomicReference;

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
}
