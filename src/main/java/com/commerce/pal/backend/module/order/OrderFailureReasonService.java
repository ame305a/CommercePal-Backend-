package com.commerce.pal.backend.module.order;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.order.orderFailureReason.OrderFailureCategoryRepository;
import com.commerce.pal.backend.repo.order.orderFailureReason.OrderFailureReasonRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log
@Service
@SuppressWarnings("Duplicates")
public class OrderFailureReasonService {

    private final OrderFailureReasonRepository orderFailureReasonRepository;
    private final OrderFailureCategoryRepository orderFailureCategoryRepository;

    public OrderFailureReasonService(OrderFailureReasonRepository orderFailureReasonRepository, OrderFailureCategoryRepository orderFailureCategoryRepository) {
        this.orderFailureReasonRepository = orderFailureReasonRepository;
        this.orderFailureCategoryRepository = orderFailureCategoryRepository;
    }

    public JSONObject getOrderFailureReasons() {
        List<JSONObject> orderFailureReasons = new ArrayList<>();
        orderFailureCategoryRepository.findByStatus(1)
                .forEach(orderFailureCategory -> {
                    JSONObject failureCategory = new JSONObject();
                    failureCategory.put("type", orderFailureCategory.getType());
                    List<JSONObject> items = new ArrayList<>();
                    orderFailureReasonRepository.findByOrderFailureCategoryAndStatus(orderFailureCategory.getId(), 1)
                            .forEach(orderFailureReason -> {
                                JSONObject item = new JSONObject();
                                item.put("id", orderFailureReason.getId());
                                item.put("reason", orderFailureReason.getReason());
                                items.add(item);

                            });
                    failureCategory.put("items", items);
                    orderFailureReasons.add(failureCategory);
                });


        JSONObject data = new JSONObject();
        data.put("orderFailureReasons", orderFailureReasons);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", data)
                .put("statusDescription", "Success")
                .put("statusMessage", "Request processed successfully");

        return response;
    }

}
