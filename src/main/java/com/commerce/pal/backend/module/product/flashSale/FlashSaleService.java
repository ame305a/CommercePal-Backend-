package com.commerce.pal.backend.module.product.flashSale;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.BadRequestException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.dto.product.flashSale.FlashSaleDTO;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleStatus;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.flashSale.FlashSaleProductRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Log
@Service
@RequiredArgsConstructor
public class FlashSaleService {
    private final FlashSaleUtil flashSaleUtil;
    private final GlobalMethods globalMethods;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final ProductRepository productRepository;

    public JSONObject addFlashSaleProduct(FlashSaleDTO flashSaleDTO) {
        LoginValidation user = globalMethods.fetchUserDetails();
        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());

        Product product = flashSaleUtil.getProduct(flashSaleDTO.getProductId());
        SubProduct subProduct = flashSaleUtil.getSubProduct(flashSaleDTO.getProductId(), flashSaleDTO.getSubProductId());
        FlashSaleUtil.ensureProductBelongsToSpecifiedMerchant(product.getMerchantId(), merchantId);
        FlashSaleUtil.ensureProductIsActive(product);
        FlashSaleUtil.ensureFlashSaleQuantityDoesNotExceedAvailableQuantity(product, flashSaleDTO.getFlashSaleInventoryQuantity());
        FlashSaleUtil.checkFlashSaleDateValidity(flashSaleDTO.getFlashSaleStartDate(), flashSaleDTO.getFlashSaleEndDate());
        FlashSaleUtil.validateFlashSaleCustomerRestrictions(flashSaleDTO);
        FlashSaleUtil.validateFlashSalePrice(subProduct.getUnitPrice(), flashSaleDTO.getFlashSalePrice());

        // Check if active flash sale already exists for the given product and sub-product
        JSONObject existingActiveFlashSale = flashSaleUtil.handleExistingActiveFlashSale(flashSaleDTO.getSubProductId());
        if (existingActiveFlashSale != null)
            return existingActiveFlashSale;

        FlashSaleProduct flashSaleProduct = FlashSaleUtil.buildFlashSaleProduct(flashSaleDTO, subProduct, merchantId);
        flashSaleProduct = flashSaleProductRepository.save(flashSaleProduct);

