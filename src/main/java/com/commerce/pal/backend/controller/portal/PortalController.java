package com.commerce.pal.backend.controller.portal;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.MultiUserService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal"})
@SuppressWarnings("Duplicates")
public class PortalController {

    private final MultiUserService multiUserService;

    @Autowired
    public PortalController(MultiUserService multiUserService) {
        this.multiUserService = multiUserService;
    }

    @RequestMapping(value = "/user-registration", method = RequestMethod.POST)
    public ResponseEntity<?> userRegistration(@RequestBody String registration) {
        JSONObject responseMap = new JSONObject();
        log.log(Level.INFO, registration);
        try {
            JSONObject request = new JSONObject(registration);
            request.put("ownerType", "WAREHOUSE");
            request.put("ownerId", "0");
            responseMap = multiUserService.userRegistration(request);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in User Registration : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/upload-docs", method = RequestMethod.POST)
    public ResponseEntity<?> uploadUserDocs(@RequestPart(value = "file") MultipartFile multipartFile,
                                            @RequestPart(value = "fileType") String fileType,
                                            @RequestPart(value = "userType") String userType,
                                            @RequestPart(value = "userId") String userid) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject();
            request.put("ownerType", "WAREHOUSE");
            request.put("ownerId", "0");
            responseMap = multiUserService.uploadDocs(multipartFile, userType, userid, fileType);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/change-acc-status", method = RequestMethod.POST)
    public ResponseEntity<?> changeAccountStatus(@RequestBody String registration) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(registration);
            request.put("ownerType", "WAREHOUSE");
            request.put("ownerId", "0");
            responseMap = multiUserService.changeAccountStatus(request);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());

    }

    @RequestMapping(value = "/user-update", method = RequestMethod.POST)
    public ResponseEntity<?> userUpdate(@RequestBody String payload) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(payload);
            request.put("ownerType", "WAREHOUSE");
            request.put("ownerId", "0");
            responseMap = multiUserService.updateUser(request);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-users", method = RequestMethod.GET)
    public ResponseEntity<?> getUsers(@RequestParam("userType") String userType) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject payload = new JSONObject();
            payload.put("userType", userType);
            payload.put("ownerType", "WAREHOUSE");
            payload.put("ownerId", "0");
            responseMap = multiUserService.getAllUsers(payload);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-user", method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@RequestParam("userType") String userType,
                                     @RequestParam("userId") String userId) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject payload = new JSONObject();
            payload.put("userType", userType);
            payload.put("userId", userId);
            responseMap = multiUserService.getAllUser(payload);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-distributor-users", method = RequestMethod.GET)
    public ResponseEntity<?> getDistributorUsers(@RequestParam("userType") String userType,
                                                 @RequestParam("userId") String userId) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject payload = new JSONObject();
            payload.put("userType", userType);
            payload.put("ownerType", "DISTRIBUTOR");
            payload.put("ownerId", userId);
            responseMap = multiUserService.getUsers(payload);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }


    @RequestMapping(value = "/get-all-users", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUsers(@RequestParam("userType") String userType) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject payload = new JSONObject();
            payload.put("userType", userType);
            responseMap = multiUserService.getAllUsers(payload);
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
