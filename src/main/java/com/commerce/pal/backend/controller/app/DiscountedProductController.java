package com.commerce.pal.backend.controller.app;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.commerce.pal.backend.module.product.DiscountedProductService;

@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/products"})
@SuppressWarnings("Duplicates")
public class DiscountedProductController {
    private final DiscountedProductService discountedProductService;

    public DiscountedProductController(DiscountedProductService discountedProductService) {
        this.discountedProductService = discountedProductService;
    }

    @GetMapping(value = "/on-flash-sale", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductsOnFlashSale(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        JSONObject response = discountedProductService.getProductsOnFlashSale(page, size);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/top-deals", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTopDealProducts(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        JSONObject response = discountedProductService.getTopDealProducts(page, size);
        return ResponseEntity.ok(response.toString());
    }


}


