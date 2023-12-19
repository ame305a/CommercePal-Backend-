package com.commerce.pal.backend.controller.order;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.order.SpecialProductOrderService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/special-order"})
@SuppressWarnings("Duplicates")
public class SpecialProductOrderController {
    private final SpecialProductOrderService specialProductOrderService;

    public SpecialProductOrderController(SpecialProductOrderService specialProductOrderService) {
        this.specialProductOrderService = specialProductOrderService;
    }

    @RequestMapping(value = {"/report"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ResponseEntity<?> getAllSpecialProductOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {

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

        } catch (Exception e) {
            log.log(Level.WARNING, "SPECIAL PRODUCT ORDER REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }

}
