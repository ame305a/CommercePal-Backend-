package com.commerce.pal.backend.module.database;

import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

@Log
@Component
@SuppressWarnings("Duplicates")
public class ImageService {
    @PersistenceContext
    private EntityManager entityManager;

    public JSONObject updateImage(JSONObject data) {
        JSONObject transResponse = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("UploadImage");
            query.registerStoredProcedureParameter(1, BigInteger.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

            query.setParameter(1, data.getBigInteger("Id"));
            query.setParameter(2, data.getString("Type"));
            query.setParameter(3, data.getString("Platform"));
            query.setParameter(4, data.getString("ImageUrl"));

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

    public JSONObject uploadPickUpImage(JSONObject data) {
        JSONObject transResponse = new JSONObject();
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("UploadItemPhoto");
            query.registerStoredProcedureParameter(1, BigInteger.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, BigInteger.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);

            query.setParameter(1, data.getBigInteger("Id"));
            query.setParameter(2, data.getBigInteger("OrderItemId"));
            query.setParameter(3, data.getString("ImageUrl"));

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
}
