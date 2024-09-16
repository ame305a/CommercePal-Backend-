package com.commerce.pal.backend.module.database;

import com.commerce.pal.backend.common.ResponseCodes;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.logging.Level;

@Log
@Component
public class UserDatabaseService {
    @PersistenceContext
    private EntityManager entityManager;

    public JSONObject callUserReportService(String startDate, String endDate, String reportType) {
        JSONObject report = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("GetUserReportByDateRange");
            //Input Parameters
            query.registerStoredProcedureParameter("StartDate", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("EndDate", String.class, ParameterMode.IN);
            query.setParameter("StartDate", startDate);
            query.setParameter("EndDate", endDate);

            //Output Parameters
            query.registerStoredProcedureParameter("AgentCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("CustomerCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("DistributorCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("MerchantCount", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("MessengerCount", Integer.class, ParameterMode.OUT);

            //Execute Stored Procedure
            query.execute();
            Integer agentCount = (Integer) query.getOutputParameterValue("AgentCount");
            Integer customerCount = (Integer) query.getOutputParameterValue("CustomerCount");
            Integer distributorCount = (Integer) query.getOutputParameterValue("DistributorCount");
            Integer merchantCount = (Integer) query.getOutputParameterValue("MerchantCount");
            Integer messengerCount = (Integer) query.getOutputParameterValue("MessengerCount");

            //Set Output Parameters
            report.put("statusCode", ResponseCodes.SUCCESS);
            report.put("agentCount", agentCount);
            report.put("customerCount", customerCount);
            report.put("distributorCount", distributorCount);
            report.put("merchantCount", merchantCount);
            report.put("messengerCount", messengerCount);

            log.info(reportType + " generated successfully: " + report.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Error at GetUserReportByDateRange - " + e.getMessage());
            report.put("statusCode", ResponseCodes.INTERNAL_SERVER_ERROR);
        }
        return report;
    }

}
