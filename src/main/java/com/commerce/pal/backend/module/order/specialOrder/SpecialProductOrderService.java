package com.commerce.pal.backend.module.order.specialOrder;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.order.SpecialProductOrder;
import com.commerce.pal.backend.models.order.SpecialProductOrderBid;
import com.commerce.pal.backend.models.product.categories.ProductSubCategory;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.repo.order.SpecialProductOrderBidRepository;
import com.commerce.pal.backend.repo.order.SpecialProductOrderRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@Service
@RequiredArgsConstructor
public class SpecialProductOrderService {
    private final GlobalMethods globalMethods;
    private final SpecialProductOrderBidRepository specialProductOrderBidRepository;
    private final SpecialProductOrderRepository specialProductOrderRepository;
    private final UploadService uploadService;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final CustomerRepository customerRepository;
    private final MerchantService merchantService;

    public JSONObject requestOrder(String request) {
        JSONObject responseMap = new JSONObject();
        JSONObject reqBody = new JSONObject(request);
        LoginValidation user = globalMethods.fetchUserDetails();
        SpecialProductOrder productOrder = new SpecialProductOrder();
        productOrder.setUserType("C");
        productOrder.setUserId(globalMethods.getCustomerId(user.getEmailAddress()));
        productOrder.setProductSubCategoryId(reqBody.getLong("subCategoryId"));
        productOrder.setProductName(reqBody.getString("ProductName"));
        productOrder.setProductDescription(reqBody.getString("Description"));
        productOrder.setEstimatePrice(reqBody.optBigDecimal("EstimatePrice", new BigDecimal(0.00)));
        productOrder.setQuantity(reqBody.getInt("quantity"));
        productOrder.setLinkToProduct(reqBody.optString("LinkToProduct", null));
        productOrder.setStatus(0);
        productOrder.setRequestDate(Timestamp.from(Instant.now()));
        specialProductOrderRepository.save(productOrder);
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("specialOrderId", productOrder.getId())
                .put("statusMessage", "Request processed successful.");
        return responseMap;
    }

    public JSONObject uploadSpecialOrderImage(Long specialOrderId, MultipartFile file) {
        JSONObject response = new JSONObject();
        log.log(Level.INFO, "File Name :" + file.getName());

        SpecialProductOrder specialProductOrder = getById(specialOrderId);
        String productUrl = uploadService.uploadFileAlone(file, "Web", "SpecialOrder");
        if (specialProductOrder.getImageOne() == null) {
            specialProductOrder.setImageOne(productUrl);
        } else if (specialProductOrder.getImageTwo() == null) {
            specialProductOrder.setImageTwo(productUrl);
        } else if (specialProductOrder.getImageThree() == null) {
            specialProductOrder.setImageThree(productUrl);
        } else if (specialProductOrder.getImageFour() == null) {
            specialProductOrder.setImageFour(productUrl);
        } else if (specialProductOrder.getImageFive() == null) {
            specialProductOrder.setImageFive(productUrl);
        }
        specialProductOrderRepository.save(specialProductOrder);
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("productUrl", productUrl)
                .put("statusDescription", "success")
                .put("statusMessage", "Request Successful");

        return response;
    }

    public JSONObject getSpecialOrderById(Long specialOrderId) {
        SpecialProductOrder specialProductOrder = getById(specialOrderId);

        JSONObject payload = specialOrderJsonObject(specialProductOrder);
        payload.put("specialOrderId", specialProductOrder.getId());
        payload.put("subCategoryName", subCategoryName(specialProductOrder));
        payload.put("description", specialProductOrder.getProductDescription());

        JSONObject responseMap = new JSONObject();
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", payload)
                .put("statusDescription", "success")
                .put("statusMessage", "Request Successful");

        return responseMap;
    }

