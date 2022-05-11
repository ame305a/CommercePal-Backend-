package com.commerce.pal.backend.controller.customer;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.integ.notification.EmailClient;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.order.LoanOrder;
import com.commerce.pal.backend.models.order.Order;
import com.commerce.pal.backend.models.order.OrderItem;
import com.commerce.pal.backend.repo.order.LoanOrderRepository;
import com.commerce.pal.backend.repo.order.OrderItemRepository;
import com.commerce.pal.backend.repo.order.OrderRepository;
import com.commerce.pal.backend.repo.order.ShipmentPricingRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.user.CustomerAddressRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static com.commerce.pal.backend.common.ResponseCodes.MERCHANT_TO_CUSTOMER;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/customer/order"})
@SuppressWarnings("Duplicates")
public class CustomerOrderController {

    @Value("${org.commerce.pal.loan.request.email}")
    private String loanRequestEmails;

    @Autowired
    private EmailClient emailClient;

    private final GlobalMethods globalMethods;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final LoanOrderRepository loanOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentPricingRepository shipmentPricingRepository;
    private final CustomerAddressRepository customerAddressRepository;


    public CustomerOrderController(GlobalMethods globalMethods,
                                   OrderRepository orderRepository,
                                   ProductRepository productRepository,
                                   CustomerRepository customerRepository,
                                   LoanOrderRepository loanOrderRepository,
                                   OrderItemRepository orderItemRepository,
                                   ShipmentPricingRepository shipmentPricingRepository,
                                   CustomerAddressRepository customerAddressRepository) {
        this.globalMethods = globalMethods;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.loanOrderRepository = loanOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shipmentPricingRepository = shipmentPricingRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    @RequestMapping(value = "/check-out", method = RequestMethod.POST)
    public ResponseEntity<?> customerCheckOut(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(checkOut);
            JSONArray items = request.getJSONArray("items");
            String transRef = globalMethods.generateTrans();
            LoginValidation user = globalMethods.fetchUserDetails();

            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        AtomicReference<Order> newOrder = new AtomicReference<>(new Order());
                        newOrder.get().setCustomerId(customer.getCustomerId());
                        newOrder.get().setMerchantId(0L);
                        newOrder.get().setAgentId(0l);
                        newOrder.get().setBusinessId(0L);
                        newOrder.get().setOrderRef(transRef);
                        newOrder.get().setPaymentMethod(request.getString("paymentMethod"));
                        newOrder.get().setSaleType(MERCHANT_TO_CUSTOMER);
                        newOrder.get().setOrderDate(Timestamp.from(Instant.now()));
                        newOrder.get().setStatus(0);
                        newOrder.get().setIsAgentInitiated(0);
                        newOrder.get().setStatusDescription("Pending Payment and Shipping");
                        newOrder.get().setIsCustomerAddressAssigned(1);
                        newOrder.get().setPaymentStatus(0);
                        newOrder.get().setCurrency("ETB");
                        newOrder.get().setCountryCode("ET");
                        newOrder.get().setTax(new BigDecimal(0));
                        newOrder.get().setDeliveryPrice(new BigDecimal(0));

                        newOrder.set(orderRepository.save(newOrder.get()));
                        AtomicReference<Double> totalAmount = new AtomicReference<>(0d);
                        AtomicReference<Double> totalDiscount = new AtomicReference<>(0d);
                        AtomicReference<Integer> count = new AtomicReference<>(0);
                        items.forEach(item -> {
                            count.set(count.get() + 1);
                            JSONObject itmValue = new JSONObject(item.toString());
                            productRepository.findById(Long.valueOf(itmValue.getInt("productId")))
                                    .ifPresentOrElse(product -> {
                                        OrderItem orderItem = new OrderItem();
                                        orderItem.setSubOrderNumber(transRef + "-" + count.get().toString());
                                        orderItem.setOrderId(newOrder.get().getOrderId());
                                        orderItem.setProductLinkingId(product.getProductId());
                                        orderItem.setMerchantId(product.getMerchantId());
                                        orderItem.setUnitPrice(product.getUnitPrice());
                                        orderItem.setIsDiscount(product.getIsDiscounted());
                                        orderItem.setQuantity(itmValue.getInt("quantity"));
                                        if (product.getIsDiscounted().equals(1)) {

                                            orderItem.setIsDiscount(1);
                                            orderItem.setDiscountType(product.getDiscountType());
                                            Double discountAmount = 0D;
                                            if (product.getDiscountType().equals("FIXED")) {
                                                orderItem.setDiscountValue(product.getDiscountValue());
                                                orderItem.setDiscountAmount(product.getDiscountValue());
                                            } else {
                                                discountAmount = product.getUnitPrice().doubleValue() * product.getDiscountValue().doubleValue() / 100;
                                                orderItem.setDiscountValue(product.getDiscountValue());
                                                orderItem.setDiscountAmount(new BigDecimal(discountAmount));
                                            }
                                        } else {
                                            orderItem.setIsDiscount(0);
                                            orderItem.setDiscountType("NotDiscounted");
                                            orderItem.setDiscountValue(new BigDecimal(0));
                                            orderItem.setDiscountAmount(new BigDecimal(0));
                                        }
                                        orderItem.setTotalAmount(new BigDecimal(product.getUnitPrice().doubleValue() * Double.valueOf(orderItem.getQuantity())));
                                        orderItem.setTotalDiscount(new BigDecimal(orderItem.getDiscountAmount().doubleValue() * Double.valueOf(orderItem.getQuantity())));
                                        orderItem.setStatus(0);
                                        orderItem.setStatusDescription("Pending Payment and Shipping");
                                        orderItem.setCreatedDate(Timestamp.from(Instant.now()));
                                        orderItem.setDeliveryPrice(new BigDecimal(0));
                                        orderItem.setTaxValue(new BigDecimal(0));
                                        orderItem.setTaxAmount(new BigDecimal(0));
                                        orderItem.setShipmentStatus(0);
                                        orderItem.setAssignedWareHouseId(0);
                                        orderItem.setUserShipmentStatus(0);
                                        orderItemRepository.save(orderItem);
                                        totalAmount.set(totalAmount.get() + orderItem.getTotalAmount().doubleValue());
                                        totalDiscount.set(totalDiscount.get() + orderItem.getTotalDiscount().doubleValue());
                                    }, () -> {
                                        newOrder.get().setStatus(5);
                                        newOrder.get().setStatusDescription("Failure as one of the products is invalid");
                                    });
                        });
                        newOrder.get().setTotalPrice(new BigDecimal(totalAmount.get()));
                        newOrder.get().setDiscount(new BigDecimal(totalDiscount.get()));
                        orderRepository.save(newOrder.get());
                        if (newOrder.get().getStatus().equals(5)) {
                            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                    .put("statusDescription", newOrder.get().getStatusDescription())
                                    .put("statusMessage", newOrder.get().getStatusDescription());
                        } else {
                            JSONObject checkoutSummary = new JSONObject();
                            checkoutSummary.put("TotalCheckoutPrice", newOrder.get().getTotalPrice());
                            checkoutSummary.put("VoucherDiscountAmount", 0.00);
                            checkoutSummary.put("DeliveryFeeAmount", newOrder.get().getDeliveryPrice());
                            checkoutSummary.put("FinalTotalCheckoutPrice", newOrder.get().getTotalPrice());

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Order was successful")
                                    .put("OrderRef", transRef)
                                    .put("checkoutSummary ", checkoutSummary)
                                    .put("statusMessage", "Order was successful");
                        }
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Customer Does not exists")
                                .put("statusMessage", "Customer Does not exists");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/get-pricing", method = RequestMethod.POST)
    public ResponseEntity<?> updatePricing(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(checkOut);
            LoginValidation user = globalMethods.fetchUserDetails();
            productRepository.findById(Long.valueOf(request.getInt("productId")))
                    .ifPresentOrElse(product -> {
                        JSONObject proValue = new JSONObject();
                        proValue.put("UnitPrice", product.getUnitPrice());
                        proValue.put("Quantity", request.getInt("quantity"));
                        proValue.put("IsDiscounted", product.getIsDiscounted());
                        if (product.getIsDiscounted().equals(1)) {
                            proValue.put("DiscountType", product.getDiscountType());

                            Double discountAmount = 0D;
                            if (product.getDiscountType().equals("FIXED")) {
                                proValue.put("DiscountValue", product.getDiscountValue());
                                proValue.put("DiscountAmount", product.getDiscountValue());
                            } else {
                                discountAmount = product.getUnitPrice().doubleValue() * product.getDiscountValue().doubleValue() / 100;
                                proValue.put("DiscountValue", product.getDiscountValue());
                                proValue.put("DiscountAmount", new BigDecimal(discountAmount));
                            }
                        } else {
                            proValue.put("DiscountType", "NotDiscounted");
                            proValue.put("DiscountValue", new BigDecimal(0));
                            proValue.put("DiscountAmount", new BigDecimal(0));
                        }

                        //Find the Delivery Price
                        JSONObject deliveryPrice = getRate(globalMethods.getMerchantCity(product.getMerchantId()), globalMethods.customerRepository(user.getEmailAddress()), product.getShipmentType());
                        proValue = globalMethods.mergeJSONObjects(proValue, deliveryPrice);

                        proValue.put("TotalUnitPrice", new BigDecimal(product.getUnitPrice().doubleValue() * Double.valueOf(request.getInt("quantity"))));
                        proValue.put("TotalDiscount", new BigDecimal(proValue.getBigDecimal("DiscountAmount").doubleValue() * Double.valueOf(request.getInt("quantity"))));
                        proValue.put("FinalPrice", proValue.getBigDecimal("TotalUnitPrice").doubleValue() - proValue.getBigDecimal("TotalDiscount").doubleValue());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("productPricing", proValue)
                                .put("statusDescription", "Product Passed")
                                .put("statusMessage", "Product Passed");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Invalid Product Passed")
                                .put("statusMessage", "Invalid Product Passed");
                    });

        } catch (Exception e) {
            log.log(Level.WARNING, "GET PRICING ERROR : " + e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    @RequestMapping(value = "/loan-request", method = RequestMethod.POST)
    public ResponseEntity<?> postLoanRequest(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject request = new JSONObject(checkOut);
            orderRepository.findOrderByOrderRef(request.getString("orderRef"))
                    .ifPresentOrElse(order -> {
                        loanOrderRepository.findLoanOrderByOrderId(order.getOrderId())
                                .ifPresentOrElse(loanOrder -> {
                                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                            .put("statusDescription", "The Order has already been received")
                                            .put("statusMessage", "The Order has already been received");
                                }, () -> {
                                    LoanOrder loanOrder = new LoanOrder();
                                    loanOrder.setOrderId(order.getOrderId());
                                    loanOrder.setCustomerId(order.getCustomerId());
                                    loanOrder.setAmount(new BigDecimal(order.getTotalPrice().doubleValue() - order.getDiscount().doubleValue()));
                                    AtomicReference<String> customerName = new AtomicReference<>("");
                                    customerRepository.findCustomerByCustomerId(order.getCustomerId())
                                            .ifPresent(customer -> {
                                                loanOrder.setCustomerPhone(customer.getPhoneNumber());
                                                loanOrder.setCustomerEmail(customer.getEmailAddress());
                                                customerName.set(customer.getFirstName() + " " + customer.getLastName());
                                            });
                                    loanOrder.setStatus(1);
                                    loanOrder.setCreatedDate(Timestamp.from(Instant.now()));
                                    loanOrderRepository.save(loanOrder);
                                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                            .put("statusDescription", "Loan Request has been received successfully")
                                            .put("statusMessage", "Loan Request has been received successfully");

                                    // Send Email
                                    String msg = "You Loan request for Order Ref : " + order.getOrderRef() + " of ETB " + loanOrder.getAmount().toString()
                                            + " has been received.Representatives will contact you.";
                                    emailClient.emailSender(msg, loanOrder.getCustomerEmail().trim(), "Loan Order Ref : " + order.getOrderRef());
                                    String[] emails = loanRequestEmails.split(",");

                                    StringBuilder payloadBody = new StringBuilder();
                                    payloadBody.append("New Order Loan request has been made by Customer");
                                    payloadBody.append("<br/>");
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Order Ref : " + order.getOrderRef());
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Customer Name: " + customerName.get());
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Customer Email: " + loanOrder.getCustomerEmail().trim());
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Customer Phone: " + loanOrder.getCustomerPhone().trim());
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Total Amount: ETB " + order.getTotalPrice().toString());
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Total Discount: ETB " + order.getDiscount().toString());
                                    payloadBody.append("<br/>");
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Products Ordered");
                                    Integer number = 1;
                                    orderItemRepository.findOrderItemsByOrderId(order.getOrderId())
                                            .forEach(orderItem -> {
                                                productRepository.findById(orderItem.getProductLinkingId())
                                                        .ifPresent(product -> {
                                                            String productName = "";
                                                            productName = number + ". " + product.getProductName() + " - ETB " + product.getUnitPrice() + " per Unit.(" + orderItem.getQuantity() + ")";
                                                            payloadBody.append("<br/>");
                                                            payloadBody.append(productName);
                                                        });
                                            });

                                    payloadBody.append("<br/>");
                                    payloadBody.append("<br/>");
                                    payloadBody.append("Time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp.from(Instant.now())));


                                    for (String email : emails) {
                                        emailClient.emailSender(payloadBody.toString(), email, "Loan Order Request - Ref (" + order.getOrderRef() + ")");
                                    }
                                });
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Invalid Order Passed")
                                .put("statusMessage", "Invalid Order Passed");
                    });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMap.toString());
    }

    private JSONObject getRate(Integer source, Integer dest, String type) {
        JSONObject rate = new JSONObject();

        shipmentPricingRepository.findShipmentPricingByShipmentTypeAndSourceAndDestination(type, source, dest)
                .ifPresentOrElse(shipmentPricing -> {
                    rate.put("DeliveryType", shipmentPricing.getDeliveryType());
                    rate.put("ServiceMode", shipmentPricing.getServiceMode());
                    rate.put("Rate", shipmentPricing.getRate());
                }, () -> {
                    rate.put("DeliveryType", "Not Defined");
                    rate.put("ServiceMode", "Not Defined");
                    rate.put("Rate", 0);
                });
        return rate;
    }

    @RequestMapping(value = "/assign-delivery-address", method = RequestMethod.POST)
    public ResponseEntity<?> assignDeliveryAddress(@RequestBody String checkOut) {
        JSONObject responseMap = new JSONObject();
        try {
            JSONObject reqBody = new JSONObject(checkOut);
            LoginValidation user = globalMethods.fetchUserDetails();
            customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                    .ifPresentOrElse(customer -> {
                        customerAddressRepository.findCustomerAddressByCustomerIdAndId(customer.getCustomerId(), Long.valueOf(reqBody.getString("AddressId")))
                                .ifPresentOrElse(customerAddress -> {
                                    orderRepository.findOrderByOrderRef(reqBody.getString("orderRef"))
                                            .ifPresentOrElse(order -> {
                                                order.setCustomerAddressId(customerAddress.getId());
                                                order.setIsCustomerAddressAssigned(1);
                                                orderRepository.save(order);
                                                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                                        .put("statusDescription", "success")
                                                        .put("statusMessage", "success");
                                            }, () -> {
                                                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                                        .put("statusDescription", "The Order does not belong to the customer")
                                                        .put("statusMessage", "The Order does not belong to the customer");
                                            });
                                }, () -> {
                                    responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                            .put("statusDescription", "The Address does not belong to the customer")
                                            .put("statusMessage", "The Address does not belong to the customer");
                                });
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
}
