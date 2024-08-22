package com.commerce.pal.backend.module.product.promotion;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.dto.product.promotion.ApplyPromoCodeDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeApprovalDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeCancelDTO;
import com.commerce.pal.backend.dto.product.promotion.ProductPromoCodeDTO;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.models.product.promotion.DiscountType;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCode;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import com.commerce.pal.backend.models.product.promotion.PromoCodeStatus;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.SubProductRepository;
import com.commerce.pal.backend.repo.product.promotion.ProductPromoCodeRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@Service
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class ProductPromoCodeService {
    private final ProductPromoCodeRepository productPromoCodeRepository;
    private final ProductPromoCodeUtil productPromoCodeUtil;
    private final GlobalMethods globalMethods;
    private final ProductRepository productRepository;
    private final SubProductRepository subProductRepository;

    public JSONObject addProductPromoCode(ProductPromoCodeDTO promoCodeDTO) {
        ProductPromoCode productPromoCode;
        if (promoCodeDTO.getOwner() == ProductPromoCodeOwner.MERCHANT)
            productPromoCode = addMerchantProductPromoCode(promoCodeDTO);
        else
            productPromoCode = addWareHouseProductPromoCode(promoCodeDTO);

        productPromoCodeRepository.save(productPromoCode);
        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "The product promo code has been successfully added.");
    }


    private ProductPromoCode addMerchantProductPromoCode(ProductPromoCodeDTO promoCodeDTO) {
        LoginValidation user = globalMethods.fetchUserDetails();
        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());

        ProductPromoCodeUtil.checkIfProductIdIsProvided(promoCodeDTO.getProductId(), promoCodeDTO.getSubProductId());
        productPromoCodeUtil.validateProductExistence(promoCodeDTO.getProductId(), promoCodeDTO.getSubProductId());
        SubProduct subProduct = productPromoCodeUtil.getSubProduct(promoCodeDTO.getProductId(), promoCodeDTO.getSubProductId());

        productPromoCodeUtil.checkPromoCodeExists(promoCodeDTO.getCode());
        ProductPromoCodeUtil.checkPromoCodeDateValidity(promoCodeDTO.getStartDate(), promoCodeDTO.getEndDate());

        if (promoCodeDTO.getDiscountType() == DiscountType.FIXED)
            ProductPromoCodeUtil.validateFixedDiscountType(subProduct.getUnitPrice(), promoCodeDTO.getDiscountAmount());
        else
            ProductPromoCodeUtil.validatePercentageDiscountType(promoCodeDTO.getDiscountAmount());

        return ProductPromoCodeUtil.buildProductPromoCode(promoCodeDTO, merchantId);
    }

    private ProductPromoCode addWareHouseProductPromoCode(ProductPromoCodeDTO promoCodeDTO) {
        productPromoCodeUtil.checkPromoCodeExists(promoCodeDTO.getCode());
        ProductPromoCodeUtil.checkPromoCodeDateValidity(promoCodeDTO.getStartDate(), promoCodeDTO.getEndDate());

        if (promoCodeDTO.getDiscountType() == DiscountType.PERCENTAGE)
            ProductPromoCodeUtil.validatePercentageDiscountType(promoCodeDTO.getDiscountAmount());

        return ProductPromoCodeUtil.buildProductPromoCode(promoCodeDTO, null);
    }

    public JSONObject updateProductPromoCode(Long id, ProductPromoCodeDTO promoCodeDTO) {
        ProductPromoCode productPromoCode = productPromoCodeUtil.getProductPromoCodeById(id);

        if (promoCodeDTO.getDiscountAmount() != null)
            productPromoCode.setDiscountAmount(promoCodeDTO.getDiscountAmount());

        if (promoCodeDTO.getEndDate() != null) {
            if (promoCodeDTO.getEndDate().before(Timestamp.from(Instant.now())))
                throw new IllegalArgumentException("End date must be in the future.");

            if (promoCodeDTO.getEndDate().before(productPromoCode.getStartDate()))
                throw new IllegalArgumentException("End date must be after the start date.");

            productPromoCode.setEndDate(promoCodeDTO.getEndDate());
        }

        productPromoCode.setUpdatedDate(Timestamp.from(Instant.now()));
        productPromoCodeRepository.save(productPromoCode);
        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "The product promo code has been successfully updated.");
    }


    public JSONObject getProductPromoCodesByOwner(ProductPromoCodeOwner owner) {
        List<ProductPromoCode> productPromoCodeList;
        if (owner == ProductPromoCodeOwner.MERCHANT) {
            LoginValidation user = globalMethods.fetchUserDetails();
            Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());
            productPromoCodeList = productPromoCodeRepository.findByOwnerAndMerchantIdOrderByUpdatedDateDesc(ProductPromoCodeOwner.MERCHANT, merchantId);
        } else
            productPromoCodeList = productPromoCodeRepository.findByOwnerOrderByUpdatedDateDesc(ProductPromoCodeOwner.WAREHOUSE);

        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("data", productPromoCodeList)
                .put("statusDescription", "Success")
                .put("statusMessage", "Promo codes retrieved successfully");
    }

    public JSONObject getProductPromoCodesByStatus(PromoCodeStatus status) {
        List<ProductPromoCode> productPromoCodeList =
                productPromoCodeRepository.findByPromoCodeStatusOrderByUpdatedDateDesc(status);

        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("data", productPromoCodeList)
                .put("statusDescription", "Success")
                .put("statusMessage", "Promo codes retrieved successfully");
    }


    public JSONObject getProductPromoCodes(PromoCodeStatus status, ProductPromoCodeOwner owner) {
        List<ProductPromoCode> productPromoCodeList =
                productPromoCodeRepository.findAll((Root<ProductPromoCode> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    if(status != null)
                        predicates.add(criteriaBuilder.equal(root.get("promoCodeStatus"), status));

                    if(owner != null)
                        predicates.add(criteriaBuilder.equal(root.get("owner"), owner));

                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                });

        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("data", productPromoCodeList)
                .put("statusDescription", "Success")
                .put("statusMessage", "Promo codes retrieved successfully");
    }

    public JSONObject getProductPromoCodeById(Long id) {
        ProductPromoCode productPromoCode = productPromoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product promo code not found."));

        JSONObject promoCodeJsonObject = new JSONObject();
        promoCodeJsonObject.put("id", productPromoCode.getId());
        promoCodeJsonObject.put("code", productPromoCode.getCode());
        promoCodeJsonObject.put("promoCodeDescription", productPromoCode.getPromoCodeDescription());
        promoCodeJsonObject.put("discountType", productPromoCode.getDiscountType().toString());
        promoCodeJsonObject.put("discountAmount", productPromoCode.getDiscountAmount());
        promoCodeJsonObject.put("startDate", productPromoCode.getStartDate());
        promoCodeJsonObject.put("endDate", productPromoCode.getEndDate());
        promoCodeJsonObject.put("promoCodeStatus", productPromoCode.getPromoCodeStatus().toString());
        promoCodeJsonObject.put("productId", productPromoCode.getProductId());
        promoCodeJsonObject.put("subProductId", productPromoCode.getSubProductId());
        promoCodeJsonObject.put("owner", productPromoCode.getOwner().toString());
        promoCodeJsonObject.put("merchantId", productPromoCode.getMerchantId());
        promoCodeJsonObject.put("createdDate", productPromoCode.getCreatedDate());
        promoCodeJsonObject.put("updatedDate", productPromoCode.getUpdatedDate());
        promoCodeJsonObject.put("updatedBy", productPromoCode.getUpdatedBy());

        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("promoCode", promoCodeJsonObject)
                .put("statusDescription", "Success")
                .put("statusMessage", "Promo codes retrieved successfully");
    }

    public JSONObject cancelProductPromoCode(Long id, ProductPromoCodeCancelDTO codeCancelDTO) {
        ProductPromoCode productPromoCode;
        if (codeCancelDTO.getOwner() == ProductPromoCodeOwner.MERCHANT) {
            // Obtain the merchant ID
            LoginValidation user = globalMethods.fetchUserDetails();
            Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());

            // Find the product promo code associated with the merchant and the specified code
            productPromoCode = productPromoCodeRepository.findByIdAndOwnerAndMerchantId(id, ProductPromoCodeOwner.MERCHANT, merchantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product promo code not found."));
        } else {
            productPromoCode = productPromoCodeRepository.findByIdAndOwner(id, ProductPromoCodeOwner.WAREHOUSE)
                    .orElseThrow(() -> new ResourceNotFoundException("Product promo code not found."));
        }

        if (productPromoCode.getPromoCodeStatus() == PromoCodeStatus.CANCELED)
            throw new ResourceAlreadyExistsException("The promo code is already canceled.");

        // Cancel the product promo code
        productPromoCode.setPromoCodeStatus(PromoCodeStatus.CANCELED);
        productPromoCode.setUpdatedDate(Timestamp.from(Instant.now()));
        productPromoCodeRepository.save(productPromoCode);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS);
        response.put("statusDescription", "Success");
        response.put("statusMessage", "Product promo code canceled successfully.");

        return response;
    }


    public JSONObject approveProductPromoCode(Long id, ProductPromoCodeApprovalDTO approvalDTO) {

        PromoCodeStatus requestedStatus = approvalDTO.getPromoCodeStatus();

        // Validate the promo code status for approval
        if (requestedStatus != PromoCodeStatus.REJECTED_BY_WAREHOUSE && requestedStatus != PromoCodeStatus.ACTIVE) {
            throw new IllegalArgumentException("Only 'REJECTED_BY_WAREHOUSE' or 'ACTIVE' statuses are allowed for approval.");
        }

        ProductPromoCode productPromoCode = productPromoCodeUtil.getProductPromoCodeById(id);

        PromoCodeStatus currentStatus = productPromoCode.getPromoCodeStatus();
        if (currentStatus != PromoCodeStatus.PENDING_WAREHOUSE_APPROVAL) {
            throw new IllegalArgumentException("Only promo codes with status 'PENDING_WAREHOUSE_APPROVAL' can be approved.");
        }

        // Approve the product promo code by updating its status
        productPromoCode.setPromoCodeStatus(requestedStatus);
        productPromoCode.setUpdatedDate(Timestamp.from(Instant.now()));
        productPromoCode.setStartDate(Timestamp.from(Instant.now()));
        productPromoCodeRepository.save(productPromoCode);

        // Prepare a successful response
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS);
        response.put("statusDescription", "Success");
        response.put("statusMessage", "The promo code has been set to '" + requestedStatus + "' successfully.");

        return response;
    }

    public JSONObject applyPromoCode(ApplyPromoCodeDTO applyPromoCodeDTO) {
        // Retrieve product promo code and validate
        ProductPromoCode productPromoCode = productPromoCodeUtil.getProductPromoCode(applyPromoCodeDTO.getPromoCode());
        ProductPromoCodeUtil.checkIfPromoCodeValidity(productPromoCode);

        // Calculate product price
        JSONObject priceSummary;
        if (productPromoCode.getOwner() == ProductPromoCodeOwner.WAREHOUSE)
            priceSummary = productPromoCodeUtil.calculateWareHousePromoCodePriceSummary(applyPromoCodeDTO.getItems(), productPromoCode);
        else
            priceSummary = productPromoCodeUtil.calculateMerchantPromoCodePriceSummary(applyPromoCodeDTO.getItems(), productPromoCode);

        // Prepare response data
        JSONObject response = new JSONObject();
        JSONObject promoCodeSummary = new JSONObject();
        promoCodeSummary.put("promoCodeId", productPromoCode.getId());
        promoCodeSummary.put("owner", productPromoCode.getOwner());
        promoCodeSummary.put("discountType", productPromoCode.getDiscountType());
        promoCodeSummary.put("discountAmount", productPromoCode.getDiscountAmount());

        response.put("statusCode", ResponseCodes.SUCCESS);
        response.put("priceSummary", priceSummary);
        response.put("promoCodeSummary", promoCodeSummary);
        response.put("statusDescription", "Success");
        response.put("statusMessage", "Success");

        return response;
    }

}
