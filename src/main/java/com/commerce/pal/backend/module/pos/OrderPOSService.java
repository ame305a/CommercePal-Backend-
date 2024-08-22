package com.commerce.pal.backend.module.pos;

import com.commerce.pal.backend.models.order.Order;
import com.commerce.pal.backend.repo.order.OrderRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log
@Service
@RequiredArgsConstructor
public class OrderPOSService {

    private final GlobalMethods globalMethods;
    private final OrderRepository orderRepository;

    public List<JSONObject> getMerchantOrders() {
        long merchantId = getMerchantId();
        List<Order> orders = orderRepository.findByMerchantIdOrderByOrderDateDesc(merchantId);

        List<JSONObject> details = new ArrayList<>();
        orders.forEach(order -> {
            JSONObject detail = new JSONObject();
            detail.put("orderId", order.getOrderId());
            detail.put("orderRef", order.getOrderRef());
            detail.put("paymentMethod", order.getPaymentMethod());
            detail.put("saleType", order.getSaleType());
            detail.put("totalPrice", order.getTotalPrice());
            detail.put("discount", order.getDiscount());
            detail.put("charge", order.getCharge());
            detail.put("status", order.getStatus());
            detail.put("orderDate", order.getOrderDate());
            detail.put("isOrderSuccess", order.getPaymentStatus() == 3 ? "Yes" : "No");
            detail.put("paymentStatus", order.getPaymentStatus());
            detail.put("status", order.getStatus());

            details.add(detail);
        });

        return details;
    }

    private Long getMerchantId() {
//        LoginValidation user = globalMethods.fetchUserDetails();
//        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());
//
//        if (merchantId == null || merchantId == 0L)
//            throw new ForbiddenException("This operation is exclusive to merchant accounts. " +
//                    "Please log in with a valid merchant account.");
//
//        return merchantId;
        return 1404L;
    }
}