        JSONObject flashSaleJSONObject = flashSaleUtil.buildFlashSaleJSONObject(flashSaleProduct);
        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Flash sale created successfully.")
                .put("flashSale", flashSaleJSONObject);
    }


    public JSONObject updateFlashSale(Long id, FlashSaleDTO flashSaleDTO) {
        FlashSaleProduct flashSaleProduct = flashSaleUtil.getFlashSaleByIdAndMerchant(id);
        if (flashSaleProduct.getStatus() != FlashSaleStatus.PENDING)
            throw new BadRequestException("Only flash sales with a status of 'Pending' can be updated.");

        if (flashSaleDTO.getFlashSalePrice() != null) {
            SubProduct subProduct = flashSaleProduct.getSubProduct();
            FlashSaleUtil.validateFlashSalePrice(subProduct.getUnitPrice(), flashSaleDTO.getFlashSalePrice());
            flashSaleProduct.setFlashSalePrice(flashSaleDTO.getFlashSalePrice());
        }

        if (flashSaleDTO.getIsQuantityRestrictedPerCustomer() != null && flashSaleDTO.getIsQuantityRestrictedPerCustomer()) {
            if (flashSaleDTO.getFlashSaleMaxQuantityPerCustomer() != null)
                flashSaleProduct.setFlashSaleMaxQuantityPerCustomer(flashSaleDTO.getFlashSaleMaxQuantityPerCustomer());

            if (flashSaleDTO.getFlashSaleMinQuantityPerCustomer() != null)
                flashSaleProduct.setFlashSaleMinQuantityPerCustomer(flashSaleDTO.getFlashSaleMinQuantityPerCustomer());

            flashSaleProduct.setIsQuantityRestrictedPerCustomer(flashSaleDTO.getIsQuantityRestrictedPerCustomer());
        }

        if (flashSaleDTO.getFlashSaleInventoryQuantity() != null) {
            SubProduct subProduct = flashSaleProduct.getSubProduct();
            Product product = flashSaleUtil.getProduct(subProduct.getProductId());

            FlashSaleUtil.ensureFlashSaleQuantityDoesNotExceedAvailableQuantity(product, flashSaleDTO.getFlashSaleInventoryQuantity());
            flashSaleProduct.setFlashSaleInventoryQuantity(flashSaleDTO.getFlashSaleInventoryQuantity());
        }

        if (flashSaleDTO.getFlashSaleEndDate() != null) {
            if (flashSaleDTO.getFlashSaleEndDate().before(Timestamp.from(Instant.now())))
                throw new IllegalArgumentException("End date must be in the future.");

            if (flashSaleDTO.getFlashSaleEndDate().before(flashSaleProduct.getFlashSaleStartDate()))
                throw new IllegalArgumentException("End date must be after the start date.");

            flashSaleProduct.setFlashSaleEndDate(flashSaleDTO.getFlashSaleEndDate());
        }

        flashSaleProduct.setUpdatedDate(Timestamp.from(Instant.now()));
        flashSaleProduct = flashSaleProductRepository.save(flashSaleProduct);
        JSONObject flashSaleJSONObject = flashSaleUtil.buildFlashSaleJSONObject(flashSaleProduct);
        return new JSONObject()
                .put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "The Flash sale has been successfully updated.")
                .put("flashSale", flashSaleJSONObject);
    }

    @Transactional
    public JSONObject activateFlashSale(Long id) {
        FlashSaleProduct flashSaleProduct = flashSaleUtil.getFlashSaleByIdAndMerchant(id);
        if (flashSaleProduct.getStatus() == FlashSaleStatus.ACTIVE)
            throw new ResourceAlreadyExistsException("Flash sale is already active.");

        // Check if active flash sale already exists for the given product and sub-product
        long subProductId = flashSaleProduct.getSubProduct().getSubProductId();
        JSONObject existingActiveFlashSale = flashSaleUtil.handleExistingActiveFlashSale(subProductId);
        if (existingActiveFlashSale != null)
            return existingActiveFlashSale;

        long productId = flashSaleProduct.getSubProduct().getProductId();
        int isProductOnFlashSale = 1; // 1 is yes

        if (flashSaleProduct.getFlashSaleEndDate().before(Timestamp.from(Instant.now())))
            throw new IllegalArgumentException("The flash sale end date must be in the future. Please update the end date.");

        flashSaleProduct.setStatus(FlashSaleStatus.ACTIVE);
        flashSaleProduct.setFlashSaleStartDate(Timestamp.from(Instant.now()));
        flashSaleProduct.setUpdatedDate(Timestamp.from(Instant.now()));
        flashSaleProductRepository.save(flashSaleProduct);

        //update product
        flashSaleUtil.updateProductFlashStatus(productId, isProductOnFlashSale);

        // Prepare a successful response
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS);
        response.put("statusDescription", "Success");
        response.put("statusMessage", "The flash sale has been successfully activated.");

        return response;
    }

    @Transactional
    public JSONObject cancelFlashSale(Long id) {
        FlashSaleProduct flashSaleProduct = flashSaleUtil.getFlashSaleByIdAndMerchant(id);
        long productId = flashSaleProduct.getSubProduct().getProductId();
        int isProductOnFlashSale = 0; // 0 is No

        if (flashSaleProduct.getStatus() == FlashSaleStatus.CANCELED)
            throw new ResourceAlreadyExistsException("Flash sale is already canceled.");

        flashSaleProduct.setStatus(FlashSaleStatus.CANCELED);
        flashSaleProduct.setUpdatedDate(Timestamp.from(Instant.now()));
        flashSaleProductRepository.save(flashSaleProduct);

        //update product
        flashSaleUtil.updateProductFlashStatus(productId, isProductOnFlashSale);

        // Prepare a successful response
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS);
        response.put("statusDescription", "Success");
        response.put("statusMessage", "The flash sale has been successfully canceled.");

        return response;
    }


    //For warehouse management
    public JSONObject getAllFlashSales(int page, int size, String sortDirection, FlashSaleStatus flashSaleStatus) {
        return getFlashSales(page, size, sortDirection, flashSaleStatus, null);
    }

    public JSONObject getMerchantFlashSales(int page, int size, String sortDirection, FlashSaleStatus status) {
        LoginValidation user = globalMethods.fetchUserDetails();
        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());
        return getFlashSales(page, size, sortDirection, status, merchantId);
    }

    public JSONObject getFlashSales(int page, int size, String sortDirection, FlashSaleStatus flashSaleStatus, Long merchantId) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "UpdatedDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        String status = flashSaleStatus != null ? flashSaleStatus.toString() : null;

        Page<FlashSaleProduct> flashSaleProductPage = flashSaleProductRepository.findFlashSales(merchantId, status, pageable);
        return flashSaleUtil.flashSaleResponse(flashSaleProductPage);
    }

}
