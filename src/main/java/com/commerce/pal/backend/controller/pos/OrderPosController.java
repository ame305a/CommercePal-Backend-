package com.commerce.pal.backend.controller.pos;

import com.commerce.pal.backend.module.pos.OrderPOSService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/pos/orders"})
@SuppressWarnings("Duplicates")
public class OrderPosController {
    private final OrderPOSService orderPOSService;

    public OrderPosController(OrderPOSService orderPOSService) {
        this.orderPOSService = orderPOSService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMerchantOrders() {
        List<JSONObject> response = orderPOSService.getMerchantOrders();
        return ResponseEntity.ok(response.toString());
    }

}
