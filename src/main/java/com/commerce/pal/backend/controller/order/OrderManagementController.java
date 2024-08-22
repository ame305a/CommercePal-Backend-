package com.commerce.pal.backend.controller.order;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.order.OrderFailureReasonService;
import com.commerce.pal.backend.module.order.OrderService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/portal/orders"})
@SuppressWarnings("Duplicates")
public class OrderManagementController {
    private final OrderService orderService;
    private final OrderFailureReasonService orderFailureReasonService;

    public OrderManagementController(OrderService orderService, OrderFailureReasonService orderFailureReasonService) {
        this.orderService = orderService;
        this.orderFailureReasonService = orderFailureReasonService;
    }

    @GetMapping(value = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "OrderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Integer shippingStatus,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to descending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.DESC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
                direction = Sort.Direction.ASC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = orderService.getOrderReport(page, size, sort, status, paymentStatus, shippingStatus, requestStartDate, requestEndDate);
            return ResponseEntity.ok(response.toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "ORDER REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Failed to process request")
                    .put("statusMessage", "Internal system error");

            return ResponseEntity.ok(responseMap.toString());
        }
    }

    @RequestMapping(value = "/unsuccessful", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> customerOrders(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(defaultValue = "desc") String sortDirection,
                                                 @RequestParam(required = false) Integer customerContacted,
                                                 @RequestParam(required = false) Timestamp requestStartDate,
                                                 @RequestParam(required = false) Timestamp requestEndDate) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to descending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.DESC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
                direction = Sort.Direction.ASC;

            Sort sort = Sort.by(direction, "OrderDate");
            Pageable pageable = PageRequest.of(page, size, sort);

            JSONObject response = orderService.getUnsuccessfulOrders(customerContacted, requestStartDate, requestEndDate, pageable);
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "UNSUCCESSFUL ORDER REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Failed to process request")
                    .put("statusMessage", "Internal system error");

            return ResponseEntity.ok(responseMap.toString());
        }
    }

    @GetMapping(value = "/unsuccessful/customer-feedbacks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFeedbacks(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "desc") String sortDirection,
                                               @RequestParam(required = false) Timestamp requestStartDate,
                                               @RequestParam(required = false) Timestamp requestEndDate) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to descending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.DESC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
                direction = Sort.Direction.ASC;

            Sort sort = Sort.by(direction, "feedbackDate");
            Pageable pageable = PageRequest.of(page, size, sort);

            JSONObject response = orderService.getFailedOrdersCustomerFeedback(requestStartDate, requestEndDate, pageable);
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "FAILED ORDER FEEDBACK: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.ok(responseMap.toString());
        }
    }

    @PostMapping(value = "/unsuccessful/add-feedback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addOrderFeedback(@RequestBody String reqBdy) {
        JSONObject response = orderService.addOrderFeedback(reqBdy);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/failure-reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOrderFailureReasons() {
        JSONObject response = orderFailureReasonService.getOrderFailureReasons();
        return ResponseEntity.ok(response.toString());
    }
}
