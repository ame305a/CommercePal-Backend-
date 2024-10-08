package com.commerce.pal.backend.module.database;

import com.commerce.pal.backend.common.ResponseCodes;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
            query.registerStoredProcedureParameter("ShortDescription", String.class, ParameterMode.IN);
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
            query.registerStoredProcedureParameter("ProductType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("IsDiscounted", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("DiscountType", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("DiscountValue", String.class, ParameterMode.IN);
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
            query.setParameter("ShortDescription", regHm.getString("shortDescription"));
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
            query.setParameter("ProductType", regHm.getString("productType").toUpperCase());
            query.setParameter("IsDiscounted", regHm.getString("isDiscounted"));
            query.setParameter("DiscountType", regHm.getString("discountType").toUpperCase());
            query.setParameter("DiscountValue", regHm.getString("discountValue"));
            query.setParameter("IsPromoted", regHm.getString("isPromoted"));
            query.setParameter("IsPrioritized", regHm.getString("isPrioritized"));
            query.setParameter("CreatedBy", regHm.getString("createdBy"));
            query.registerStoredProcedureParameter("ProductExist", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("SubProductId", Integer.class, ParameterMode.OUT);
            query.execute();
            Integer productId = (Integer) query.getOutputParameterValue("ProductExist");
            Integer subProductId = (Integer) query.getOutputParameterValue("SubProductId");
            retDet.put("productId", productId);
            retDet.put("subProductId", subProductId);
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Error at DoProductRegistration - " + e.getMessage());
            retDet.put("productId", 0);
        }
        return retDet;
    }

    public JSONObject calculateProductPrice(BigDecimal priceProduct) {
        JSONObject transResponse = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("GetProductChargeCommission");
            query.registerStoredProcedureParameter("TransactionAmount", BigDecimal.class, ParameterMode.IN);
            query.setParameter("TransactionAmount", priceProduct);
            query.registerStoredProcedureParameter("ChargeId", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("TransactionCharge", String.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("FinalPrice", String.class, ParameterMode.OUT);
            query.execute();
            transResponse.put("ChargeId", query.getOutputParameterValue("ChargeId"));
            transResponse.put("Charge", query.getOutputParameterValue("TransactionCharge"));
            transResponse.put("FinalPrice", query.getOutputParameterValue("FinalPrice"));

        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            transResponse.put("Status", "99");
            transResponse.put("Narration", ex.getMessage());
        }
        return transResponse;
    }

    public JSONObject updateMerchantStatus(JSONObject data) {
        JSONObject transResponse = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("MerchantStatus");
            query.registerStoredProcedureParameter(1, BigInteger.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);

            query.setParameter(1, data.getBigInteger("MerchantId"));
            query.setParameter(2, data.getString("Type"));
            query.setParameter(3, data.getString("StatusComment"));

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
                    transResponse.put("Status", "88");
                    transResponse.put("Narration", ex.getMessage());
                }
            });
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
            transResponse.put("Status", "99");
            transResponse.put("Narration", ex.getMessage());
        }
        return transResponse;
    }

    public JSONObject callProductReportService(String startDate, String endDate, String reportType) {
        JSONObject report = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("GetProductReportByDateRange");
            //Input Parameters
            query.registerStoredProcedureParameter("StartDate", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("EndDate", String.class, ParameterMode.IN);
            query.setParameter("StartDate", startDate);
            query.setParameter("EndDate", endDate);

            //Output Parameters
            query.registerStoredProcedureParameter("WarehouseProductCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("MerchantProductCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("PendingProductCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("ApprovedProductCount", Integer.class, ParameterMode.OUT);

            //Execute Stored Procedure
            query.execute();
            Integer warehouseProductCount = (Integer) query.getOutputParameterValue("WarehouseProductCount");
            Integer merchantProductCount = (Integer) query.getOutputParameterValue("MerchantProductCount");
            Integer approvedProductCount = (Integer) query.getOutputParameterValue("ApprovedProductCount");
            Integer PendingProductCount = (Integer) query.getOutputParameterValue("PendingProductCount");

            //Set Output Parameters
            report.put("statusCode", ResponseCodes.SUCCESS);
            report.put("warehouseProductCount", warehouseProductCount);
            report.put("merchantProductCount", merchantProductCount);
            report.put("approvedProductCount", approvedProductCount);
            report.put("pendingProductCount", PendingProductCount);

            log.info(reportType + " generated successfully: " + report.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Error at GetProductReportByDateRange - " + e.getMessage());
            report.put("statusCode", ResponseCodes.INTERNAL_SERVER_ERROR);
        }
        return report;
    }

}
