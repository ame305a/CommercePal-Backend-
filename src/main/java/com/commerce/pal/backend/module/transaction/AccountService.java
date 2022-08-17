package com.commerce.pal.backend.module.transaction;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class AccountService {
    @PersistenceContext
    private EntityManager entityManager;

    public String getAccountBalance(String account) {
        AtomicReference<String> balance = new AtomicReference<>("0.00");
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SpGetAccountBalance");
            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
            query.setParameter(1, account);
            List response = query.getResultList();
            response.forEach(res -> {
                try {
                    List<?> resData = new ArrayList();
                    if (res.getClass().isArray()) {
                        resData = Arrays.asList((Object[]) res);
                        balance.set(resData.get(0).toString());
                    } else if (res instanceof Collection) {
                        balance.set(res.toString());
                    }else{
                        balance.set(res.toString());
                    }

                } catch (Exception ex) {
                    log.log(Level.WARNING, ex.getMessage());
                }
            });
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }
        return balance.get();
    }
}