    public JSONObject getCustomerSpecialOrders() {
        JSONObject responseMap = new JSONObject();

        LoginValidation user = globalMethods.fetchUserDetails();

        List<JSONObject> orders = new ArrayList<>();
        specialProductOrderRepository
                .findSpecialProductOrdersByUserTypeAndUserId("C", globalMethods.getCustomerId(user.getEmailAddress()))
                .forEach(order -> {

                    JSONObject payload = specialOrderJsonObject(order);
                    payload.put("specialOrderId", order.getId());
                    payload.put("subCategoryName", subCategoryName(order));
                    payload.put("description", order.getProductDescription());
                    orders.add(payload);
                });

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", orders)
                .put("statusDescription", "success")
                .put("statusMessage", "Request Successful");

        return responseMap;
    }

    public JSONObject getSpecialProductOrders(int page, int size, String sortDirection, Long productSubCategoryId,
                                              Integer status, String startDate, String endDate) {

        Timestamp startTimestamp = null;
        Timestamp endTimestamp = null;
        if (startDate != null) {
            startTimestamp = GlobalMethods.parseTimestampFromDateString(startDate);
            endTimestamp = endDate != null ? GlobalMethods.parseTimestampFromDateString(endDate) : Timestamp.from(Instant.now());
        }

        // Default to desc if sortDirection is not provided or invalid
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("asc"))
            direction = Sort.Direction.ASC;

