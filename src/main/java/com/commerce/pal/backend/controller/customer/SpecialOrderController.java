package com.commerce.pal.backend.controller.customer;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.order.SpecialProductOrder;
import com.commerce.pal.backend.repo.order.SpecialProductOrderRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/customer/order/special"})
@SuppressWarnings("Duplicates")
public class SpecialOrderController {
    private final GlobalMethods globalMethods;
    private final SpecialProductOrderRepository specialProductOrderRepository;

    @Autowired
    public SpecialOrderController(GlobalMethods globalMethods,
                                  SpecialProductOrderRepository specialProductOrderRepository) {
        this.globalMethods = globalMethods;
        this.specialProductOrderRepository = specialProductOrderRepository;
    }

    @RequestMapping(value = "/request-order", method = RequestMethod.POST)
    public ResponseEntity<?> requestOrder(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();
            SpecialProductOrder productOrder = new SpecialProductOrder();
            productOrder.setUserType("C");
            productOrder.setUserId(globalMethods.getCustomerId(user.getEmailAddress()));
            productOrder.setProductName(reqBody.getString("ProductName"));
            productOrder.setProductDescription(reqBody.getString("Description"));
            productOrder.setEstimatePrice(reqBody.has("EstimatePrice") ? reqBody.getBigDecimal("EstimatePrice") : new BigDecimal(0.00));
            productOrder.setLinkToProduct(reqBody.getString("LinkToProduct"));
            productOrder.setStatus(0);
            productOrder.setRequestDate(Timestamp.from(Instant.now()));
            specialProductOrderRepository.save(productOrder);
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("specialOrderId", productOrder.getId())
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            log.log(Level.WARNING, "Special Order ERROR : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/my-request-order", method = RequestMethod.GET)
    public ResponseEntity<?> getSpecialOrders() {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();

            List<JSONObject> orders = new ArrayList<>();
            specialProductOrderRepository.findSpecialProductOrdersByUserTypeAndUserId(
                    "C", globalMethods.getCustomerId(user.getEmailAddress())
            ).forEach(order -> {
                JSONObject payload = new JSONObject();
                payload.put("Id", order.getId());
                payload.put("ProductName", order.getProductName());
                payload.put("Description", order.getProductDescription());
                payload.put("LinkToProduct", order.getLinkToProduct());
                payload.put("ImageOne", order.getImageOne());
                payload.put("ImageTwo", order.getImageTwo());
                payload.put("ImageThree", order.getImageThree());
                payload.put("ImageFour", order.getImageFour());
                payload.put("ImageFive", order.getImageFive());
                payload.put("Status", order.getStatus());
                payload.put("RequestDate", order.getRequestDate());
                orders.add(payload);
            });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("data", orders)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");

        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }
}
