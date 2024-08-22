package com.commerce.pal.backend.module.product.promotion;

import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.logging.Level;

@Log
@Component
@SuppressWarnings("Duplicates")
public class ProductPromoCodeExpirationTask {

    @PersistenceContext
    private EntityManager entityManager;


    //    @Scheduled(fixedRate = 3600000) // runs every hour
//    @Scheduled(fixedRate = 30000) // runs every 30 seconds
//    @Async
//    public void expirePromoCodes() {
//        int returnValue = processPromoCodesExpiration();
//        "Method running on thread: " + Thread.currentThread().getName());
//        //TODO: SEND SMS AND EMAIL TO ADMIN
//        if (returnValue == -1) {
//            "Error occurred while processing promo codes expiration");
//        } else "successful running mf");
//    }

    @Scheduled(cron = "0 5 * * * *") // runs every hour at 5 minutes past the hour
    @Async
    public void expirePromoCodes() {
        System.out.println("Method running on thread: " + Thread.currentThread().getName());
        int returnValue = processPromoCodesExpiration();
        //TODO: SEND SMS AND EMAIL TO ADMIN
        if (returnValue == -1) {
        }
    }

    private int processPromoCodesExpiration() {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("ExpireActivePromoCodes");

            query.registerStoredProcedureParameter("ReturnValue", Integer.class, ParameterMode.OUT);
            query.execute();

            return (Integer) query.getOutputParameterValue("ReturnValue");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error occurred while processing promo codes expiration", e);
            return -1;
        }
    }

}