        Sort sort = Sort.by(direction, "requestDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SpecialProductOrder> specialProductOrderPage = specialProductOrderRepository
                .findByFilterAndDateAndStatus(null, startTimestamp, endTimestamp, status, productSubCategoryId, pageable);

        List<JSONObject> specialProductOrders = new ArrayList<>();
        specialProductOrderPage.getContent().stream()
                .forEach(specialProductOrder -> {

                    JSONObject detail = specialOrderJsonObject(specialProductOrder);
                    detail.put("subCategoryName", subCategoryName(specialProductOrder));

                    specialProductOrders.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", specialProductOrderPage.getNumber())
                .put("pageSize", specialProductOrderPage.getSize())
                .put("totalElements", specialProductOrderPage.getTotalElements())
                .put("totalPages", specialProductOrderPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("specialOrders", specialProductOrders).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Request processed successfully.")
                .put("statusMessage", "Request processed successfully.")
                .put("data", data);

        return response;
    }

    public JSONObject processMerchantAssignmentAndNotification(String requestBody) {
        JSONObject responseMap = new JSONObject();

        JSONObject requestJson = new JSONObject(requestBody);
        JSONArray merchantsIdArray = requestJson.getJSONArray("merchantsId");

        for (Object merchantId : merchantsIdArray) {
            Long merchantIdLong = Long.valueOf(merchantId.toString());

            SpecialProductOrderBid specialProductOrderBid = new SpecialProductOrderBid();
            specialProductOrderBid.setSpecialOrderId(requestJson.getLong("specialOrderId"));
            specialProductOrderBid.setMerchantId(merchantIdLong);
            specialProductOrderBid.setAssignedDate(Timestamp.from(Instant.now()));
            specialProductOrderBidRepository.save(specialProductOrderBid);
        }

        // send sms, email and push notifications to merchants
        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Merchants Assigned Successfully");
        return responseMap;
    }


    //Retrieves a paginated list of Special Product Orders with support for sorting, filtering, searching, and date range.
    public JSONObject getAllSpecialProductOrders(int page, int size, Sort sort, Integer status, String
            searchKeyword, Timestamp startDate, Timestamp endDate) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SpecialProductOrder> specialProductOrdersPage = specialProductOrderRepository.findByFilterAndDateAndStatus(searchKeyword, startDate, endDate, status, null, pageable);

        List<JSONObject> specialProductOrders = new ArrayList<>();
        specialProductOrdersPage.getContent().stream()
                .forEach(specialProductOrder -> {
                    String customerName = "";
                    Optional<Customer> optionalCustomer = customerRepository.findById(specialProductOrder.getUserId());
                    if (optionalCustomer.isPresent()) {
                        Customer customer = optionalCustomer.get();
                        customerName = customer.getFirstName() + " " + customer.getMiddleName() + " " + customer.getLastName();
                    }

                    JSONObject detail = specialOrderJsonObject(specialProductOrder);
                    detail.put("customerName", customerName);
                    specialProductOrders.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", specialProductOrdersPage.getNumber())
                .put("pageSize", specialProductOrdersPage.getSize())
                .put("totalElements", specialProductOrdersPage.getTotalElements())
                .put("totalPages", specialProductOrdersPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("specialProductOrders", specialProductOrders).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Special Product Order Passed")
                .put("statusMessage", "Special Product Order Passed")
                .put("data", data);

        return response;
    }


    private JSONObject specialOrderJsonObject(SpecialProductOrder specialProductOrder) {
        JSONObject detail = new JSONObject();
        detail.put("specialOrderId", specialProductOrder.getId());
        detail.put("customerId", specialProductOrder.getUserId());
        detail.put("productName", specialProductOrder.getProductName());
        detail.put("estimatePrice", specialProductOrder.getEstimatePrice());
        detail.put("quantity", specialProductOrder.getQuantity());
        detail.put("status", specialProductOrder.getStatus());
        detail.put("requestDate", specialProductOrder.getRequestDate());
        detail.put("uploadedBy", specialProductOrder.getUploadedBy());
        detail.put("uploadedDate", specialProductOrder.getUploadedDate());
        detail.put("linkToProduct", specialProductOrder.getLinkToProduct() != null ? specialProductOrder.getLinkToProduct() : "");
        detail.put("imageOne", specialProductOrder.getImageOne() != null ? specialProductOrder.getImageOne() : "");
        detail.put("imageTwo", specialProductOrder.getImageTwo() != null ? specialProductOrder.getImageTwo() : "");
        detail.put("imageThree", specialProductOrder.getImageThree() != null ? specialProductOrder.getImageThree() : "");
        detail.put("imageFour", specialProductOrder.getImageFour() != null ? specialProductOrder.getImageFour() : "");
        detail.put("imageFive", specialProductOrder.getImageFive() != null ? specialProductOrder.getImageFive() : "");
        return detail;
    }

    public SpecialProductOrder getById(Long specialOrderId) {
        return specialProductOrderRepository.findById(specialOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Special order not found."));
    }


    public String subCategoryName(SpecialProductOrder specialProductOrder) {
        String subCategoryName = "";
        //productSubCategoryId is added recently added -> there are records with null values
        if (specialProductOrder.getProductSubCategoryId() != null) {
            Optional<ProductSubCategory> subCategory = productSubCategoryRepository.findById(specialProductOrder.getProductSubCategoryId());
            if (subCategory.isPresent())
                subCategoryName = subCategory.get().getSubCategoryName();
        }

        return subCategoryName;
    }

    public JSONObject getMerchantsAssignedToSpecialOrder(Long specialOrderId) {
        JSONObject responseMap = new JSONObject();
        List<JSONObject> bidList = new ArrayList<>();

        specialProductOrderBidRepository.findBySpecialOrderId(specialOrderId)
                .forEach(specialProductOrderBid -> {
                    JSONObject bidInfo = new JSONObject();
                    bidInfo.put("bidId", specialProductOrderBid.getBidId());
                    bidInfo.put("isMerchantAccepted", specialProductOrderBid.getIsMerchantAccepted());
                    bidInfo.put("merchantResponseDate", specialProductOrderBid.getMerchantResponseDate());
                    JSONObject merchantInfo = merchantService.getMerchantInfo(specialProductOrderBid.getMerchantId());
                    bidInfo.put("merchant", merchantInfo);
                    bidList.add(bidInfo);
                });

        return responseMap
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("data", bidList)
                .put("statusDescription", "Success")
                .put("statusMessage", "Request processed successfully");
    }

}
