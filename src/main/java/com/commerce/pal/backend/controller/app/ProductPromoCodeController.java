package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.dto.product.promotion.ApplyPromoCodeDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeApprovalDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeCancelDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeDTO;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import com.commerce.pal.backend.models.product.promotion.PromoCodeStatus;
import com.commerce.pal.backend.module.product.promotion.ProductPromoCodeService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/product/promo-codes"})
@SuppressWarnings("Duplicates")
public class ProductPromoCodeController {
    private final ProductPromoCodeService productPromoCodeService;

    public ProductPromoCodeController(ProductPromoCodeService productPromoCodeService) {
        this.productPromoCodeService = productPromoCodeService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProductPromoCode(@RequestBody @Valid ProductPromoCodeDTO promoCodeDTO) {
        JSONObject response = productPromoCodeService.addProductPromoCode(promoCodeDTO);
        return ResponseEntity.ok(response.toString());
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductPromoCodesByStatus(@RequestParam(required = false) PromoCodeStatus status,
                                                               @RequestParam(required = false) ProductPromoCodeOwner owner) {
        JSONObject response = productPromoCodeService.getProductPromoCodes(status, owner);
        return ResponseEntity.ok(response.toString());
    }


    @GetMapping(value = "/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductPromoCodesByStatus(@RequestParam PromoCodeStatus status) {
        JSONObject response = productPromoCodeService.getProductPromoCodesByStatus(status);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/by-owner", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductPromoCodes(@RequestParam ProductPromoCodeOwner owner) {
        JSONObject response = productPromoCodeService.getProductPromoCodesByOwner(owner);
        return ResponseEntity.ok(response.toString());
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateProductPromoCode(@PathVariable Long id, @RequestBody ProductPromoCodeDTO promoCodeDTO) {
        JSONObject response = productPromoCodeService.updateProductPromoCode(id, promoCodeDTO);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProductPromoCodeById(@PathVariable Long id) {
        JSONObject response = productPromoCodeService.getProductPromoCodeById(id);
        return ResponseEntity.ok(response.toString());
    }

    @PutMapping(value = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cancelProductPromoCode(@PathVariable Long id, @RequestBody @Valid ProductPromoCodeCancelDTO codeCancelDTO) {
        JSONObject response = productPromoCodeService.cancelProductPromoCode(id, codeCancelDTO);
        return ResponseEntity.ok(response.toString());
    }

    @PutMapping(value = "/{id}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> activateProductPromoCode(@PathVariable Long id, @RequestBody @Valid ProductPromoCodeApprovalDTO approvalDTO) {
        JSONObject response = productPromoCodeService.approveProductPromoCode(id, approvalDTO);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping(value = "/apply", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> applyPromoCode(@RequestBody @Valid ApplyPromoCodeDTO applyPromoCodeDTO) {
        JSONObject response = productPromoCodeService.applyPromoCode(applyPromoCodeDTO);
        return ResponseEntity.ok(response.toString());
    }
}


