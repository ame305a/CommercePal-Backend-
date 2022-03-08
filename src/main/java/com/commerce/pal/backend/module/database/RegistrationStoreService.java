package com.commerce.pal.backend.module.database;

import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

@Log
@Component
@SuppressWarnings("Duplicates")
public class RegistrationStoreService {
    @PersistenceContext
    private EntityManager entityManager;

    public JSONObject doMerchantRegistration(JSONObject regHm) {
        JSONObject retDet = new JSONObject();

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoMerchantRegistration");
            query.registerStoredProcedureParameter("@OwnerType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@OwnerId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@FirstName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@LastName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Email", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Password", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Longitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Latitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@City", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Country", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Language", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@OwnerPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@BusinessPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@MerchantName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@BusinessType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@BusinessCategory", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@BusinessLicense", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@CommercialCertNo", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@TaxNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@BankCode", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@BankAccountNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@Branch", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("@RegisteredBy", String.class, ParameterMode.IN);

            query.setParameter("@OwnerType", regHm.getString("ownerType"));
            query.setParameter("@OwnerId", regHm.getString("ownerId"));
            query.setParameter("@FirstName", regHm.getString("firstName"));
            query.setParameter("@LastName", regHm.getString("lastName"));
            query.setParameter("@Email", regHm.getString("email"));
            query.setParameter("@Password", regHm.getString("password"));
            query.setParameter("@Longitude", regHm.getString("longitude"));
            query.setParameter("@Latitude", regHm.getString("latitude"));
            query.setParameter("@City", regHm.getString("city"));
            query.setParameter("@Country", regHm.getString("country"));
            query.setParameter("@Language", regHm.getString("language"));
            query.setParameter("@OwnerPhoneNumber", regHm.getString("ownerPhoneNumber"));
            query.setParameter("@BusinessPhoneNumber", regHm.getString("businessPhoneNumber"));
            query.setParameter("@MerchantName", regHm.getString("businessName"));
            query.setParameter("@BusinessType", regHm.getString("businessType"));
            query.setParameter("@BusinessCategory", regHm.getString("businessCategory"));
            query.setParameter("@BusinessLicense", regHm.getString("businessLicense"));
            query.setParameter("@CommercialCertNo", regHm.getString("commercialCertNo"));
            query.setParameter("@TaxNumber", regHm.getString("taxNumber"));
            query.setParameter("@BankCode", regHm.getString("bankCode"));
            query.setParameter("@BankAccountNumber", regHm.getString("bankAccountNumber"));
            query.setParameter("@Branch", regHm.getString("branch"));
            query.setParameter("@RegisteredBy", regHm.getString("registeredBy"));
            query.registerStoredProcedureParameter("RegistrationExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("RegistrationExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception ex) {
            retDet.put("returnValue", 1);
            log.log(Level.WARNING, "Error doMerchantRegistration : " + ex.getMessage());
        }
        return retDet;
    }

    public JSONObject doBusinessRegistration(JSONObject regHm) {

        JSONObject retDet = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoBusinessRegistration");
            query.registerStoredProcedureParameter("OwnerType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("FirstName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("LastName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Email", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Password", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Longitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Latitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("City", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Country", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Language", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("BusinessPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("BusinessName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("CommercialCertNo", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Branch", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("RegisteredBy", String.class, ParameterMode.IN);
            query.setParameter("OwnerType", regHm.getString("ownerType"));
            query.setParameter("OwnerId", regHm.getString("ownerId"));
            query.setParameter("FirstName", regHm.getString("firstName"));
            query.setParameter("LastName", regHm.getString("lastName"));
            query.setParameter("Email", regHm.getString("email"));
            query.setParameter("Password", regHm.getString("password"));
            query.setParameter("Longitude", regHm.getString("longitude"));
            query.setParameter("Latitude", regHm.getString("latitude"));
            query.setParameter("City", regHm.getString("city"));
            query.setParameter("Country", regHm.getString("country"));
            query.setParameter("Language", regHm.getString("language"));
            query.setParameter("OwnerPhoneNumber", regHm.getString("ownerPhoneNumber"));
            query.setParameter("BusinessPhoneNumber", regHm.getString("businessPhoneNumber"));
            query.setParameter("BusinessName", regHm.getString("businessName"));
            query.setParameter("CommercialCertNo", regHm.getString("commercialCertNo"));
            query.setParameter("Branch", regHm.getString("branch"));
            query.setParameter("RegisteredBy", regHm.getString("registeredBy"));
            query.registerStoredProcedureParameter("RegistrationExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("RegistrationExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error at doBusinessRegistration - " + e.getMessage());
            retDet.put("returnValue", 1);
        }
        return retDet;
    }

    public JSONObject doDistributorRegistration(JSONObject regHm) {
        JSONObject retDet = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoDistributorRegistration");
            query.registerStoredProcedureParameter("FirstName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("LastName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Email", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Password", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Longitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Latitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("City", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Country", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Language", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("BusinessPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("DistributorName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("DistributorType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Branch", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("IdNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("RegisteredBy", String.class, ParameterMode.IN);
            query.setParameter("FirstName", regHm.getString("firstName"));
            query.setParameter("LastName", regHm.getString("lastName"));
            query.setParameter("Email", regHm.getString("email"));
            query.setParameter("Password", regHm.getString("password"));
            query.setParameter("Longitude", regHm.getString("longitude"));
            query.setParameter("Latitude", regHm.getString("latitude"));
            query.setParameter("City", regHm.getString("city"));
            query.setParameter("Country", regHm.getString("country"));
            query.setParameter("Language", regHm.getString("language"));
            query.setParameter("OwnerPhoneNumber", regHm.getString("ownerPhoneNumber"));
            query.setParameter("BusinessPhoneNumber", regHm.getString("businessPhoneNumber"));
            query.setParameter("DistributorName", regHm.getString("distributorName"));
            query.setParameter("DistributorType", regHm.getString("distributorType"));
            query.setParameter("Branch", regHm.getString("branch"));
            query.setParameter("IdNumber", regHm.getString("idNumber"));
            query.setParameter("RegisteredBy", regHm.getString("registeredBy"));
            query.registerStoredProcedureParameter("RegistrationExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("RegistrationExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error at doDistributorRegistration - " + e.getMessage());
            retDet.put("returnValue", 1);
        }
        return retDet;
    }

    public JSONObject doAgentRegistration(JSONObject regHm) {

        JSONObject retDet = new JSONObject();
        try {
            /*
            To Be discussed later
             */
            regHm.put("agentCode", "12413r").put("agentType", "1").put("agentCategory", "Shop");
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoAgentRegistration");
            query.registerStoredProcedureParameter("OwnerType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("FirstName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("LastName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Email", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Password", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Longitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Latitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("City", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Country", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Language", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("BusinessPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("AgentName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("AgentCode", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("AgentType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("AgentCategory", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("TaxNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("BankCode", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("BankAccountNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("CommercialCertNo", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Branch", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("RegisteredBy", String.class, ParameterMode.IN);
            query.setParameter("OwnerType", regHm.getString("ownerType"));
            query.setParameter("OwnerId", regHm.getString("ownerId"));
            query.setParameter("FirstName", regHm.getString("firstName"));
            query.setParameter("LastName", regHm.getString("lastName"));
            query.setParameter("Email", regHm.getString("email"));
            query.setParameter("Password", regHm.getString("password"));
            query.setParameter("Longitude", regHm.getString("longitude"));
            query.setParameter("Latitude", regHm.getString("latitude"));
            query.setParameter("City", regHm.getString("city"));
            query.setParameter("Country", regHm.getString("country"));
            query.setParameter("Language", regHm.getString("language"));
            query.setParameter("OwnerPhoneNumber", regHm.getString("ownerPhoneNumber"));
            query.setParameter("BusinessPhoneNumber", regHm.getString("businessPhoneNumber"));
            query.setParameter("AgentName", regHm.getString("businessName"));
            query.setParameter("AgentCode", regHm.getString("agentCode"));
            query.setParameter("AgentType", regHm.getString("agentType"));
            query.setParameter("AgentCategory", regHm.getString("agentCategory"));
            query.setParameter("TaxNumber", regHm.getString("taxNumber"));
            query.setParameter("BankCode", regHm.getString("bankCode"));
            query.setParameter("BankAccountNumber", regHm.getString("bankAccountNumber"));
            query.setParameter("CommercialCertNo", regHm.getString("commercialCertNo"));
            query.setParameter("Branch", regHm.getString("branch"));
            query.setParameter("RegisteredBy", regHm.getString("registeredBy"));
            query.registerStoredProcedureParameter("RegistrationExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("RegistrationExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error at doAgentRegistration - " + e.getMessage());
            retDet.put("returnValue", 1);
        }

        return retDet;
    }

    public JSONObject doMessengerRegistration(JSONObject regHm) {

        JSONObject retDet = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoMessengerRegistration");
            query.registerStoredProcedureParameter("OwnerType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("FirstName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("LastName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OwnerPhoneNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Email", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("City", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Country", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Password", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Longitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Latitude", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Language", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("District", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Location", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("IdNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("DrivingLicenceNumber", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("InsuranceExpiry", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("CarrierType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("CarrierLicencePlate", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("RegisteredBy", String.class, ParameterMode.IN);

            query.setParameter("OwnerType", regHm.getString("ownerType"));
            query.setParameter("OwnerId", regHm.getString("ownerId"));
            query.setParameter("FirstName", regHm.getString("firstName"));
            query.setParameter("LastName", regHm.getString("lastName"));
            query.setParameter("OwnerPhoneNumber", regHm.getString("ownerPhoneNumber"));
            query.setParameter("Email", regHm.getString("email"));
            query.setParameter("City", regHm.getString("city"));
            query.setParameter("Country", regHm.getString("country"));
            query.setParameter("Password", regHm.getString("password"));
            query.setParameter("Longitude", regHm.getString("longitude"));
            query.setParameter("Latitude", regHm.getString("latitude"));
            query.setParameter("Language", regHm.getString("language"));
            query.setParameter("District", regHm.getString("district"));
            query.setParameter("Location", regHm.getString("location"));
            query.setParameter("IdNumber", regHm.getString("idNumber"));
            query.setParameter("DrivingLicenceNumber", regHm.getString("drivingLicenceNumber"));
            query.setParameter("InsuranceExpiry", regHm.getString("insuranceExpiry"));
            query.setParameter("CarrierType", regHm.getString("carrierType"));
            query.setParameter("CarrierLicencePlate", regHm.getString("carrierLicencePlate"));
            query.setParameter("RegisteredBy", regHm.getString("registeredBy"));
            query.registerStoredProcedureParameter("RegistrationExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("RegistrationExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error at doAgentRegistration - " + e.getMessage());
            retDet.put("returnValue", 1);
        }
        return retDet;
    }

    public JSONObject doCustomerRegistration(JSONObject regHm) {

        JSONObject retDet = new JSONObject();

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoCustomerRegistration");

            query.registerStoredProcedureParameter("FirstName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("LastName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Email", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Password", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("City", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Country", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Language", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Msisdn", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("RegisteredBy", String.class, ParameterMode.IN);
            query.setParameter("FirstName", regHm.getString("firstName"));
            query.setParameter("LastName", regHm.getString("lastName"));
            query.setParameter("Email", regHm.getString("email"));
            query.setParameter("Password", regHm.getString("password"));
            query.setParameter("City", regHm.getString("city"));
            query.setParameter("Country", regHm.getString("country"));
            query.setParameter("Language", regHm.getString("language"));
            query.setParameter("Msisdn", regHm.getString("msisdn"));
            query.setParameter("RegisteredBy", regHm.getString("registeredBy"));
            query.registerStoredProcedureParameter("RegistrationExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("RegistrationExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error doCustomerRegistration - " + e.getMessage());
            retDet.put("returnValue", 1);
        }
        return retDet;
    }

    public JSONObject changeAccount(JSONObject request) {
        JSONObject transResponse = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("DoAccountStatus");
            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(5, String.class, ParameterMode.IN);
            query.setParameter(1, request.getString("AccountType"));
            query.setParameter(2, request.getString("UserEmail"));
            query.setParameter(3, request.getString("Comment"));
            query.setParameter(4, request.getString("User"));
            query.setParameter(5, request.getString("Action"));

            List response = query.getResultList();
            response.forEach(res -> {
                try {
                    List<?> resData = new ArrayList();
                    if (res.getClass().isArray()) {
                        resData = Arrays.asList((Object[]) res);
                    } else if (res instanceof Collection) {
                        resData = new ArrayList((Collection) res);
                    }
                    transResponse.put("Status", resData.get(0).toString());
                    transResponse.put("Narration", resData.get(1).toString());
                } catch (Exception ex) {
                    log.log(Level.WARNING, ex.getMessage());
                    transResponse.put("Status", "99");
                    transResponse.put("Narration", "Failed Processing Response");
                }
            });
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            transResponse.put("Status", "99");
            transResponse.put("Narration", "Failed Processing Response");
        }
        return transResponse;
    }
}
