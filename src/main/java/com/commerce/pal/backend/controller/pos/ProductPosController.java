package com.commerce.pal.backend.controller.pos;

import com.commerce.pal.backend.module.pos.ProductPOSService;
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
@RequestMapping({"/prime/api/v1/pos/products"})
@SuppressWarnings("Duplicates")
public class ProductPosController {
    private final ProductPOSService productPOSService;

    public ProductPosController(ProductPOSService productPOSService) {
        this.productPOSService = productPOSService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMerchantProducts() {
        List<JSONObject> response = productPOSService.getMerchantProducts();
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMerchantProductOverview() {
        JSONObject response = productPOSService.getMerchantProductOverview();
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/sold", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMerchantSoldProducts() {
        List<JSONObject> response = productPOSService.getMerchantSoldProducts();
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/discounted", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMerchantDiscountedProducts() {
        List<JSONObject> response = productPOSService.getMerchantDiscountedProducts();
        return ResponseEntity.ok(response.toString());
    }
}
