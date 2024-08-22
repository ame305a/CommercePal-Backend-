package com.commerce.pal.backend.controller.order;

import com.commerce.pal.backend.module.order.specialOrder.SpecialProductOrderBidService;
import com.commerce.pal.backend.module.order.specialOrder.SpecialProductOrderService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping("/prime/api/v1/special-orders")
@SuppressWarnings("Duplicates")
public class SpecialProductOrderController {
    private final SpecialProductOrderBidService specialProductOrderBidService;
    private final SpecialProductOrderService specialProductOrderService;

    public SpecialProductOrderController(SpecialProductOrderBidService specialProductOrderBidService, SpecialProductOrderService specialProductOrderService) {
        this.specialProductOrderBidService = specialProductOrderBidService;
        this.specialProductOrderService = specialProductOrderService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> requestOrder(@RequestBody String requestBody) {
        return ResponseEntity.ok(specialProductOrderService.requestOrder(requestBody).toString());
    }

    @PostMapping(value = "/upload-image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadSpecialOrderImage(@RequestParam(value = "file") MultipartFile file,
                                                          @RequestParam(value = "orderId") Long specialOrderId) {
        return ResponseEntity.ok(specialProductOrderService.uploadSpecialOrderImage(specialOrderId, file).toString());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpecialProductOrders(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @RequestParam(defaultValue = "desc") String sortDirection,
                                                          @RequestParam(required = false) Long subCategoryId,
                                                          @RequestParam(required = false) Integer status,
                                                          @RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate) {

        JSONObject response = specialProductOrderService.getSpecialProductOrders(page, size, sortDirection, subCategoryId, status, startDate, endDate);
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpecialOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(specialProductOrderService.getSpecialOrderById(id).toString());
    }

    @GetMapping(value = "/my-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCustomerSpecialOrders() {
        return ResponseEntity.ok(specialProductOrderService.getCustomerSpecialOrders().toString());
    }

    @PostMapping(value = "/assign-to-merchants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> assignAndNotifyMerchants(@RequestBody String requestBody) {
        return ResponseEntity.ok(specialProductOrderService.processMerchantAssignmentAndNotification(requestBody).toString());
    }

    @PostMapping(value = "/bids/merchant/respond-to-assignment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> merchantResponse(@RequestBody String requestBody) {
        return ResponseEntity.ok(specialProductOrderBidService.processMerchantResponseToBidAssignment(requestBody).toString());
    }

    @GetMapping(value = "/bids/merchant/assigned-to-me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBidsAssignedToMerchant() {
        return ResponseEntity.ok(specialProductOrderBidService.getBidsAssignedToMerchant().toString());
    }

    @GetMapping(value = "/merchant-bids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpecialOrderBids(@RequestParam Long specialProductOrderId) {
        return ResponseEntity.ok(specialProductOrderBidService.getSpecialOrderBids(specialProductOrderId).toString());
    }

    @PostMapping(value = "/bid/respond-by-customer", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> customerSpecialOrderBidResponse(@RequestBody String requestBody) {
        return ResponseEntity.ok(specialProductOrderBidService.processCustomerResponseToMerchantBid(requestBody).toString());
    }

    @GetMapping(value = "/{specialOrderId}/assigned-merchants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssignedMerchantsForSpecialOrder(@PathVariable Long specialOrderId) {
        String responseJson = specialProductOrderService.getMerchantsAssignedToSpecialOrder(specialOrderId).toString();
        return ResponseEntity.ok(responseJson);
    }

    @RequestMapping(value = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllSpecialProductOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        // If requestEndDate is not provided, set it to the current timestamp
        if (requestEndDate == null)
            requestEndDate = Timestamp.from(Instant.now());

        // Default to sorting by customer 1st name in descending order if sortBy is not provided
        if (sortBy == null || sortBy.isEmpty())
            sortBy = "productName";

        // Default to descending order if sortDirection is not provided or is invalid
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
            direction = Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        JSONObject response = specialProductOrderService.getAllSpecialProductOrders(page, size, sort, status, searchKeyword, requestStartDate, requestEndDate);
        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

}
