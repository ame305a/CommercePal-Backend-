package com.commerce.pal.backend.module.multi;

import com.commerce.pal.backend.repo.user.CustomerRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
@SuppressWarnings("Duplicates")
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public JSONObject getMultiUserCustomer(String email) {
        JSONObject customerData = new JSONObject();
        customerRepository.findCustomerByEmailAddress(email)
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
}
