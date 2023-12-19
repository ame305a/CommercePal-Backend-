package com.commerce.pal.backend.controller.data;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.DistributorService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.module.users.AgentService;
import com.commerce.pal.backend.module.users.CustomerService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.module.users.MessengerService;
import com.commerce.pal.backend.module.users.business.BusinessCollateralService;
import com.commerce.pal.backend.module.users.business.BusinessService;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping({"/prime/api/v1/data"})
@SuppressWarnings("Duplicates")
public class DataAccessController {
    private final AgentService agentService;
    private final ProductService productService;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final MerchantService merchantService;
    private final DistributorService distributorService;
    private final MessengerService messengerService;
    private final SubProductService subProductService;
    private final BusinessCollateralService businessCollateralService;
    private final LoginValidationRepository loginValidationRepository;

    @Autowired
    public DataAccessController(AgentService agentService,
                                ProductService productService,
                                BusinessService businessService,
                                CustomerService customerService,
                                MerchantService merchantService,
                                DistributorService distributorService, MessengerService messengerService,
                                SubProductService subProductService,
                                BusinessCollateralService businessCollateralService,
                                LoginValidationRepository loginValidationRepository) {
        this.agentService = agentService;
        this.productService = productService;
        this.businessService = businessService;
        this.customerService = customerService;
        this.merchantService = merchantService;
        this.distributorService = distributorService;
        this.messengerService = messengerService;
        this.subProductService = subProductService;
        this.businessCollateralService = businessCollateralService;
        this.loginValidationRepository = loginValidationRepository;
    }

    @RequestMapping(value = {"/request"}, method = {RequestMethod.POST}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> getDataRequest(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
            JSONObject jsonObject = new JSONObject(request);
            switch (jsonObject.getString("Type")) {
                case "PRODUCT":
                    responseMap = productService.getProductLimitedDetails(jsonObject.getLong("TypeId"));
                    break;
                case "SUB-PRODUCT":
                    responseMap = subProductService.getSubProductInfo(jsonObject.getLong("TypeId"), "ETB");
                    break;
                case "PRODUCT-AND-SUB":
                    responseMap = productService.getSubProductInfo(jsonObject.getLong("TypeId"), jsonObject.getLong("SubProductId"));
                    break;
                case "AGENT":
                    responseMap = agentService.getAgentInfo(jsonObject.getLong("TypeId"));
                    break;
                case "CUSTOMER":
                    responseMap = customerService.getCustomerInfo(jsonObject.getLong("TypeId"));
                    break;
                case "BUSINESS":
                    responseMap = businessService.getBusinessInfo(jsonObject.getLong("TypeId"));
                    break;
                case "BUSINESS-LOAN-LIMIT":
                    responseMap = businessCollateralService.getLoanLimit(jsonObject.getLong("TypeId"));
                    break;
                case "BUSINESS-COLLATERAL":
                    responseMap = businessCollateralService.getBusinessCollateral(jsonObject.getLong("TypeId"));
                    break;
                case "MERCHANT":
                    responseMap = merchantService.getMerchantInfo(jsonObject.getLong("TypeId"));
                    break;
                case "DISTRIBUTOR":
                    responseMap = distributorService.getDistributorInfo(jsonObject.getLong("TypeId"));
                    break;
                case "MERCHANT-ADDRESS":
                    responseMap = merchantService.getMerchantAddressInfo(jsonObject.getLong("TypeId"));
                    break;
                case "CUSTOMER-ADDRESS":
                    responseMap = customerService.getCustomerAddressById(jsonObject.getLong("TypeId"));
                    break;
                case "MESSENGER":
                    responseMap = messengerService.getMessengerInfo(jsonObject.getLong("TypeId"));
                    break;
                default:
                    responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                            .put("statusDescription", "failed")
                            .put("issueType", jsonObject.getString("Type"))
                            .put("statusMessage", "Request failed");
                    break;


            }

        } catch (Exception ex) {
            responseMap.put("statusCode", ResponseCodes.TRANSACTION_FAILED)
                    .put("statusDescription", "failed")
                    .put("statusMessage", "Request failed");
        }
        return ResponseEntity.ok(responseMap.toString());
    }

    @RequestMapping(value = {"/report"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long filterByCategory,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {

            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by product name in ascending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "productName";

            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = productService.getAllProducts(page, size, sort, filterByCategory, status, merchantId, searchKeyword, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "PRODUCT REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }
}
