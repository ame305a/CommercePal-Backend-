package com.commerce.pal.backend.utils;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.common.exceptions.customExceptions.UnauthorizedException;
import com.commerce.pal.backend.integ.notification.SmsEmailPushService;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.module.transaction.AccountService;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.setting.AppVersionRepository;
import com.commerce.pal.backend.repo.setting.CityRepository;
import com.commerce.pal.backend.repo.setting.RegionRepository;
import com.commerce.pal.backend.repo.user.*;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@Component
@SuppressWarnings("Duplicates")
public class GlobalMethods {

    private final SmsLogging smsLogging;
    private final CityRepository cityRepository;
    private final RegionRepository regionRepository;
    private final AccountService accountService;
    private final AgentRepository agentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final BusinessRepository businessRepository;
    private final SmsEmailPushService smsEmailPushService;
    private final MessengerRepository messengerRepository;
    private final DistributorRepository distributorRepository;
    private final AppVersionRepository appVersionRepository;
    private final LoginValidationRepository loginValidationRepository;

    @Autowired
    public GlobalMethods(SmsLogging smsLogging,
                         CityRepository cityRepository, RegionRepository regionRepository,
                         AccountService accountService,
                         AgentRepository agentRepository,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository,
                         MerchantRepository merchantRepository,
                         BusinessRepository businessRepository,
                         SmsEmailPushService smsEmailPushService,
                         MessengerRepository messengerRepository,
                         DistributorRepository distributorRepository,
                         AppVersionRepository appVersionRepository,
                         LoginValidationRepository loginValidationRepository) {
        this.smsLogging = smsLogging;
        this.cityRepository = cityRepository;
        this.regionRepository = regionRepository;
        this.accountService = accountService;
        this.agentRepository = agentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
        this.businessRepository = businessRepository;
        this.smsEmailPushService = smsEmailPushService;
        this.messengerRepository = messengerRepository;
        this.distributorRepository = distributorRepository;
        this.appVersionRepository = appVersionRepository;
        this.loginValidationRepository = loginValidationRepository;
    }

    public static JSONObject enhanceWithSuccessStatus(JSONObject responseObject) {
        return responseObject.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "The request has been processed successfully.");
    }

    public void sendSMSNotification(JSONObject data) {
        String message = smsLogging.generateMessage(data);
        smsEmailPushService.pickAndProcess(message, data.getString("Phone"));
    }

    public void sendEmailNotification(JSONObject data) {
        smsEmailPushService.pickAndProcessEmail(data);
    }

    public void sendSlackNotification(JSONObject data) {
        smsEmailPushService.pickAndProcessSlack(data);
    }

    public void sendPushNotification(JSONObject payload) {
        smsEmailPushService.pickAndProcessPush(payload.getString("UserId"),
                payload.getString("Header"),
                payload.getString("Message"),
                payload.getJSONObject("data"));
    }

