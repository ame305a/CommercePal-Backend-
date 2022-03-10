package com.commerce.pal.backend.module.transaction;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.repo.order.OrderRepository;
import com.commerce.pal.backend.repo.order.ShipmentPricingRepository;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
@Component
@SuppressWarnings("Duplicates")
public class OrderProcessingService {
    private final GlobalMethods globalMethods;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShipmentPricingRepository shipmentPricingRepository;

    @Autowired
    public OrderProcessingService(GlobalMethods globalMethods,
                                  OrderRepository orderRepository,
                                  ProductRepository productRepository,
                                  ShipmentPricingRepository shipmentPricingRepository) {
        this.globalMethods = globalMethods;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.shipmentPricingRepository = shipmentPricingRepository;
    }

    public JSONObject getPricing(JSONObject request) {
        JSONObject responseMap = new JSONObject();

        try {
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
                        JSONObject deliveryPrice = getRate(globalMethods.getMerchantCity(product.getMerchantId()), globalMethods.customerRepository(request.getString("userEmail")), product.getShipmentType());
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
        } catch (Exception ex) {
            log.log(Level.WARNING, "GET PRICING ERROR : " + ex.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }

        return responseMap;
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

    public List<JSONObject> getOrderSummary(JSONObject rqBdy) {
        List<JSONObject> details = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            JSONObject detail = new JSONObject();
            detail.put("OrderRef", "CPA8AZV4UX1A");
            detail.put("PaymentMethod", "Murabaha");
            detail.put("SaleType", "M2C");
            detail.put("TotalPrice", "21300.00");
            detail.put("Discount", "295.00");
            detail.put("DeliveryPrice", "60.00");
            detail.put("OrderDate", "2021-09-04 13:49:42.710");
            detail.put("StatusDescription", "Paid");
            detail.put("SahayRef", "BILA8BYF4RQD");
            details.add(detail);
        }
        return details;
    }


}
