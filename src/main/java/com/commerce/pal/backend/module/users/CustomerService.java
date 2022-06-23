package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.repo.user.CustomerAddressRepository;
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
    private final CustomerAddressRepository customerAddressRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           CustomerAddressRepository customerAddressRepository) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
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

    public JSONObject getCustomerAddressById(Long addressId) {
        JSONObject payload = new JSONObject();
        customerAddressRepository.findById(addressId)
                .ifPresent(customerAddress -> {
                    payload.put("id", customerAddress.getId());
                    payload.put("regionId", customerAddress.getRegionId());
                    payload.put("country", customerAddress.getCountry());
                    payload.put("city", customerAddress.getCity());
                    payload.put("subCity", customerAddress.getSubCity());
                    payload.put("physicalAddress", customerAddress.getPhysicalAddress());
                    payload.put("latitude", customerAddress.getLatitude());
                    payload.put("longitude", customerAddress.getLongitude());
                    payload.put("addressId", customerAddress.getId());
                });
        return payload;
    }
}