//    public LoginValidation fetchUserDetails() {
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String username = user.getUsername();
//        LoginValidation principalDetails = loginValidationRepository.findByEmailAddress(username);
//        return principalDetails;
//    }

    public LoginValidation fetchUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken)
            throw new UnauthorizedException("Access denied. Invalid authentication token.");

        String username = authentication.getName();
        LoginValidation user = loginValidationRepository.findByEmailAddress(username);

        if (user == null)
            throw new ResourceNotFoundException("User with username '" + username + "' not found.");

        return user;
    }

    public String getAccountBalance(String account) {
        return accountService.getAccountBalance(account);
    }

    public String generateTrans() {
        String ref = Timestamp.from(Instant.now()).toString();
        Boolean finalRef = false;
        while (finalRef == false) {
            ref = IDGenerator.getInstance("CP").getRRN();
            if (!ref.contains("0") || !ref.contains("O")) {
                finalRef = true;
            }
        }
        return ref;
    }

    public JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
        JSONObject mergedJSON = new JSONObject();
        try {
            mergedJSON = new JSONObject(json1, JSONObject.getNames(json1));
            for (String crunchifyKey : JSONObject.getNames(json2)) {
                mergedJSON.put(crunchifyKey, json2.get(crunchifyKey));
            }

        } catch (JSONException e) {
            throw new RuntimeException("JSON Exception" + e);
        }
        return mergedJSON;
    }

    public Integer getMerchantCity(Long mer) {
        AtomicReference<Integer> city = new AtomicReference<>(1);
        try {
            merchantRepository.findById(mer)
                    .ifPresentOrElse(merchant -> {
                        city.set(Integer.valueOf(merchant.getCity()));
                    }, () -> {
                        city.set(1);
                    });
        } catch (Exception ex) {

        }
        return city.get();
    }

    public Integer customerRepository(String email) {
        AtomicReference<Integer> city = new AtomicReference<>(1);
        try {
            customerRepository.findCustomerByEmailAddress(email)
                    .ifPresentOrElse(customer -> {
                        city.set(Integer.valueOf(customer.getCity()));
                    }, () -> {
                        city.set(1);
                    });
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }
        return city.get();
    }

    public JSONObject getMultiUserCustomer(String user) {
        JSONObject customerData = new JSONObject();
        customerRepository.findByEmailAddressOrPhoneNumber(user, user)
                .ifPresent(customer -> {
                    customerData.put("firstName", customer.getFirstName());
                    customerData.put("lastName", customer.getLastName());
                    customerData.put("language", customer.getLanguage());
                    customerData.put("phoneNumber", customer.getPhoneNumber());
                    customerData.put("email", customer.getEmailAddress());
                });

        return customerData;
    }

    public String generatePassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        //String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                //.concat(specialChar)
                .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return password;
    }

    public String getGeneratePassword() {
        Random rnd = new Random();
        Integer n = Integer.valueOf(100001 + rnd.nextInt(900000));
        return n.toString();
    }

    public String getMobileValidationCode() {
        Random rnd = new Random();
        int n = 1000 + rnd.nextInt(9000);
        return Integer.toString(n);
    }

    public Long getDistributorId(String email) {
        return Long.valueOf(getUserId("DISTRIBUTOR", email));
    }

    public Long getCustomerId(String email) {
        return Long.valueOf(getUserId("CUSTOMER", email));
    }


    public Customer getCurrentLogginedCustomer() {
        LoginValidation user = fetchUserDetails();
        return customerRepository.findCustomerByEmailAddressOrPhoneNumber(user.getEmailAddress(), user.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
    }

    /**
     * Retrieves the currently logged-in customer based on the user's credentials.
     */
    public Customer getLoggedInCustomer() {
        LoginValidation userDetails = fetchUserDetails();
        return customerRepository.findCustomerByEmailAddressOrPhoneNumber(userDetails.getEmailAddress(), userDetails.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
    }

    public Long getMerchantId(String email) {
        return Long.valueOf(getUserId("MERCHANT", email));
    }

    public Boolean validateDistUser(Long dist, String type, String user) {
        Boolean results = false;
        switch (type) {
            case "MERCHANT":
                results = merchantRepository.findMerchantByOwnerIdAndOwnerTypeAndMerchantId(
                        dist.intValue(), "DISTRIBUTOR", Long.valueOf(user)).isPresent();
                break;
            case "AGENT":
                results = agentRepository.findAgentByOwnerIdAndOwnerTypeAndAgentId(
                        dist.intValue(), "DISTRIBUTOR", Long.valueOf(user)).isPresent();
                break;
            case "BUSINESS":
                results = businessRepository.findBusinessByOwnerIdAndOwnerTypeAndBusinessId(
                        dist.intValue(), "DISTRIBUTOR", Long.valueOf(user)).isPresent();
                break;
        }
        return results;
    }

    public Boolean validateMerchantProduct(Long merchant, Long product) {
        Boolean results = false;
        try {
            results = productRepository.findProductByOwnerTypeAndMerchantIdAndProductId(
                    "MERCHANT", Long.valueOf(merchant), Long.valueOf(product)
            ).isPresent();
        } catch (Exception ex) {
            log.log(Level.WARNING, "VALIDATE MERCHANT PRODUCT : " + ex.getMessage());
        }
        return results;
    }

    public String getUserId(String type, String email) {
        Long results = 0L;
        switch (type) {
            case "MERCHANT":
                results = merchantRepository.findMerchantByEmailAddress(email).get().getMerchantId();
                break;
            case "DISTRIBUTOR":
                results = distributorRepository.findDistributorByEmailAddress(email).get().getDistributorId();
                break;
            case "AGENT":
                results = agentRepository.findAgentByEmailAddress(email).get().getAgentId();
                break;
            case "BUSINESS":
                results = businessRepository.findBusinessByEmailAddress(email).get().getBusinessId();
                break;
            case "MESSENGER":
                results = messengerRepository.findMessengerByEmailAddress(email).get().getMessengerId();
                break;
            case "CUSTOMER":
                results = customerRepository.findCustomerByEmailAddress(email).get().getCustomerId();
                break;
        }
        return String.valueOf(results);
    }


    public String getUserId(String type, String email, String phoneNumber) {
        Long results = 0L;
        switch (type) {
            case "MERCHANT":
                results = merchantRepository.findMerchantByEmailAddressOrOwnerPhoneNumber(email, phoneNumber).get().getMerchantId();
                break;
            case "DISTRIBUTOR":
                results = distributorRepository.findDistributorByEmailAddressOrPhoneNumber(email, phoneNumber).get().getDistributorId();
                break;
            case "AGENT":
                results = agentRepository.findAgentByEmailAddressOrOwnerPhoneNumber(email, phoneNumber).get().getAgentId();
                break;
            case "BUSINESS":
                results = businessRepository.findBusinessByEmailAddressOrOwnerPhoneNumber(email, phoneNumber).get().getBusinessId();
                break;
            case "MESSENGER":
                results = messengerRepository.findMessengerByEmailAddressOrOwnerPhoneNumber(email, phoneNumber).get().getMessengerId();
                break;
            case "CUSTOMER":
                results = customerRepository.findCustomerByEmailAddressOrPhoneNumber(email, phoneNumber).get().getCustomerId();
                break;
        }
        return String.valueOf(results);
    }

    public String generateValidationCode() {
        Random rnd = new Random();
        Integer n = Integer.valueOf(1000 + rnd.nextInt(9000));
        return n.toString();
    }

    public String encryptCode(String code) {
        return code;
    }

    public String deCryptCode(String code) {
        return code;
    }


    public String generateUniqueString(String strValue) {
        String uniqueString = "";
        boolean corectFormat = false;
        try {
            while (!corectFormat) {
                uniqueString = Base64.getEncoder().encodeToString(strValue.getBytes());
                if (uniqueString.contains("/")) continue;
                corectFormat = true;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
        }
        return uniqueString;
    }

    public String getStringValue(String strEncrypted) {
        String strValue = "";
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(strEncrypted);
            strValue = new String(decodedBytes);
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            strValue = "";
        }
        return strValue;
    }

    public String cityName(Long cityId) {
        AtomicReference<String> name = new AtomicReference<>("Addis Ababa");
        cityRepository.findById(cityId)
                .ifPresent(city -> {
                    name.set(city.getCity());
                });
        return name.get();
    }

    public String regionName(int regionId) {
        AtomicReference<String> name = new AtomicReference<>("Addis Ababa");
        regionRepository.findById(regionId)
                .ifPresent(region -> {
                    name.set(region.getRegionName());
                });
        return name.get();
    }

    public String getHashkey() {
        AtomicReference<String> mes = new AtomicReference<>("");
        appVersionRepository.findById(1)
                .ifPresent(appVersion -> {
                    mes.set("\n " + appVersion.getSmsHash());
                });
        return mes.get();
    }

    public static Timestamp parseTimestampFromDateString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return new Timestamp(parsedDate.getTime());
    }


    // Helper method to get customer name with handling for null values
    public String getCustomerName(Long customerId) {
        String defaultName = "Unknown Customer";
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            // Concatenate non-null values
            StringBuilder nameBuilder = new StringBuilder();
            appendIfNotNull(nameBuilder, customer.getFirstName());
            appendIfNotNull(nameBuilder, customer.getMiddleName());
            appendIfNotNull(nameBuilder, customer.getLastName());
            return nameBuilder.toString().trim();
        } else {
            return defaultName;
        }
    }

    // Helper method to append a non-null value to StringBuilder
    private void appendIfNotNull(StringBuilder builder, String value) {
        if (value != null && !value.isEmpty()) {
            builder.append(value).append(" ");
        }

    }

    public static String formatPhoneNumberToEthiopianStandard(String phoneNumber) {
        if (phoneNumber == null)
            return null;

        if (phoneNumber.length() < 9)
            throw new IllegalArgumentException("Invalid phone number. Phone number should be at least 9 digits long.");

        return "251" + phoneNumber.substring(phoneNumber.length() - 9);
    }

    public static <T, R> JSONObject buildResponseWithPagination(String dataName, List<R> data, Page<T> page) {
        JSONObject response = new JSONObject();
        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", page.getNumber())
                .put("pageSize", page.getSize())
                .put("totalElements", page.getTotalElements())
                .put("totalPages", page.getTotalPages());

        JSONObject responseData = new JSONObject();
        responseData.put("paginationInfo", paginationInfo)
                .put(dataName, data);

        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "SUCCESS")
                .put("statusMessage", "SUCCESS")
                .put("responseData", responseData);

        return response;
    }

}
