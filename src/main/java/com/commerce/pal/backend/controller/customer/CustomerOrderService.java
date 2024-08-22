package com.commerce.pal.backend.controller.customer;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.order.Order;
import com.commerce.pal.backend.models.order.OrderItem;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCode;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.module.product.promotion.ProductPromoCodeUtil;
import com.commerce.pal.backend.repo.order.*;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.SubProductRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static com.commerce.pal.backend.common.ResponseCodes.MERCHANT_TO_CUSTOMER;

@Log
@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final GlobalMethods globalMethods;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final SubProductRepository subProductRepository;
    private final ProductDatabaseService productDatabaseService;
    private final ProductPromoCodeUtil productPromoCodeUtil;

    @Transactional
    public JSONObject customerCheckOut(String checkOut) {
        log.log(Level.INFO, checkOut);

        JSONObject request = new JSONObject(checkOut);
        JSONArray items = request.getJSONArray("items");
        String promoCode = request.optString("promoCode", null);
        ProductPromoCode productPromoCode = promoCode(promoCode);

        String transRef = globalMethods.generateTrans();
        LoginValidation user = globalMethods.fetchUserDetails();

        Customer customer = customerRepository.findCustomerByEmailAddress(user.getEmailAddress())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        AtomicReference<Order> newOrder = createOrder(customer, transRef, request);
        newOrder.set(orderRepository.save(newOrder.get()));

        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalProductDiscount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalCharge = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalMerchantPromoCodeDiscount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalWareHousePromoCodeDiscount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<Integer> count = new AtomicReference<>(0);

        for (var item : items) {
            count.set(count.get() + 1);
            JSONObject itmValue = new JSONObject(item.toString());

            Long productId = itmValue.getLong("productId");
            Long subProductId = itmValue.getLong("subProductId");

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            SubProduct subProduct = subProductRepository
                    .findSubProductsByProductIdAndSubProductId(productId, subProductId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sub product not found"));

            OrderItem orderItem = createOrderItem(transRef, count, newOrder.get(), product, subProduct, itmValue);

            if (subProduct.getIsDiscounted().equals(1)) {
                orderItem.setIsDiscount(1);
                orderItem.setDiscountType(subProduct.getDiscountType());
                if (subProduct.getDiscountType().equals("FIXED")) {
                    orderItem.setDiscountValue(subProduct.getDiscountValue());
                    orderItem.setDiscountAmount(subProduct.getDiscountValue());
                } else {
                    BigDecimal discountAmount = subProduct.getUnitPrice()
                            .multiply(subProduct.getDiscountValue())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    orderItem.setDiscountValue(subProduct.getDiscountValue());
                    orderItem.setDiscountAmount(discountAmount);
                }
            } else {
                orderItem.setIsDiscount(0);
                orderItem.setDiscountType("NotDiscounted");
                orderItem.setDiscountValue(new BigDecimal(0));
                orderItem.setDiscountAmount(new BigDecimal(0));
            }

            // Get Charges
            BigDecimal discountedPrice = subProduct.getUnitPrice().subtract(orderItem.getDiscountAmount());
            JSONObject chargeBdy = productDatabaseService.calculateProductPrice(discountedPrice);

            BigDecimal productPromoCodeDiscount = BigDecimal.ZERO;
            BigDecimal singleProductPrice = chargeBdy.getBigDecimal("FinalPrice");

            if (isValidMerchantProductPromoCode(productPromoCode, productId, subProductId)) {
                productPromoCodeDiscount = productPromoCodeUtil.calculatePromoCodeDiscountAmount(productPromoCode, singleProductPrice);

                BigDecimal totalProductPromoCodeDiscount = productPromoCodeDiscount.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                totalMerchantPromoCodeDiscount.set(totalMerchantPromoCodeDiscount.get().add(totalProductPromoCodeDiscount));
            }

            singleProductPrice = singleProductPrice.subtract(productPromoCodeDiscount);

            orderItem.setChargeId(chargeBdy.getInt("ChargeId"));
            orderItem.setChargeAmount(chargeBdy.getBigDecimal("Charge"));
            orderItem.setTotalCharge(chargeBdy.getBigDecimal("Charge").multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItem.setTotalAmount(singleProductPrice.multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItem.setTotalDiscount(orderItem.getDiscountAmount().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItemRepository.save(orderItem);

            totalAmount.set(totalAmount.get().add(orderItem.getTotalAmount()));
            totalProductDiscount.set(totalProductDiscount.get().add(orderItem.getTotalDiscount()));
            totalCharge.set(totalCharge.get().add(orderItem.getTotalCharge()));
        }

        //apply warehouse promo code
        if (productPromoCode != null && productPromoCode.getOwner() == ProductPromoCodeOwner.WAREHOUSE) {
            BigDecimal promoCodeDiscountAmount = productPromoCodeUtil.calculatePromoCodeDiscountAmount(productPromoCode, totalAmount.get());

            totalWareHousePromoCodeDiscount.set(promoCodeDiscountAmount);
        }

        BigDecimal discount = totalProductDiscount.get().setScale(2, RoundingMode.CEILING);
        BigDecimal charge = totalCharge.get().setScale(2, RoundingMode.CEILING);
        BigDecimal totalPriceAfterPromoCode = totalAmount.get().subtract(totalWareHousePromoCodeDiscount.get())
                .setScale(2, RoundingMode.CEILING);

        newOrder.get().setTotalPrice(totalPriceAfterPromoCode);
        newOrder.get().setDiscount(discount);
        newOrder.get().setCharge(charge);
        orderRepository.save(newOrder.get());

        BigDecimal totalOriginalPrice = totalAmount.get().add(discount);
        BigDecimal totalPromoCodeDiscount = totalWareHousePromoCodeDiscount.get().add(totalMerchantPromoCodeDiscount.get());
        BigDecimal totalDiscount = discount.add(totalPromoCodeDiscount);

        JSONObject responseMap = new JSONObject();
        JSONObject priceSummary = new JSONObject();
        priceSummary.put("currency", "ETB");
        priceSummary.put("totalOriginalPrice", totalOriginalPrice);
        priceSummary.put("totalProductDiscount", discount); // Total discount from products
        priceSummary.put("totalPromoDiscount", totalPromoCodeDiscount); // Total discount from promo codes
        priceSummary.put("totalDiscount", totalDiscount); // Sum of product and promo discounts
        priceSummary.put("totalVoucherDiscount", 0.00);
        priceSummary.put("deliveryFee", newOrder.get().getDeliveryPrice());
        priceSummary.put("totalFinalPrice", newOrder.get().getTotalPrice()); // Final price after all discounts

        if (productPromoCode != null) {
            JSONObject promoCodeSummary = new JSONObject();
            promoCodeSummary.put("owner", productPromoCode.getOwner());
            promoCodeSummary.put("discountType", productPromoCode.getDiscountType());
            promoCodeSummary.put("discountAmount", productPromoCode.getDiscountAmount());

            responseMap.put("promoCodeSummary", promoCodeSummary);
        }

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("orderRef", transRef)
                .put("priceSummary", priceSummary)
                .put("statusMessage", "Your order has been successfully completed. Please proceed to payment.");

        return responseMap;
    }

    private AtomicReference<Order> createOrder(Customer customer, String transRef, JSONObject request) {
        AtomicReference<Order> newOrder = new AtomicReference<>(new Order());
        newOrder.get().setCustomerId(customer.getCustomerId());
        newOrder.get().setMerchantId(0L);
        newOrder.get().setAgentId(0L);
        newOrder.get().setBusinessId(0L);
        newOrder.get().setOrderRef(transRef);
        newOrder.get().setPaymentMethod(request.getString("paymentMethod"));
        newOrder.get().setSaleType(MERCHANT_TO_CUSTOMER);
        newOrder.get().setOrderDate(Timestamp.from(Instant.now()));
        newOrder.get().setStatus(0);
        newOrder.get().setIsAgentInitiated(0);
        newOrder.get().setStatusDescription("Pending Payment and Shipping");
        newOrder.get().setIsUserAddressAssigned(0);
        newOrder.get().setPaymentStatus(0);
        newOrder.get().setCurrency("ETB");
        newOrder.get().setCountryCode("ET");
        newOrder.get().setTax(new BigDecimal(0));
        newOrder.get().setDeliveryPrice(new BigDecimal(0));
        newOrder.get().setPromotionId(0);
        newOrder.get().setPromotionAmount(new BigDecimal(0));
        newOrder.get().setReferralUserType("ZZ");
        newOrder.get().setReferralUserId(0);
        newOrder.get().setCustomerContacted(0);

        return newOrder;
    }

    private OrderItem createOrderItem(String transRef, AtomicReference<Integer> count, Order newOrder, Product product, SubProduct subProduct, JSONObject itmValue) {
        OrderItem orderItem = new OrderItem();
        orderItem.setSubOrderNumber(transRef + "-" + count.get().toString());
        orderItem.setOrderId(newOrder.getOrderId());
        orderItem.setProductLinkingId(product.getProductId());
        orderItem.setSubProductId(subProduct.getSubProductId());
        orderItem.setMerchantId(product.getMerchantId());
        orderItem.setUnitPrice(subProduct.getUnitPrice());
        orderItem.setIsDiscount(subProduct.getIsDiscounted());
        orderItem.setQuantity(itmValue.getInt("quantity"));
        orderItem.setStatus(0);
        orderItem.setSettlementStatus(0);
        orderItem.setStatusDescription("Pending Payment and Shipping");
        orderItem.setCreatedDate(Timestamp.from(Instant.now()));
        orderItem.setDeliveryPrice(new BigDecimal(0));
        orderItem.setTaxValue(new BigDecimal(0));
        orderItem.setTaxAmount(new BigDecimal(0));
        orderItem.setShipmentStatus(0);
        orderItem.setAssignedWareHouseId(0);
        orderItem.setUserShipmentStatus(0);
        return orderItem;
    }

    public ProductPromoCode promoCode(String promoCode) {
        if (promoCode == null || promoCode.isEmpty())
            return null;

        // Retrieve product promo code and validate
        ProductPromoCode productPromoCode = productPromoCodeUtil.getProductPromoCode(promoCode);
        ProductPromoCodeUtil.checkIfPromoCodeValidity(productPromoCode);
        return productPromoCode;
    }

    private boolean isValidMerchantProductPromoCode(ProductPromoCode productPromoCode, Long productId, Long subProductId) {
        return productPromoCode != null &&
                productPromoCode.getOwner() == ProductPromoCodeOwner.MERCHANT &&
                productId.equals(productPromoCode.getProductId()) &&
                subProductId.equals(productPromoCode.getSubProductId());
    }

}
