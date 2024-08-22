package com.commerce.pal.backend.module.product.flashSale;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.dto.product.flashSale.FlashSaleDTO;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleStatus;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.SubProductRepository;
import com.commerce.pal.backend.repo.product.flashSale.FlashSaleProductRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashSaleUtil {
    private final SubProductRepository subProductRepository;
    private final ProductRepository productRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final GlobalMethods globalMethods;


    public FlashSaleProduct getFlashSaleById(Long id) {
        return flashSaleProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A Flash sale not found."));
    }

    public FlashSaleProduct getFlashSaleByIdAndMerchant(long id) {
        LoginValidation user = globalMethods.fetchUserDetails();
        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());

        return flashSaleProductRepository.findByIdAndMerchantId(id, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("A Flash sale not found."));
    }


    public Product getProduct(Long productId) {
        return productRepository.findProductByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    public SubProduct getSubProduct(Long productId, Long subProductId) {
        return subProductRepository.findSubProductsByProductIdAndSubProductId(productId, subProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-product not found for the specified product."));
    }

    public static void ensureProductBelongsToSpecifiedMerchant(Long ownerId, Long merchantId) {
        if (!ownerId.equals(merchantId))
            throw new IllegalArgumentException("Product does not belong to this merchant.");
    }

    public static void ensureProductIsActive(Product product) {
        if (product.getStatus() != 1)
            throw new IllegalArgumentException("Product must be active to create a flash sale.");
    }

    public static void ensureFlashSaleQuantityDoesNotExceedAvailableQuantity(Product product, Integer flashSaleInventoryQuantity) {
        int availableQuantity = product.getQuantity() - product.getSoldQuantity();

        if (product.getQuantity() == 0 || availableQuantity <= 0)
            throw new IllegalArgumentException("The product is out of stock.");

        if (flashSaleInventoryQuantity > availableQuantity)
            throw new IllegalArgumentException(String.format("The flash sale quantity cannot exceed the available quantity. Available product stock is %d.", availableQuantity));
    }

    public static void checkFlashSaleDateValidity(Timestamp startDate, Timestamp endDate) {
        Timestamp currentTimestamp = Timestamp.from(Instant.now());

        if (startDate.after(endDate))
            throw new IllegalArgumentException("Start date must be before the end date.");

        if (currentTimestamp.after(startDate))
            throw new IllegalArgumentException("Start date must not be in the past.");

        if (endDate.before(currentTimestamp))
            throw new IllegalArgumentException("End date must be in the future.");
    }

    public static void validateFlashSaleCustomerRestrictions(FlashSaleDTO flashSaleDTO) {
        if (flashSaleDTO.getIsQuantityRestrictedPerCustomer() != null && flashSaleDTO.getIsQuantityRestrictedPerCustomer()) {
            if (flashSaleDTO.getFlashSaleMaxQuantityPerCustomer() == null)
                throw new IllegalArgumentException("Maximum quantity per customer must be specified.");

            if (flashSaleDTO.getFlashSaleMinQuantityPerCustomer() == null)
                throw new IllegalArgumentException("Minimum quantity per customer must be specified.");
        }
    }

    public static void validateFlashSalePrice(BigDecimal unitPrice, BigDecimal flashSalePrice) {
        if (flashSalePrice.compareTo(unitPrice) > 0) {
            throw new IllegalArgumentException(String.format("Flash sale price must be lower than the regular unit price. Product unit price is %.2f ETB.", unitPrice));
        }
    }


    public static FlashSaleProduct buildFlashSaleProduct(FlashSaleDTO flashSaleDTO, SubProduct subProduct, Long ownerId) {
        FlashSaleProduct flashSaleProduct = new FlashSaleProduct();

        int minQuantity = flashSaleDTO.getFlashSaleMinQuantityPerCustomer() != null ?
                flashSaleDTO.getFlashSaleMinQuantityPerCustomer() : 1;

        flashSaleProduct.setSubProduct(subProduct);
        flashSaleProduct.setMerchantId(ownerId);
        flashSaleProduct.setFlashSalePrice(flashSaleDTO.getFlashSalePrice());
        flashSaleProduct.setFlashSaleStartDate(flashSaleDTO.getFlashSaleStartDate());
        flashSaleProduct.setFlashSaleEndDate(flashSaleDTO.getFlashSaleEndDate());

        flashSaleProduct.setFlashSaleInventoryQuantity(flashSaleDTO.getFlashSaleInventoryQuantity());
        flashSaleProduct.setFlashSaleTotalQuantitySold(0);
        flashSaleProduct.setIsQuantityRestrictedPerCustomer(flashSaleDTO.getIsQuantityRestrictedPerCustomer());
        flashSaleProduct.setFlashSaleMinQuantityPerCustomer(minQuantity);
        flashSaleProduct.setFlashSaleMaxQuantityPerCustomer(flashSaleDTO.getFlashSaleMaxQuantityPerCustomer());

        flashSaleProduct.setStatus(FlashSaleStatus.PENDING);
        flashSaleProduct.setCreatedDate(Timestamp.from(Instant.now()));
        flashSaleProduct.setUpdatedDate(Timestamp.from(Instant.now()));
        return flashSaleProduct;
    }


    public void updateProductFlashStatus(Long productId, int isProductOnFlashSale) {
        Product product = getProduct(productId);
        product.setIsProductOnFlashSale(isProductOnFlashSale);
        productRepository.save(product);
    }


    public JSONObject buildFlashSaleJSONObject(FlashSaleProduct flashSaleProduct) {
        JSONObject jsonObject = new JSONObject();
        Long productId = flashSaleProduct.getSubProduct().getProductId();
        Product product = getProduct(productId);

        jsonObject.put("id", flashSaleProduct.getId())
                .put("productName", product.getProductName())
                .put("merchantId", flashSaleProduct.getMerchantId())
                .put("flashSalePrice", flashSaleProduct.getFlashSalePrice())
                .put("flashSaleStartDate", flashSaleProduct.getFlashSaleStartDate())
                .put("flashSaleEndDate", flashSaleProduct.getFlashSaleEndDate())
                .put("flashSaleInventoryQuantity", flashSaleProduct.getFlashSaleInventoryQuantity())
                .put("flashSaleTotalQuantitySold", flashSaleProduct.getFlashSaleTotalQuantitySold())
                .put("isQuantityRestrictedPerCustomer", flashSaleProduct.getIsQuantityRestrictedPerCustomer())
                .put("flashSaleMinQuantityPerCustomer", flashSaleProduct.getFlashSaleMinQuantityPerCustomer())
                .put("flashSaleMaxQuantityPerCustomer", flashSaleProduct.getFlashSaleMaxQuantityPerCustomer())
                .put("status", flashSaleProduct.getStatus())
                .put("createdDate", flashSaleProduct.getCreatedDate());

        return jsonObject;
    }


    public JSONObject flashSaleResponse(Page<FlashSaleProduct> flashSaleProductPage) {
        List<JSONObject> flashSaleProductList = new ArrayList<>();
        flashSaleProductPage.forEach(flashSaleProduct -> {

            JSONObject flashSaleJSONObject = buildFlashSaleJSONObject(flashSaleProduct);
            flashSaleProductList.add(flashSaleJSONObject);
        });

        JSONObject response = new JSONObject();
        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", flashSaleProductPage.getNumber())
                .put("pageSize", flashSaleProductPage.getSize())
                .put("totalElements", flashSaleProductPage.getTotalElements())
                .put("totalPages", flashSaleProductPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("flashSales", flashSaleProductList)
                .put("paginationInfo", paginationInfo);

        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Flash sales retrieved successfully")
                .put("data", data);

        return response;
    }


    public JSONObject handleExistingActiveFlashSale(Long subProductId) {
        Optional<FlashSaleProduct> existingActiveFlashSaleProductOpt = flashSaleProductRepository.findBySubProductSubProductIdAndStatus(subProductId, FlashSaleStatus.ACTIVE);
        if (existingActiveFlashSaleProductOpt.isPresent()) {
            FlashSaleProduct existingFlashSaleProduct = existingActiveFlashSaleProductOpt.get();
            JSONObject flashSaleDetails = buildFlashSaleJSONObject(existingFlashSaleProduct);

            return new JSONObject()
                    .put("statusCode", ResponseCodes.RESOURCE_ALREADY_EXIST)
                    .put("statusDescription", "Success")
                    .put("statusMessage", "An active flash sale already exists for this product.")
                    .put("flashSale", flashSaleDetails);
        }
        return null;
    }

}
