package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleStatus;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.flashSale.FlashSaleProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Log
@Service
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class DiscountedProductService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final FlashSaleProductRepository flashSaleProductRepository;

    public JSONObject getProductsOnFlashSale(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "UnitPrice");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findProductsOnFlashSale("RETAIL", pageable);
        return productResponse(productPage);
    }


    public JSONObject getTopDealProducts(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "UnitPrice");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findTopDealProducts("RETAIL", pageable);
        List<JSONObject> details = new ArrayList<>();
        productPage.forEach(pro -> {
            JSONObject detail = productService.getProductListDetailsAlready(pro);
            if (!detail.get("DiscountType").equals("NotDiscounted"))
                details.add(detail);
        });

        JSONObject response = new JSONObject();
        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", productPage.getNumber())
                .put("pageSize", productPage.getSize())
                .put("totalElements", productPage.getTotalElements())
                .put("totalPages", productPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("products", details)
                .put("paginationInfo", paginationInfo);

        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed")
                .put("data", data);

        return response;
    }

    private JSONObject productResponse(Page<Product> productPage) {
        List<JSONObject> details = new ArrayList<>();
        productPage.forEach(pro -> {
            JSONObject detail = productService.getProductListDetailsAlready(pro);
            if (pro.getIsProductOnFlashSale() == 1) {
                Optional<FlashSaleProduct> optionalFlashSaleProduct = flashSaleProductRepository
                        .findBySubProductProductIdAndStatus(pro.getProductId(), FlashSaleStatus.ACTIVE);
                if (optionalFlashSaleProduct.isPresent()) {
                    FlashSaleProduct flashSaleProduct = optionalFlashSaleProduct.get();
                    JSONObject flashSale = new JSONObject();
                    flashSale.put("flashSalePrice", flashSaleProduct.getFlashSalePrice())
                            .put("subProductId", flashSaleProduct.getSubProduct().getSubProductId())
                            .put("flashSaleStartDate", flashSaleProduct.getFlashSaleStartDate())
                            .put("flashSaleEndDate", flashSaleProduct.getFlashSaleEndDate())
                            .put("flashSaleInventoryQuantity", flashSaleProduct.getFlashSaleInventoryQuantity())
                            .put("flashSaleTotalQuantitySold", flashSaleProduct.getFlashSaleTotalQuantitySold())
                            .put("isQuantityRestrictedPerCustomer", flashSaleProduct.getIsQuantityRestrictedPerCustomer())
                            .put("flashSaleMinQuantityPerCustomer", flashSaleProduct.getFlashSaleMinQuantityPerCustomer())
                            .put("flashSaleMaxQuantityPerCustomer", flashSaleProduct.getFlashSaleMaxQuantityPerCustomer());

                    detail.put("flashSale", flashSale);
                }
            }

            details.add(detail);
        });

        JSONObject response = new JSONObject();

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", productPage.getNumber())
                .put("pageSize", productPage.getSize())
                .put("totalElements", productPage.getTotalElements())
                .put("totalPages", productPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("products", details)
                .put("paginationInfo", paginationInfo);

        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed")
                .put("data", data);

        return response;
    }
}
