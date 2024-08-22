package com.commerce.pal.backend.module.order;


import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.order.Order;
import com.commerce.pal.backend.models.order.OrderFeedback;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.repo.order.OrderFeedbackRepository;
import com.commerce.pal.backend.repo.order.OrderRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@Service
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderFeedbackRepository orderFeedbackRepository;
    private final CustomerRepository customerRepository;


    //Retrieves a paginated list of Orders with support for sorting, filtering, searching, and date range.
    public JSONObject getOrderReport(
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
        orderPage.getContent().forEach(order -> {
            JSONObject detail = new JSONObject();

            detail.put("paymentMethod", order.getPaymentMethod());
            detail.put("saleType", order.getSaleType());
            detail.put("totalPrice", order.getTotalPrice());
            detail.put("discount", order.getDiscount());
            detail.put("tax", order.getTax());
            detail.put("deliveryPrice", order.getDeliveryPrice());
            detail.put("orderDate", order.getOrderDate());
            detail.put("status", order.getStatus());
            detail.put("verifiedBy", order.getVerifiedBy());
            detail.put("paymentStatus", order.getPaymentStatus());
            detail.put("shippingStatus", order.getShippingStatus());

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

    public JSONObject getUnsuccessfulOrders(Integer customerContacted, Timestamp requestStartDate, Timestamp requestEndDate, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findUnsuccessfulOrders(customerContacted, requestStartDate, requestEndDate, pageable);
        List<JSONObject> orders = new ArrayList<>();
        orderPage.getContent()
                .forEach(order -> {
                    String contacted = order.getCustomerContacted() != null && order.getCustomerContacted() == 1
                            ? "Yes" : "No";

                    String customerName = "Unknown";
                    Optional<Customer> customer = customerRepository.findById(order.getCustomerId());
                    if (customer.isPresent())
                        customerName = customer.get().getFirstName() + " " + customer.get().getLastName();

                    JSONObject orderInfo = new JSONObject();
                    orderInfo.put("OrderId", order.getOrderId());
                    orderInfo.put("customerId", order.getCustomerId());
                    orderInfo.put("customerName", customerName);
                    orderInfo.put("OrderRef", order.getOrderRef());
                    orderInfo.put("OrderDate", order.getOrderDate());
                    orderInfo.put("DeliveryPrice", order.getDeliveryPrice());
                    orderInfo.put("TotalPrice", order.getTotalPrice());
                    orderInfo.put("PaymentStatus", order.getPaymentStatus());
                    orderInfo.put("PaymentDate", order.getPaymentDate());
                    orderInfo.put("PaymentMethod", order.getPaymentMethod());
                    orderInfo.put("Discount", order.getDiscount());
                    orderInfo.put("DeliveryPrice", order.getDeliveryPrice());
                    orderInfo.put("customerContacted", contacted);
                    orders.add(orderInfo);
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

    public JSONObject getFailedOrdersCustomerFeedback(Timestamp requestStartDate, Timestamp requestEndDate, Pageable pageable) {
        Page<OrderFeedback> orderPage = orderFeedbackRepository.findByFeedbackDateBetween(requestStartDate, requestEndDate, pageable);
        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", orderPage.getNumber())
                .put("pageSize", orderPage.getSize())
                .put("totalElements", orderPage.getTotalElements())
                .put("totalPages", orderPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("feedbacks", orderPage.getContent()).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Failed Order Feedback Passed")
                .put("statusMessage", "Failed Order Feedback Passed")
                .put("data", data);

        return response;
    }

    public JSONObject addOrderFeedback(@RequestBody String reqBdy) {
        JSONObject responseMap = new JSONObject();

        JSONObject request = new JSONObject(reqBdy);

        orderRepository.findById(request.getLong("orderId"))
                .ifPresentOrElse(order -> {
                    order.setCustomerContacted(1);
                    order.setCustomerContactDate(Timestamp.from(Instant.now()));
                    orderRepository.save(order);

                    OrderFeedback orderFeedback = new OrderFeedback();
                    orderFeedback.setOrderId(request.getLong("orderId"));
                    orderFeedback.setCustomerId(order.getCustomerId());
                    orderFeedback.setFailureReasonId(request.getLong("failureReasonId"));
                    orderFeedback.setFeedbackText(request.has("feedbackText") ? request.getString("feedbackText") : null);
                    orderFeedback.setFeedbackDate(Timestamp.from(Instant.now()));
                    orderFeedback.setStatus(1);
                    orderFeedbackRepository.save(orderFeedback);

                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "Request processed successfully.")
                            .put("statusMessage", "Request processed successfully.");

                }, () -> responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "Failed to process Request.")
                        .put("statusMessage", "Order not found"));

        return responseMap;
    }

}
