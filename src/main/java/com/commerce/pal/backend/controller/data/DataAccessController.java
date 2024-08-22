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
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/data"})
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
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

    @GetMapping(value = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long parentCategory,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer city,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        JSONObject responseMap = new JSONObject();
        if (city != null && merchantId != null) {
            responseMap.put("statusCode", HttpStatus.BAD_REQUEST)
                    .put("statusDescription", "Invalid Request")
                    .put("statusMessage", "Filtering by 'city' and 'merchant' simultaneously is not supported. Choose one or the other.");
            return ResponseEntity.ok(responseMap.toString());
        }

        if (productType != null) {
            if (!(productType.equalsIgnoreCase("RETAIL") || productType.equalsIgnoreCase("SPECIAL-ORDER") ||
                    productType.equalsIgnoreCase("WHOLESALE"))) {
                responseMap.put("statusCode", HttpStatus.BAD_REQUEST)
                        .put("statusDescription", "Invalid Request")
                        .put("statusMessage", "Product type must be either 'RETAIL', 'WHOLESALE' or 'SPECIAL-ORDER'");
                return ResponseEntity.ok(responseMap.toString());
            }
        }

        // If requestEndDate is not provided, set it to the current timestamp
        if (requestEndDate == null)
            requestEndDate = Timestamp.from(Instant.now());

        // Default to ascending order if sortDirection is not provided or is invalid
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
            direction = Sort.Direction.DESC;

        Sort sort = Sort.by(direction, sortBy);

        JSONObject response = productService.getAllProducts(page, size, sort, category, parentCategory, productType, status, merchantId, city, searchKeyword, requestStartDate, requestEndDate);
        return ResponseEntity.ok(response.toString());
    }


//    @GetMapping(value = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> getProducts1(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "asc") String sortDirection,
//            @RequestParam(defaultValue = "productName") String sortBy,
//            @RequestParam Optional<Integer> status,
//            @RequestParam Optional<Long> category,
//            @RequestParam Optional<Long> parentCategory,
//            @RequestParam Optional<String> productType,
//            @RequestParam Optional<String> searchKeyword,
//            @RequestParam Optional<Long> merchantId,
//            @RequestParam Optional<Integer> city,
//            @RequestParam Optional<Timestamp> requestStartDate,
//            @RequestParam Optional<Timestamp> requestEndDate
//    ) {
//        JSONObject responseMap = new JSONObject();
//        if (city.isPresent() && merchantId.isPresent()) {
//            responseMap.put("statusCode", HttpStatus.BAD_REQUEST)
//                    .put("statusDescription", "Invalid Request")
//                    .put("statusMessage", "Filtering by 'city' and 'merchant' simultaneously is not supported. Choose one or the other.");
//            return ResponseEntity.ok(responseMap.toString());
//        }
//
//        // If requestEndDate is not provided, set it to the current timestamp
//        if (requestEndDate.isEmpty())
//            requestEndDate = Optional.of(Timestamp.from(Instant.now()));
//
//        // Default to ascending order if sortDirection is not provided or is invalid
//        Sort.Direction direction = Sort.Direction.ASC;
//        if (sortDirection.equalsIgnoreCase("desc"))
//            direction = Sort.Direction.DESC;
//
//        Sort sort = Sort.by(direction, sortBy);
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        List<Long> cityMerchantIds = new ArrayList<>();
//        city.ifPresent(value -> merchantRepository.findMerchantByCity(value)
//                .forEach(merchant -> cityMerchantIds.add(merchant.getMerchantId())));
//
//        Page<Product> products = productRepository.findAll((Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            status.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("status"), value)));
//            category.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("productCategoryId"), value)));
//            parentCategory.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("productParentCateoryId"), value)));
//            merchantId.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("merchantId"), value)));
//            productType.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("productType"), value)));
//            searchKeyword.ifPresent(value -> predicates.add(criteriaBuilder.like(root.get("productName"), value)));
//            requestStartDate.ifPresent(value -> predicates.add(criteriaBuilder.between(root.get("createdDate"), value, requestEndDate.get())));
//            city.ifPresent(value -> predicates.add(criteriaBuilder.in(root.get("MerchantId"), cityMerchantIds)));
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        }, pageable);
//
//
//        @Query(value = "SELECT * FROM Product p WHERE 1=1 " +
//                "AND (:filterByCategory IS NULL OR p.ProductCategoryId = :filterByCategory) " +
//                "AND (:searchKeyword IS NULL OR LOWER(p.ProductName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.ShortDescription) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
//                "AND (:startDate IS NULL OR p.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
//                "AND (:status IS NULL OR p.Status = :status)" +
//                "AND (:merchantId IS NULL OR p.MerchantId = :merchantId)" +
//                "AND (:city is NULL OR p.MerchantId IN :cityMerchantIds)",
//                nativeQuery = true)
//        Page<Product> findByFilterAndMerchantAndDateAndStatus (
//                @Param("filterByCategory") Long filterByCategory,
//                @Param("searchKeyword") String searchKeyword,
//                @Param("startDate") Timestamp startDate,
//                @Param("endDate") Timestamp endDate,
//                @Param("status") Integer status,
//                @Param("merchantId") Long merchantId,
//                @Param("city") Integer city,
//                @Param("cityMerchantIds") List < Long > cityMerchantIds,
//                Pageable pageable);
//
//
////        JSONObject response = productService.getAllProducts1(pageable, category, status, merchantId, city, requestStartDate, requestEndDate);
//        return ResponseEntity.ok(response.toString());
//    }
}
