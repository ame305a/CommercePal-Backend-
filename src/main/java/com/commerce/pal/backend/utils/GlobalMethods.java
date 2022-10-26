package com.commerce.pal.backend.utils;

import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.module.transaction.AccountService;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.user.*;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import org.jasypt.encryption.StringEncryptor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@Component
@SuppressWarnings("Duplicates")
public class GlobalMethods {

    private static StringEncryptor jasyptStringEncryptor;

    private final AccountService accountService;
    private final AgentRepository agentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final BusinessRepository businessRepository;
    private final MessengerRepository messengerRepository;
    private final DistributorRepository distributorRepository;
    private final LoginValidationRepository loginValidationRepository;


    @Autowired
    public GlobalMethods(AccountService accountService, AgentRepository agentRepository,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository,
                         MerchantRepository merchantRepository,
                         BusinessRepository businessRepository,
                         MessengerRepository messengerRepository,
                         DistributorRepository distributorRepository,
                         LoginValidationRepository loginValidationRepository) {
        this.accountService = accountService;
        this.agentRepository = agentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
        this.businessRepository = businessRepository;
        this.messengerRepository = messengerRepository;
        this.distributorRepository = distributorRepository;
        this.loginValidationRepository = loginValidationRepository;
    }

    public LoginValidation fetchUserDetails() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        LoginValidation principalDetails = loginValidationRepository.findByEmailAddress(username);
        return principalDetails;
    }

    public String getAccountBalance(String account) {
        return accountService.getAccountBalance(account);
    }

    public String generateTrans() {
        return IDGenerator.getInstance("CP").getRRN();
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

    public JSONObject getMultiUserCustomer(String email) {
        JSONObject customerData = new JSONObject();
        customerRepository.findCustomerByEmailAddress(email)
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

    public String getMobileValidationCode() {
        Random rnd = new Random();
        Integer n = Integer.valueOf(1000 + rnd.nextInt(9000));
        return n.toString();
    }

    public Long getDistributorId(String email) {
        return Long.valueOf(getUserId("DISTRIBUTOR", email));
    }

    public Long getCustomerId(String email) {
        return Long.valueOf(getUserId("CUSTOMER", email));
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
                uniqueString = jasyptStringEncryptor.encrypt(strValue);
                if (uniqueString.contains("/")) continue;
                corectFormat = true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return uniqueString;
    }

    public String getStringValue(String strEncrypted) {
        String strValue = "";
        try {
            strValue = jasyptStringEncryptor.decrypt(strEncrypted);
        }
        catch (Exception e) {
            strValue = "";
        }
        return strValue;
    }
}
