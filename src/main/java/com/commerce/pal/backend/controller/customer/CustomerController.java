package com.commerce.pal.backend.controller.customer;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.email.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.CustomerAddress;
import com.commerce.pal.backend.module.users.CustomerService;
import com.commerce.pal.backend.repo.order.LoanOrderRepository;
import com.commerce.pal.backend.repo.order.OrderItemRepository;
import com.commerce.pal.backend.repo.order.OrderRepository;
import com.commerce.pal.backend.repo.order.ShipmentPricingRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.user.CustomerAddressRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/customer"})
@SuppressWarnings("Duplicates")
public class CustomerController {
    private final GlobalMethods globalMethods;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final LoanOrderRepository loanOrderRepository;
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final ShipmentPricingRepository shipmentPricingRepository;
    private final CustomerService customerService;
    @Value("${org.commerce.pal.loan.request.email}")
    private String loanRequestEmails;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    public CustomerController(GlobalMethods globalMethods,
                              OrderRepository orderRepository,
                              ProductRepository productRepository,
                              LoanOrderRepository loanOrderRepository,
                              CustomerRepository customerRepository,
                              MerchantRepository merchantRepository,
                              OrderItemRepository orderItemRepository,
                              CustomerAddressRepository customerAddressRepository,
                              ShipmentPricingRepository shipmentPricingRepository, CustomerService customerService) {
        this.globalMethods = globalMethods;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.loanOrderRepository = loanOrderRepository;
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.shipmentPricingRepository = shipmentPricingRepository;
        this.customerService = customerService;
    }

