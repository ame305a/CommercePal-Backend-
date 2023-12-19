package com.commerce.pal.backend.module.order;


import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.order.Order;
import com.commerce.pal.backend.repo.order.OrderRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Log
@Service
@SuppressWarnings("Duplicates")
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    //Retrieves a paginated list of Orders with support for sorting, filtering, searching, and date range.
    public JSONObject getAllOrders(
            int page,
            int size,
            Sort sort,
            Integer status,
            Integer paymentStatus,
            Integer shippingStatus,
            Timestamp startDate,
            Timestamp endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orderPage = orderRepository.findByDateAndStatus(status, paymentStatus, shippingStatus, startDate, endDate, pageable);

        List<JSONObject> orders = new ArrayList<>();
        orderPage.getContent().stream()
                .forEach(oder -> {
                    JSONObject detail = new JSONObject();

                    detail.put("paymentMethod", oder.getPaymentMethod());
                    detail.put("saleType", oder.getSaleType());
                    detail.put("totalPrice", oder.getTotalPrice());
                    detail.put("discount", oder.getDiscount());
                    detail.put("tax", oder.getTax());
                    detail.put("deliveryPrice", oder.getDeliveryPrice());
                    detail.put("orderDate", oder.getOrderDate());
                    detail.put("status", oder.getStatus());
                    detail.put("verifiedBy", oder.getVerifiedBy());
                    detail.put("paymentStatus", oder.getPaymentStatus());
                    detail.put("shippingStatus", oder.getShippingStatus());

                    orders.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", orderPage.getNumber())
                .put("pageSize", orderPage.getSize())
                .put("totalElements", orderPage.getTotalElements())
                .put("totalPages", orderPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("orders", orders).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Order Passed")
                .put("statusMessage", "Order Passed")
                .put("data", data);

        return response;
    }
}
