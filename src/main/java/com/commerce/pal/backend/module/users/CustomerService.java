package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.repo.user.CustomerAddressRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Log
@Service
@SuppressWarnings("Duplicates")
public class CustomerService {
    private final GlobalMethods globalMethods;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @Autowired
    public CustomerService(GlobalMethods globalMethods,
                           CustomerRepository customerRepository,
                           CustomerAddressRepository customerAddressRepository) {
        this.globalMethods = globalMethods;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    public JSONObject getMultiUserCustomer(String email, String phoneNumber) {
        JSONObject customerData = new JSONObject();
        customerRepository.findByEmailAddressOrPhoneNumber(email, phoneNumber)
                .ifPresent(customer -> {
                    customerData.put("firstName", customer.getFirstName());
                    customerData.put("lastName", customer.getLastName());
                    customerData.put("language", customer.getLanguage());
                    customerData.put("phoneNumber", customer.getPhoneNumber());
                    customerData.put("email", customer.getEmailAddress());
                });

        return customerData;
    }

    public JSONObject getCustomerInfo(Long customerId) {
        JSONObject payload = new JSONObject();
        customerRepository.findCustomerByCustomerId(customerId)
                .ifPresent(customer -> {
                    payload.put("firstName", customer.getFirstName());
                    payload.put("lastName", customer.getLastName());
                    payload.put("language", customer.getLanguage());
                    payload.put("phoneNumber", customer.getPhoneNumber());
                    payload.put("email", customer.getEmailAddress());
                });
        return payload;
    }

    public JSONObject getCustomerAddressById(Long addressId) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        customerAddressRepository.findById(addressId)
                .ifPresent(customerAddress -> {
                    payload.get().put("id", customerAddress.getId());
                    payload.get().put("regionId", customerAddress.getRegionId());
                    payload.get().put("country", customerAddress.getCountry());
                    payload.get().put("city", customerAddress.getCity());
                    payload.get().put("cityName", globalMethods.cityName(Long.valueOf(customerAddress.getCity())));
                    payload.get().put("subCity", customerAddress.getSubCity());
                    payload.get().put("physicalAddress", customerAddress.getPhysicalAddress());
                    payload.get().put("latitude", customerAddress.getLatitude());
                    payload.get().put("longitude", customerAddress.getLongitude());
                    payload.get().put("addressId", customerAddress.getId());
                    payload.set(globalMethods.mergeJSONObjects(payload.get(), getCustomerInfo(customerAddress.getCustomerId())));
                });
        return payload.get();
    }


    //Retrieves a paginated list of Customers for report with support for sorting, filtering, searching, and date range.
    public JSONObject getAllCustomers(
            int page,
            int size,
            Sort sort,
            Integer status,
            String city,
            String searchKeyword,
            Timestamp startDate,
            Timestamp endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Customer> customerPage = customerRepository.findByFilterAndDateAndStatus(searchKeyword, startDate, endDate, status, city, pageable);

        List<JSONObject> customers = new ArrayList<>();
        customerPage.getContent().stream()
                .forEach(customer -> {
                    JSONObject detail = new JSONObject();

                    detail.put("firstName", customer.getFirstName());
                    detail.put("middleName", customer.getMiddleName());
                    detail.put("lastName", customer.getLastName());
                    detail.put("status", customer.getStatus());
                    detail.put("location", customer.getLocation());
                    customers.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", customerPage.getNumber())
                .put("pageSize", customerPage.getSize())
                .put("totalElements", customerPage.getTotalElements())
                .put("totalPages", customerPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("customers", customers).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Customer Passed")
                .put("statusMessage", "Customer Passed")
                .put("data", data);

        return response;
    }
}
