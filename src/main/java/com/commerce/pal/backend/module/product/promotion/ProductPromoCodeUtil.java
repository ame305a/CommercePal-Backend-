package com.commerce.pal.backend.module.product.promotion;

import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.dto.product.promotion.ApplyPromoCodeDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeDTO;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.models.product.promotion.DiscountType;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCode;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import com.commerce.pal.backend.models.product.promotion.PromoCodeStatus;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.SubProductRepository;
import com.commerce.pal.backend.repo.product.promotion.ProductPromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ProductPromoCodeUtil {
    private final ProductPromoCodeRepository productPromoCodeRepository;
    private final SubProductRepository subProductRepository;
    private final ProductRepository productRepository;
    private final ProductDatabaseService productDatabaseService;


    public void checkPromoCodeExists(String code) {
        if (productPromoCodeRepository.existsByCode(code))
            throw new ResourceAlreadyExistsException("A promo code with the code '" + code + "' already exists. Please provide a unique promo code.");
    }

    public static void validateFixedDiscountType(BigDecimal unitPrice, BigDecimal discountAmount) {
        if (discountAmount.compareTo(unitPrice) > 0)
            throw new IllegalArgumentException("The discount amount cannot exceed the product unit price.");
    }

    public static void validatePercentageDiscountType(BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("The discount percentage must be less than 100%.");
    }


    public SubProduct getSubProduct(Long productId, Long subProductId) {
        return subProductRepository.findSubProductsByProductIdAndSubProductId(productId, subProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with the specified IDs was not found."));
    }


    public static void checkPromoCodeDateValidity(Timestamp startDate, Timestamp endDate) {
        Timestamp currentTimestamp = Timestamp.from(Instant.now());

        if (startDate.after(endDate))
            throw new IllegalArgumentException("Start date must be before the end date.");

        if (currentTimestamp.after(startDate))
            throw new IllegalArgumentException("Start date must not be in the past.");

        if (endDate.before(currentTimestamp))
            throw new IllegalArgumentException("End date must be in the future.");
    }

    public static void checkIfProductIdIsProvided(Long productId, Long subProductId) {
        if (productId == null || subProductId == null)
            throw new IllegalArgumentException("Both product ID and sub-product ID must be provided.");
    }

    public static void checkIfPromoCodeValidity(ProductPromoCode productPromoCode) {
        Timestamp currentTimestamp = Timestamp.from(Instant.now());

        if (currentTimestamp.after(productPromoCode.getEndDate()))
            throw new IllegalArgumentException("The entered promo code has expired.");

        if (productPromoCode.getPromoCodeStatus() != PromoCodeStatus.ACTIVE)
            throw new IllegalArgumentException("The entered promo code is not currently active.");
    }

    public void validateProductExistence(Long productId, Long subProductId) {
        productRepository.findProductByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Product with ID %d not found.", productId)
                ));

        subProductRepository.findSubProductsByProductIdAndSubProductId(productId, subProductId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Sub-product with Product ID %d and Sub-product ID %d not found.",
                                productId, subProductId)
                ));
    }

    private void validateProductOwner(Long productId, Long subProductId) {
        productRepository.findProductByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Product with ID %d not found.", productId)
                ));

        subProductRepository.findSubProductsByProductIdAndSubProductId(productId, subProductId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Sub-product with Product ID %d and Sub-product ID %d not found.",
                                productId, subProductId)
                ));
    }

    public static ProductPromoCode buildProductPromoCode(ProductPromoCodeDTO promoCodeDTO, Long ownerId) {
        ProductPromoCode productPromoCode = new ProductPromoCode();
        productPromoCode.setCode(promoCodeDTO.getCode());
        productPromoCode.setPromoCodeDescription(promoCodeDTO.getPromoCodeDescription());
        productPromoCode.setOwner(promoCodeDTO.getOwner());
        productPromoCode.setDiscountType(promoCodeDTO.getDiscountType());
        productPromoCode.setDiscountAmount(promoCodeDTO.getDiscountAmount());
        productPromoCode.setStartDate(promoCodeDTO.getStartDate());
        productPromoCode.setEndDate(promoCodeDTO.getEndDate());
        productPromoCode.setPromoCodeStatus(PromoCodeStatus.PENDING_WAREHOUSE_APPROVAL);
        productPromoCode.setProductId(ownerId != null ? promoCodeDTO.getProductId() : null);
        productPromoCode.setSubProductId(ownerId != null ? promoCodeDTO.getSubProductId() : null);
        productPromoCode.setMerchantId(ownerId);
        productPromoCode.setCreatedDate(Timestamp.from(Instant.now()));
        productPromoCode.setUpdatedDate(Timestamp.from(Instant.now()));
        productPromoCode.setUpdatedBy(ownerId);
        return productPromoCode;
    }

    public ProductPromoCode getProductPromoCodeById(Long id) {
        return productPromoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A promo code not found."));
    }

    public ProductPromoCode getProductPromoCode(String code) {
        return productPromoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("The specified promo code is does not exist."));
    }

    public void verifyPromoCodeForProduct(ProductPromoCode productPromoCode, List<ApplyPromoCodeDTO.Item> items) {
        // Check if any item matches the product ID and sub-product ID of the promo code
        boolean isApplicable = items.stream()
                .anyMatch(item -> item.getProductId().equals(productPromoCode.getProductId()) &&
                        item.getSubProductId().equals(productPromoCode.getSubProductId()));

        if (!isApplicable) {
            throw new IllegalArgumentException("The specified promo code is not applicable to any product in the cart.");
        }
    }


    public JSONObject calculateMerchantPromoCodePriceSummary(List<ApplyPromoCodeDTO.Item> items, ProductPromoCode productPromoCode) {

        // Verifies if the promo code applies to any of the products in the given DTO
        verifyPromoCodeForProduct(productPromoCode, items);

        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalDiscount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalCharge = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalPromoCodeDiscount = new AtomicReference<>(BigDecimal.ZERO);

        for (ApplyPromoCodeDTO.Item item : items) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                Optional<SubProduct> subProductOpt = subProductRepository.findSubProductsByProductIdAndSubProductId(product.getProductId(), item.getSubProductId());
                if (subProductOpt.isPresent()) {
                    SubProduct subProduct = subProductOpt.get();
                    BigDecimal discountAmount = BigDecimal.ZERO;
                    if (subProduct.getIsDiscounted().equals(1)) {
                        if (subProduct.getDiscountType().equals("FIXED")) {
                            discountAmount = subProduct.getDiscountValue();
                        } else {
                            discountAmount = subProduct.getUnitPrice().multiply(subProduct.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                        }
                    }

                    BigDecimal discountedPrice = subProduct.getUnitPrice().subtract(discountAmount);
                    JSONObject chargeBdy = productDatabaseService.calculateProductPrice(discountedPrice);

                    BigDecimal totalProductPrice;
                    if (item.getProductId().equals(productPromoCode.getProductId()) && item.getSubProductId().equals(productPromoCode.getSubProductId())) {
                        BigDecimal promoCodeDiscountAmount = (productPromoCode.getDiscountType() == DiscountType.PERCENTAGE) ?
                                chargeBdy.getBigDecimal("FinalPrice").multiply(productPromoCode.getDiscountAmount().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)) :
                                productPromoCode.getDiscountAmount();

                        BigDecimal promoCodeDiscountedPrice = chargeBdy.getBigDecimal("FinalPrice").subtract(promoCodeDiscountAmount);

                        totalPromoCodeDiscount.set(totalPromoCodeDiscount.get().add(promoCodeDiscountAmount.multiply(BigDecimal.valueOf(item.getQuantity()))));
                        totalProductPrice = promoCodeDiscountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    } else {
                        totalProductPrice = chargeBdy.getBigDecimal("FinalPrice").multiply(BigDecimal.valueOf(item.getQuantity()));
                    }

                    BigDecimal totalProductCharge = chargeBdy.getBigDecimal("Charge").multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal totalProductDiscount = discountAmount.multiply(BigDecimal.valueOf(item.getQuantity()));

                    totalAmount.set(totalAmount.get().add(totalProductPrice));
                    totalCharge.set(totalCharge.get().add(totalProductCharge));
                    totalDiscount.set(totalDiscount.get().add(totalProductDiscount));
                }
            }
        }

        JSONObject priceSummary = new JSONObject();
        priceSummary.put("totalCheckoutPrice", totalAmount.get().add(totalPromoCodeDiscount.get()).add(totalDiscount.get()));
        priceSummary.put("totalDiscount", totalDiscount.get());
        priceSummary.put("totalPromoCodeDiscount", totalPromoCodeDiscount.get());
        priceSummary.put("totalCharge", totalCharge.get());
        priceSummary.put("finalTotalCheckoutPrice", totalAmount.get());

        JSONObject responseMap = new JSONObject();
        BigDecimal totalOriginalPrice = totalAmount.get().add(totalPromoCodeDiscount.get()).add(totalDiscount.get());
        responseMap.put("currency", "ETB");
        responseMap.put("totalOriginalPrice", totalOriginalPrice);
        responseMap.put("totalProductDiscount", totalDiscount); // Total discount from products
        responseMap.put("totalPromoDiscount", totalPromoCodeDiscount.get()); // Total discount from promo codes
        responseMap.put("totalDiscount", totalDiscount.get().add(totalPromoCodeDiscount.get())); // Sum of product and promo discounts
        responseMap.put("totalVoucherDiscount", 0.00);
        responseMap.put("deliveryFee", 0.00);
        responseMap.put("totalFinalPrice", totalAmount.get()); // Final price after all discounts

        priceSummary.put("responseMap", responseMap);

        return priceSummary;
    }

    public JSONObject calculateWareHousePromoCodePriceSummary(List<ApplyPromoCodeDTO.Item> items, ProductPromoCode productPromoCode) {

        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalDiscount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalCharge = new AtomicReference<>(BigDecimal.ZERO);

        for (ApplyPromoCodeDTO.Item item : items) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                Optional<SubProduct> subProductOpt = subProductRepository.findSubProductsByProductIdAndSubProductId(product.getProductId(), item.getSubProductId());
                if (subProductOpt.isPresent()) {
                    SubProduct subProduct = subProductOpt.get();
                    BigDecimal discountAmount = BigDecimal.ZERO;
                    if (subProduct.getIsDiscounted().equals(1)) {
                        if (subProduct.getDiscountType().equals("FIXED")) {
                            discountAmount = subProduct.getDiscountValue();
                        } else {
                            discountAmount = subProduct.getUnitPrice().multiply(subProduct.getDiscountValue()
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                        }
                    }

                    BigDecimal discountedPrice = subProduct.getUnitPrice().subtract(discountAmount);
                    JSONObject chargeBdy = productDatabaseService.calculateProductPrice(discountedPrice);

                    BigDecimal totalProductPrice = chargeBdy.getBigDecimal("FinalPrice").multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal totalProductCharge = chargeBdy.getBigDecimal("Charge").multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal totalProductDiscount = discountAmount.multiply(BigDecimal.valueOf(item.getQuantity()));

                    totalAmount.set(totalAmount.get().add(totalProductPrice));
                    totalCharge.set(totalCharge.get().add(totalProductCharge));
                    totalDiscount.set(totalDiscount.get().add(totalProductDiscount));
                }
            }
        }

        //calculate totalPromoCodeDiscount
        BigDecimal promoCodeDiscountAmount = calculatePromoCodeDiscountAmount(productPromoCode, totalAmount.get());

        BigDecimal finalTotalCheckoutPrice = totalAmount.get().subtract(promoCodeDiscountAmount);

        JSONObject priceSummary = new JSONObject();
        priceSummary.put("totalCheckoutPrice", totalAmount.get().add(totalDiscount.get()).setScale(2, RoundingMode.CEILING));
        priceSummary.put("totalDiscount", totalDiscount.get().setScale(2, RoundingMode.CEILING));
        priceSummary.put("totalPromoCodeDiscount", promoCodeDiscountAmount.setScale(2, RoundingMode.CEILING));
        priceSummary.put("totalCharge", totalCharge.get().setScale(2, RoundingMode.CEILING));
        priceSummary.put("finalTotalCheckoutPrice", finalTotalCheckoutPrice.setScale(2, RoundingMode.CEILING));

        JSONObject responseMap = new JSONObject();
        BigDecimal totalOriginalPrice = totalAmount.get().add(totalDiscount.get());
        BigDecimal totalFinalPrice = totalAmount.get().subtract(promoCodeDiscountAmount);

        responseMap.put("currency", "ETB");
        responseMap.put("totalOriginalPrice", totalOriginalPrice);
        responseMap.put("totalProductDiscount", totalDiscount); // Total discount from products
        responseMap.put("totalPromoDiscount", promoCodeDiscountAmount); // Total discount from promo codes
        responseMap.put("totalDiscount", totalDiscount.get().add(promoCodeDiscountAmount)); // Sum of product and promo discounts
        responseMap.put("totalVoucherDiscount", 0.00);
        responseMap.put("deliveryFee", 0.00);
        responseMap.put("totalFinalPrice", totalFinalPrice); // Final price after all discounts

        priceSummary.put("responseMap", responseMap);
        return priceSummary;
    }

    public BigDecimal calculatePromoCodeDiscountAmount(ProductPromoCode productPromoCode, BigDecimal amount) {
        BigDecimal discountPercentageInDecimal = productPromoCode.getDiscountAmount()
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return (productPromoCode.getDiscountType() == DiscountType.PERCENTAGE) ?
                amount.multiply(discountPercentageInDecimal) :
                productPromoCode.getDiscountAmount();
    }
}
