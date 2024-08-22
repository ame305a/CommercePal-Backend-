package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.dto.product.flashSale.FlashSaleDTO;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleStatus;
import com.commerce.pal.backend.module.product.flashSale.FlashSaleService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/product/flash-sales"})
@SuppressWarnings("Duplicates")
public class ProductFlashSaleController {
    private final FlashSaleService flashSaleService;

    public ProductFlashSaleController(FlashSaleService flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProductPromoCode(@RequestBody @Valid FlashSaleDTO flashSaleDTO) {
        JSONObject response = flashSaleService.addFlashSaleProduct(flashSaleDTO);
        return ResponseEntity.ok(response.toString());
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateFlashSale(@PathVariable Long id, @RequestBody FlashSaleDTO flashSaleDTO) {
        JSONObject response = flashSaleService.updateFlashSale(id, flashSaleDTO);
        return ResponseEntity.ok(response.toString());
    }

    @PutMapping(value = "/{id}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> activateProductPromoCode(@PathVariable Long id) {
        JSONObject response = flashSaleService.activateFlashSale(id);
        return ResponseEntity.ok(response.toString());
    }

    @PutMapping(value = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cancelProductPromoCode(@PathVariable Long id) {
        JSONObject response = flashSaleService.cancelFlashSale(id);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/merchant", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFlashSalesForCurrentMerchant(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) FlashSaleStatus status) {
        JSONObject response = flashSaleService.getMerchantFlashSales(page, size, sortDirection, status);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllFlashSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) FlashSaleStatus status) {
        JSONObject response = flashSaleService.getAllFlashSales(page, size, sortDirection, status);
        return ResponseEntity.ok(response.toString());
    }

}