    @RequestMapping(value = "/update-user-info", method = RequestMethod.POST)
    public ResponseEntity<?> updateUserInfo(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(checkOut);
            LoginValidation user = globalMethods.fetchUserDetails();
            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        customer.setFirstName(reqBody.has("firstName") ? reqBody.getString("firstName") : customer.getFirstName());
                        customer.setLastName(reqBody.has("lastName") ? reqBody.getString("lastName") : customer.getLastName());
                        customer.setLanguage(reqBody.has("language") ? reqBody.getString("language") : customer.getLanguage());
                        customer.setCity(reqBody.has("city") ? reqBody.getString("city") : customer.getCity());
                        customer.setCountry(reqBody.has("country") ? reqBody.getString("country") : customer.getCountry());
                        customer.setDistrict(reqBody.has("district") ? reqBody.getString("district") : customer.getDistrict());
                        customer.setLocation(reqBody.has("location") ? reqBody.getString("location") : customer.getLocation());
                        customerRepository.save(customer);

                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER UPDATE INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/add-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> addDeliveryAddress(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(checkOut);
            LoginValidation user = globalMethods.fetchUserDetails();

            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        AtomicReference<CustomerAddress> customerAddress = new AtomicReference<>(new CustomerAddress());
                        customerAddress.get().setCustomerId(customer.getCustomerId());
                        customerAddress.get().setRegionId(reqBody.getInt("regionId"));
                        customerAddress.get().setCountry(reqBody.getString("country"));
                        customerAddress.get().setCity(reqBody.getInt("city"));
                        customerAddress.get().setSubCity(reqBody.getString("subCity"));
                        customerAddress.get().setPhoneNumber(reqBody.getString("phoneNumber"));
                        customerAddress.get().setPhysicalAddress(reqBody.getString("physicalAddress"));
                        customerAddress.get().setLatitude(reqBody.getString("latitude"));
                        customerAddress.get().setLongitude(reqBody.getString("longitude"));
                        customerAddress.get().setStatus(1);
                        customerAddress.get().setCreatedDate(Timestamp.from(Instant.now()));
                        customerAddress.set(customerAddressRepository.save(customerAddress.get()));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("AddressId", customerAddress.get().getId())
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/update-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> updateDeliveryAddress(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();

            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        customerAddressRepository.findCustomerAddressByCustomerIdAndId(
                                        customer.getCustomerId(), reqBody.getLong("id"))
                                .ifPresentOrElse(customerAddress -> {
                                    customerAddress.setRegionId(reqBody.has("productName") ? reqBody.getInt("regionId") : customerAddress.getRegionId());
                                    customerAddress.setCountry(reqBody.has("country") ? reqBody.getString("country") : customerAddress.getCountry());
                                    customerAddress.setCity(reqBody.has("city") ? reqBody.getInt("city") : customerAddress.getCity());
                                    customerAddress.setSubCity(reqBody.has("subCity") ? reqBody.getString("subCity") : customerAddress.getSubCity());
                                    customerAddress.setPhoneNumber(reqBody.has("phoneNumber") ? reqBody.getString("phoneNumber") : customerAddress.getPhoneNumber());
                                    customerAddress.setPhysicalAddress(reqBody.has("physicalAddress") ? reqBody.getString("physicalAddress") : customerAddress.getPhysicalAddress());
                                    customerAddress.setLatitude(reqBody.has("latitude") ? reqBody.getString("latitude") : customerAddress.getLatitude());
                                    customerAddress.setLongitude(reqBody.has("longitude") ? reqBody.getString("longitude") : customerAddress.getLongitude());
                                    customerAddressRepository.save(customerAddress);
                                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                            .put("statusDescription", "success")
                                            .put("statusMessage", "success");
                                }, () -> {
                                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                            .put("statusDescription", "failed to process request")
                                            .put("statusMessage", "internal system error");
                                });
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/delete-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> deleteDeliveryAddress(@RequestBody String request) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(request);
            LoginValidation user = globalMethods.fetchUserDetails();

            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        customerAddressRepository.findCustomerAddressByCustomerIdAndId(
                                        customer.getCustomerId(), reqBody.getLong("id"))
                                .ifPresentOrElse(customerAddress -> {
                                    customerAddress.setStatus(0);
                                    customerAddressRepository.save(customerAddress);
                                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                            .put("statusDescription", "success")
                                            .put("statusMessage", "success");
                                }, () -> {
                                    responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                            .put("statusDescription", "failed to process request")
                                            .put("statusMessage", "internal system error");
                                });
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "success");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-delivery-address", method = RequestMethod.GET)
    public ResponseEntity<?> getDeliveryAddress() {
        JSONObject responseMap = new JSONObject();
        try {
            LoginValidation user = globalMethods.fetchUserDetails();
            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        List<JSONObject> addresses = new ArrayList<>();
                        customerAddressRepository.findCustomerAddressByCustomerIdAndStatus(
                                        customer.getCustomerId(), 1)
                                .forEach(customerAddress -> {
                                    JSONObject payload = new JSONObject();
                                    payload.put("id", customerAddress.getId());
                                    payload.put("regionId", customerAddress.getRegionId());
                                    payload.put("country", customerAddress.getCountry());
                                    payload.put("cityId", customerAddress.getCity());
                                    payload.put("city", globalMethods.cityName(Long.valueOf(customerAddress.getCity())));
                                    payload.put("subCity", customerAddress.getSubCity());
                                    payload.put("physicalAddress", customerAddress.getPhysicalAddress());
                                    payload.put("latitude", customerAddress.getLatitude());
                                    payload.put("longitude", customerAddress.getLongitude());
                                    payload.put("addressId", customerAddress.getId());
                                    addresses.add(payload);
                                });
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("data", addresses)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "failed to process request")
                                .put("statusMessage", "internal system error");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER DELIVERY ADDRESS INFO : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }


    @RequestMapping(value = {"/report"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Timestamp requestStartDate,
            @RequestParam(required = false) Timestamp requestEndDate
    ) {
        try {
            // If requestEndDate is not provided, set it to the current timestamp
            if (requestEndDate == null)
                requestEndDate = Timestamp.from(Instant.now());

            // Default to sorting by customer 1st name in ascending order if sortBy is not provided
            if (sortBy == null || sortBy.isEmpty())
                sortBy = "FirstName";

            // Default to ascending order if sortDirection is not provided or is invalid
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
                direction = Sort.Direction.DESC;

            Sort sort = Sort.by(direction, sortBy);

            JSONObject response = customerService.getAllCustomers(page, size, sort, status, searchKeyword, requestStartDate, requestEndDate);
            return ResponseEntity.status(HttpStatus.OK).body(response.toString());

        } catch (Exception e) {
            log.log(Level.WARNING, "CUSTOMER REPORT: " + e.getMessage());
            JSONObject responseMap = new JSONObject();
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
        }
    }

}
