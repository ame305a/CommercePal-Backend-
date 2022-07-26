package com.commerce.pal.backend.module;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.database.RegistrationStoreService;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.module.users.AgentService;
import com.commerce.pal.backend.module.users.business.BusinessService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.module.users.MessengerService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class MultiUserService {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private EmailClient emailClient;
    private final AgentService agentService;
    private final GlobalMethods globalMethods;
    private final BusinessService businessService;
    private final MerchantService merchantService;
    private final MessengerService messengerService;
    private final MerchantRepository merchantRepository;
    private final DistributorService distributorService;
    private final RegistrationStoreService registrationStoreService;

    @Autowired
    public MultiUserService(AgentService agentService,
                            GlobalMethods globalMethods,
                            BusinessService businessService,
                            MerchantService merchantService,
                            MessengerService messengerService,
                            MerchantRepository merchantRepository,
                            DistributorService distributorService,
                            RegistrationStoreService registrationStoreService) {
        this.agentService = agentService;
        this.globalMethods = globalMethods;
        this.businessService = businessService;
        this.merchantService = merchantService;
        this.messengerService = messengerService;
        this.merchantRepository = merchantRepository;
        this.distributorService = distributorService;
        this.registrationStoreService = registrationStoreService;
    }

    public JSONObject userRegistration(JSONObject request) {
        JSONObject responseMap = new JSONObject();
        try {
            String password = globalMethods.generatePassword();
            String passwordHash = bcryptEncoder.encode(password);
            request.put("password", passwordHash);
            switch (request.getString("userType")) {
                case "MERCHANT":
                    responseMap = registrationStoreService.doMerchantRegistration(request);
                    break;
                case "BUSINESS":
                    responseMap = registrationStoreService.doBusinessRegistration(request);
                    break;
                case "DISTRIBUTOR":
                    responseMap = registrationStoreService.doDistributorRegistration(request);
                    break;
                case "AGENT":
                    responseMap = registrationStoreService.doAgentRegistration(request);
                    break;
                case "MESSENGER":
                    responseMap = registrationStoreService.doMessengerRegistration(request);
                    break;
            }

            int exists = responseMap.getInt("exists");
            String userId = globalMethods.getUserId(request.getString("userType"), request.getString("email").trim());

            if (exists == 1) {
                responseMap.put("statusCode", ResponseCodes.REGISTERED)
                        .put("statusDescription", "email address already registered")
                        .put("statusMessage", "registration exists");
            } else if (exists == -1) {
                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("userId", userId)
                        .put("statusDescription", "success. Use current login details")
                        .put("statusMessage", "registration successful. Use current login details");
                String msg = "Registration successful. Use current login details";
                emailClient.emailSender(msg, request.getString("email").trim(), "REGISTRATION");
            } else {
                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("userId", userId)
                        .put("statusDescription", "success")
                        .put("statusMessage", "registration successful");

                String msg = "Welcome to CommercePal! Your " + request.getString("userType") + " Account has been created. " +
                        "Username is " + request.getString("email").trim() + " and password : " + password;

                emailClient.emailSender(msg, request.getString("email").trim(), "REGISTRATION");
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in User Registration : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }


    public JSONObject uploadDocs(MultipartFile multipartFile, String userType, String userID, String fileType) {
        JSONObject responseMap = new JSONObject();
        try {
            String imageFileUrl = uploadService.uploadFileAlone(multipartFile, "Web", userType);

            switch (userType) {
                case "MERCHANT":
                    responseMap = merchantService.uploadDocs(userID, fileType, imageFileUrl);
                    break;
                case "BUSINESS":
                    responseMap = businessService.uploadDocs(userID, fileType, imageFileUrl);
                    break;
                case "AGENT":
                    responseMap = agentService.uploadDocs(userID, fileType, imageFileUrl);
                    break;
                case "MESSENGER":
                    responseMap = messengerService.uploadDocs(userID, fileType, imageFileUrl);
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.uploadDocs(userID, fileType, imageFileUrl);
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                    break;
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject changeAccountStatus(JSONObject request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject changeRes = registrationStoreService.changeAccount(request);

            if (changeRes.getString("Status").equals("99")) {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "The Account does not exists")
                        .put("statusMessage", "The Account does not exists");
            } else {
                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "success")
                        .put("statusMessage", "successful");
                String status = "De-Activated";
                if (request.getString("Action").equals("1")) {
                    status = "Activated";
                }
                StringBuilder payloadBody = new StringBuilder();
                payloadBody.append("Dear,Your " + request.getString("AccountType").trim() + " Account has been " + status);
                payloadBody.append("<br/>");
                payloadBody.append("<br/>");
                payloadBody.append("Comments");
                payloadBody.append("<br/>");
                payloadBody.append(request.getString("Comment"));
                emailClient.emailSender(payloadBody.toString(), request.getString("UserEmail").trim(), "ACCOUNT-STATUS");
            }

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject updateUser(JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            switch (payload.getString("userType")) {
                case "MERCHANT":
                    responseMap = merchantService.updateMerchant(payload.getString("userId"), payload);
                    break;
                case "BUSINESS":
                    responseMap = businessService.updateBusiness(payload.getString("userId"), payload);
                    break;
                case "AGENT":
                    responseMap = agentService.updateAgent(payload.getString("userId"), payload);
                    break;
                case "MESSENGER":
                    responseMap = messengerService.updateMessenger(payload.getString("userId"), payload);
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.updateDistributor(payload.getString("userId"), payload);
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                    break;
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getUsers(JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            switch (payload.getString("userType")) {
                case "MERCHANT":
                    responseMap = merchantService.getUsers(payload);
                    break;
                case "BUSINESS":
                    responseMap = businessService.getUsers(payload);
                    break;
                case "AGENT":
                    responseMap = agentService.getUsers(payload);
                    break;
                case "MESSENGER":
                    responseMap = messengerService.getUsers(payload);
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.getUsers(payload);
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                    break;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getAllUsers(JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            switch (payload.getString("userType")) {
                case "MERCHANT":
                    responseMap = merchantService.getAllUsers(payload);
                    break;
                case "BUSINESS":
                    responseMap = businessService.getAllUsers(payload);
                    break;
                case "AGENT":
                    responseMap = agentService.getAllUsers(payload);
                    break;
                case "MESSENGER":
                    responseMap = messengerService.getAllUsers(payload);
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.getUsers(payload);
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                    break;
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getUser(JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            switch (payload.getString("userType")) {
                case "MERCHANT":
                    responseMap = merchantService.getUser(payload);
                    break;
                case "BUSINESS":
                    responseMap = businessService.getUser(payload);
                    break;
                case "AGENT":
                    responseMap = agentService.getUser(payload);
                    break;
                case "MESSENGER":
                    responseMap = messengerService.getUser(payload);
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.getUser(payload);
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                    break;
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getAllUser(JSONObject payload) {
        JSONObject responseMap = new JSONObject();

        JSONObject info = new JSONObject();
        try {
            switch (payload.getString("userType")) {
                case "MERCHANT":
                    info = merchantService.getMerchantInfo(payload.getLong("userId"));
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("data", info)
                            .put("statusDescription", "success")
                            .put("statusMessage", "Request Successful");
                    break;
                case "BUSINESS":
                    info = businessService.getBusinessInfo(payload.getLong("userId"));
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("data", info)
                            .put("statusDescription", "success")
                            .put("statusMessage", "Request Successful");
                    break;
                case "AGENT":
                    info = agentService.getAgentInfo(payload.getLong("userId"));
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("data", info)
                            .put("statusDescription", "success")
                            .put("statusMessage", "Request Successful");
                    break;
                case "MESSENGER":
                    info = messengerService.getMessengerInfo(payload.getLong("userId"));
                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("data", info)
                            .put("statusDescription", "success")
                            .put("statusMessage", "Request Successful");
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.getUser(payload);
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                            .put("statusDescription", "failed to process request")
                            .put("statusMessage", "internal system error");
                    break;
            }
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public Integer countUsers(JSONObject payload) {
        Integer count = 0;
        try {
            switch (payload.getString("userType")) {
                case "MERCHANT":
                    count = merchantService.countUser(payload);
                    break;
                case "BUSINESS":
                    count = businessService.countUser(payload);
                    break;
                case "AGENT":
                    count = agentService.countUser(payload);
                    break;
            }
        } catch (Exception e) {
            log.log(Level.INFO, e.getMessage());
            count = 0;
        }
        return count;
    }


}
