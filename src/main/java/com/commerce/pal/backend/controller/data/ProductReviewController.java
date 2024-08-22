package com.commerce.pal.backend.controller.data;

import com.commerce.pal.backend.module.product.ProductReviewService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/product-reviews"})
@SuppressWarnings("Duplicates")
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProductReview(@RequestBody String reqBdy) {
        JSONObject response = productReviewService.addProductReview(reqBdy);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/by-product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getReviewsByProductId(@PathVariable Long productId) {
        JSONObject response = productReviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping(value = "/increment-helpful/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> incrementHelpfulCount(@PathVariable Long reviewId) {
        JSONObject response = productReviewService.incrementHelpfulCount(reviewId);
        return ResponseEntity.ok(response.toString());
    }
}
