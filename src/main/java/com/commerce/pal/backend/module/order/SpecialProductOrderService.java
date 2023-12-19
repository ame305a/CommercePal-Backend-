package com.commerce.pal.backend.module.order;


import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.order.SpecialProductOrder;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.repo.order.SpecialProductOrderRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@Service
@SuppressWarnings("Duplicates")
public class SpecialProductOrderService {

    private final SpecialProductOrderRepository specialProductOrdersRepository;
    private final CustomerRepository customerRepository;

    public SpecialProductOrderService(SpecialProductOrderRepository specialProductOrdersRepository, CustomerRepository customerRepository) {
        this.specialProductOrdersRepository = specialProductOrdersRepository;
        this.customerRepository = customerRepository;
    }

    //Retrieves a paginated list of Special Product Orders with support for sorting, filtering, searching, and date range.
    public JSONObject getAllSpecialProductOrders(int page, int size, Sort sort, Integer status, String searchKeyword, Timestamp startDate, Timestamp endDate) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SpecialProductOrder> specialProductOrdersPage = specialProductOrdersRepository.findByFilterAndDateAndStatus(searchKeyword, startDate, endDate, status, pageable);

        List<JSONObject> specialProductOrders = new ArrayList<>();
        specialProductOrdersPage.getContent().stream()
                .forEach(specialProductOrder -> {
                    JSONObject detail = new JSONObject();

                    String customerName = "";
                    Optional<Customer> optionalCustomer = customerRepository.findById(specialProductOrder.getUserId());
                    if (optionalCustomer.isPresent()) {
                        Customer customer = optionalCustomer.get();
                        customerName = customer.getFirstName() + " " + customer.getMiddleName() + " " + customer.getLastName();
                    }

                    detail.put("customerName", customerName);
                    detail.put("productName", specialProductOrder.getProductName());
                    detail.put("estimatePrice", specialProductOrder.getEstimatePrice());
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
}
