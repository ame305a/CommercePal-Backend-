package com.commerce.pal.backend.controller.multi;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.DistributorService;
import com.commerce.pal.backend.module.MultiUserService;
import com.commerce.pal.backend.repo.user.DistributorRepository;
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
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/distributor"})
@SuppressWarnings("Duplicates")
public class DistributorController {
    @Autowired
    private UploadService uploadService;

    private final GlobalMethods globalMethods;
    private final MultiUserService multiUserService;
    private final DistributorService distributorService;
    private final DistributorRepository distributorRepository;

    @Autowired
    public DistributorController(GlobalMethods globalMethods,
                                 MultiUserService multiUserService,
                                 DistributorService distributorService,
                                 DistributorRepository distributorRepository) {
        this.globalMethods = globalMethods;
        this.multiUserService = multiUserService;
        this.distributorService = distributorService;
        this.distributorRepository = distributorRepository;
    }

    @RequestMapping(value = "/user-registration", method = RequestMethod.POST)
    public ResponseEntity<?> userRegistration(@RequestBody String registration) {
        log.log(Level.INFO, "Registration Payload : " + registration);
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                JSONObject request = new JSONObject(registration);
                request.put("ownerType", "DISTRIBUTOR");
                request.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                responseMap = multiUserService.userRegistration(request);
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
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

            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        userType, userid)) {
                    JSONObject request = new JSONObject();
                    request.put("ownerType", "DISTRIBUTOR");
                    request.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                    responseMap = multiUserService.uploadDocs(multipartFile, userType, userid, fileType);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to the distributor")
                            .put("statusMessage", "he User does not belong to the distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
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
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                request.put("ownerType", "DISTRIBUTOR");
                request.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                responseMap = multiUserService.changeAccountStatus(request);
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
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
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        request.getString("userType"), request.getString("userid"))) {
                    request.put("ownerType", "DISTRIBUTOR");
                    request.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                    responseMap = multiUserService.updateUser(request);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to the distributor")
                            .put("statusMessage", "he User does not belong to the distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());

    }

    @RequestMapping(value = "/get-dashboard", method = RequestMethod.GET)
    public ResponseEntity<?> getDashboard() {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                JSONObject payload = new JSONObject();
                payload.put("ownerType", "DISTRIBUTOR");
                payload.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                payload.put("userType", "MERCHANT");
                Integer merchantCount = multiUserService.countUsers(payload);
                payload.put("userType", "BUSINESS");
                Integer businessCount = multiUserService.countUsers(payload);
                payload.put("userType", "AGENT");
                Integer agentCount = multiUserService.countUsers(payload);

                JSONObject dashCount = new JSONObject();
                dashCount.put("Merchant", merchantCount)
                        .put("Agent", agentCount)
                        .put("Business", businessCount)
                        .put("Total", businessCount + agentCount + merchantCount);

                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("data", dashCount)
                        .put("statusDescription", "success")
                        .put("statusMessage", "Request Successful");
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
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
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                JSONObject payload = new JSONObject();
                payload.put("userType", userType);
                payload.put("ownerType", "DISTRIBUTOR");
                payload.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                responseMap = multiUserService.getUsers(payload);
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }
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
            LoginValidation user = globalMethods.fetchUserDetails();
            if (!globalMethods.getDistributorId(user.getEmailAddress()).equals(0)) {
                if (globalMethods.validateDistUser(
                        globalMethods.getDistributorId(user.getEmailAddress()),
                        userType, userId)) {
                    JSONObject payload = new JSONObject();
                    payload.put("userType", userType);
                    payload.put("userId", userId);
                    payload.put("ownerType", "DISTRIBUTOR");
                    payload.put("ownerId", globalMethods.getDistributorId(user.getEmailAddress()).toString());
                    responseMap = multiUserService.getUser(payload);
                } else {
                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                            .put("statusDescription", "The User does not belong to the distributor")
                            .put("statusMessage", "he User does not belong to the distributor");
                }
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The User is not a distributor")
                        .put("statusMessage", "The User is not a distributor");
            }

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());

    }

    @RequestMapping(value = "/update-detail", method = RequestMethod.POST)
    public ResponseEntity<?> updateDetails(@RequestBody String req) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            JSONObject request = new JSONObject(req);
            LoginValidation user = globalMethods.fetchUserDetails();
            distributorRepository.findDistributorByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(distributor -> {
                        responseMap.set(distributorService.updateDistributor(String.valueOf(distributor.getDistributorId()), request));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "distributor Does not exists")
                                .put("statusMessage", "distributor Does not exists");
                    });

        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/upload-my-doc", method = RequestMethod.POST)
    public ResponseEntity<?> uploadMyDocs(@RequestPart(value = "file") MultipartFile multipartFile,
                                          @RequestPart(value = "fileType") String fileType) {
        AtomicReference<JSONObject> responseMap = new AtomicReference<>(new JSONObject());
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            distributorRepository.findDistributorByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(distributor -> {
                        String imageFileUrl = uploadService.uploadFileAlone(multipartFile, "Web", "DISTRIBUTOR");
                        responseMap.set(distributorService.uploadDocs(String.valueOf(distributor.getDistributorId()), fileType, imageFileUrl));
                    }, () -> {
                        responseMap.get().put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "distributor Does not exists")
                                .put("statusMessage", "distributor Does not exists");
                    });
        } catch (Exception e) {
            responseMap.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
