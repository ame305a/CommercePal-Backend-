package com.commerce.pal.backend.database;

import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.sql.SQLException;
import java.util.logging.Level;

@Log
@Component
@SuppressWarnings("Duplicates")
public class ProductDatabaseService {
    @PersistenceContext
    private EntityManager entityManager;

    public JSONObject doAddProduct(JSONObject regHm) {

        JSONObject retDet = new JSONObject();

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("AddProduct");
            query.registerStoredProcedureParameter("OwnerType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("MerchantId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("ProductParentCateoryId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("ProductCategoryId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("ProductSubCategoryId", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("ProductName", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("ProductImage", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("ProductDescription", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("SpecialInstruction", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Quantity", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("UnitOfMeasure", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("UnitPrice", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Currency", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Tax", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("MinOrder", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("MaxOrder", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("SoldQuantity", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("CountryOfOrigin", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("Manufucturer", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("IsDiscounted", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("IsPromoted", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("IsPrioritized", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("CreatedBy", String.class, ParameterMode.IN);

            query.setParameter("OwnerType", regHm.getString("ownerType"));
            query.setParameter("MerchantId", regHm.getString("merchantId"));
            query.setParameter("ProductParentCateoryId", regHm.getString("productParentCateoryId"));
            query.setParameter("ProductCategoryId", regHm.getString("productCategoryId"));
            query.setParameter("ProductSubCategoryId", regHm.getString("productSubCategoryId"));
            query.setParameter("ProductName", regHm.getString("productName"));
            query.setParameter("ProductImage", regHm.getString("productImage"));
            query.setParameter("ProductDescription", regHm.getString("productDescription"));
            query.setParameter("SpecialInstruction", regHm.getString("specialInstruction"));
            query.setParameter("Quantity", regHm.getString("quantity"));
            query.setParameter("UnitOfMeasure", regHm.getString("unitOfMeasure"));
            query.setParameter("UnitPrice", regHm.getString("unitPrice"));
            query.setParameter("Currency", regHm.getString("currency"));
            query.setParameter("Tax", regHm.getString("tax"));
            query.setParameter("MinOrder", regHm.getString("minOrder"));
            query.setParameter("MaxOrder", regHm.getString("maxOrder"));
            query.setParameter("SoldQuantity", regHm.getString("soldQuantity"));
            query.setParameter("CountryOfOrigin", regHm.getString("countryOfOrigin"));
            query.setParameter("Manufucturer", regHm.getString("manufucturer"));
            query.setParameter("IsDiscounted", regHm.getString("isDiscounted"));
            query.setParameter("IsPromoted", regHm.getString("isPromoted"));
            query.setParameter("IsPrioritized", regHm.getString("isPrioritized"));
            query.setParameter("CreatedBy", regHm.getString("createdBy"));

            query.registerStoredProcedureParameter("ProductExist", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer exists = (Integer) query.getOutputParameterValue("ProductExist");
            retDet.put("returnValue", exists);
            retDet.put("exists", exists);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error at doCustomerRegistration - " + e.getMessage());
            retDet.put("returnValue", 1);
        }
        return retDet;
    }
}
