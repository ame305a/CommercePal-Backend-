package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.product.ProductSearchService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/products/search"})
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class ProductSearchController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductSearchService productSearchService;

    @GetMapping(value = "/by-keyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchProducts(@RequestParam("keyword") String keyword) {
        JSONObject responseMap = new JSONObject();

        List<JSONObject> details = new ArrayList<>();
        productRepository.findProductByProductId(keyword, keyword, keyword, keyword, "RETAIL")
                .forEach(pro -> {
                    JSONObject detail = productService.getProductListDetailsAlready(pro);
                    details.add(detail);
                });
        if (details.isEmpty()) {
            responseMap.put("statusCode", ResponseCodes.NOT_EXIST);
        } else {
            responseMap.put("statusCode", ResponseCodes.SUCCESS);
        }
        responseMap.put("statusDescription", "success")
                .put("details", details)
                .put("statusMessage", "Request Successful");
        return ResponseEntity.ok(responseMap.toString());
    }

    @GetMapping(value = "/by-price-range", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductsByPriceRange(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam() BigDecimal minPrice,
            @RequestParam() BigDecimal maxPrice) {
        JSONObject response = productSearchService.searchProductsByPriceRange(page, size, sortDirection, subCategoryId, minPrice, maxPrice);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/newly-added", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getNewlyAddedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) String timestamp) {
        JSONObject response = productSearchService.getNewlyAddedProducts(page, size, subCategoryId, timestamp);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/under-price-threshold/random", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRandomProductsUnder1000(
            @RequestParam(name = "priceThreshold", defaultValue = "1000") BigDecimal priceThreshold,
            @RequestParam(name = "count", defaultValue = "24") int count) {
        JSONObject response = productSearchService.getRandomProductsUnder1000(priceThreshold, count);
        return ResponseEntity.ok(response.toString());
    }

}
