package com.commerce.pal.backend.module.transaction;

import com.commerce.pal.backend.repo.transaction.AgentFloatRepository;
import com.commerce.pal.backend.service.specification.SpecificationsDao;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
@SuppressWarnings("Duplicates")
public class TransactionProcessingService {
    private final SpecificationsDao specificationsDao;
    private final AgentFloatRepository agentFloatRepository;

    @Autowired
    public TransactionProcessingService(SpecificationsDao specificationsDao,
                                        AgentFloatRepository agentFloatRepository) {
        this.specificationsDao = specificationsDao;
        this.agentFloatRepository = agentFloatRepository;
    }

    public List<JSONObject> getPayment(JSONObject rqBdy) {
        List<JSONObject> details = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            JSONObject detail = new JSONObject();
            detail.put("TransRef", "CPA8AZV4UX1A");
            detail.put("TransType", "PRODUCT-PAYMENT");
            detail.put("PaymentMethod", "Murabaha");
            detail.put("Amount", "21300.00");
            detail.put("AvailableBalance", "295.00");
            detail.put("DrCr", "C");
            detail.put("Currency", "ETB");
            detail.put("TransDate", "2021-09-04 13:49:42.710");
            detail.put("Narration", "Payment of Product [Nokia 2310]");
            details.add(detail);
        }
        return details;
    }


}
