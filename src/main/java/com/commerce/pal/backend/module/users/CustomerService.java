package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.repo.user.CustomerAddressRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        customerAddressRepository.findById(addressId)
                .ifPresent(customerAddress -> {
                    payload.get().put("id", customerAddress.getId());
                    payload.get().put("regionId", customerAddress.getRegionId());
                    payload.get().put("country", customerAddress.getCountry());
                    payload.get().put("city", customerAddress.getCity());
                    payload.get().put("subCity", customerAddress.getSubCity());
                    payload.get().put("physicalAddress", customerAddress.getPhysicalAddress());
                    payload.get().put("latitude", customerAddress.getLatitude());
                    payload.get().put("longitude", customerAddress.getLongitude());
                    payload.get().put("addressId", customerAddress.getId());
                    payload.set(globalMethods.mergeJSONObjects(payload.get(), getCustomerInfo(customerAddress.getCustomerId())));
                });
        return payload.get();
    }
}
