package com.commerce.pal.backend.module.pos;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ForbiddenException;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.module.database.ProductDatabaseService;
import com.commerce.pal.backend.module.product.CategoryService;
import com.commerce.pal.backend.module.product.ProductCategoryService;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.repo.product.*;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@Service
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class ProductPOSService {

    private final GlobalMethods globalMethods;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public List<JSONObject> getMerchantProducts() {
        long merchantId = getMerchantId();
        List<Product> products = productRepository
                .findByOwnerTypeAndMerchantId("MERCHANT", merchantId);

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = productService.getProductDetail(pro.getProductId());
            details.add(detail);
        });

        return details;
    }

    public JSONObject getMerchantProductOverview() {
        Long merchantId = getMerchantId();
//        Long merchantId = 672L;
        List<Product> products = productRepository
                .findByOwnerTypeAndMerchantId("MERCHANT", merchantId);

        int soldProductCount = 0;
        int notDiscountedProductCount = 0;
        int discountedProductCount = 0;

        for (Product product : products) {
            if (product.getIsDiscounted() == 1)
                discountedProductCount++;
            else
                notDiscountedProductCount++;

            if (product.getSoldQuantity() != 0) {
                soldProductCount++;
            }
        }

        Map<String, Object> productOverview = new HashMap<>();
        productOverview.put("soldProductCount", soldProductCount);
        productOverview.put("discountedProductCount", discountedProductCount);
        productOverview.put("notDiscountedProductCount", notDiscountedProductCount);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("statusCode", ResponseCodes.SUCCESS);
        responseData.put("statusDescription", "Success");
        responseData.put("statusMessage", "Success");
        responseData.put("data", productOverview);

        return new JSONObject(responseData);
    }


    public List<JSONObject> getMerchantSoldProducts() {
        long merchantId = getMerchantId();

        List<Product> products = productRepository
                .findByOwnerTypeAndMerchantIdAndSoldQuantityGreaterThan("MERCHANT", merchantId, 0);

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = productService.getProductListDetailsAlready(pro);
            details.add(detail);
        });

        return details;
    }


    public List<JSONObject> getMerchantDiscountedProducts() {
        long merchantId = getMerchantId();

        List<Product> products = productRepository
                .findByOwnerTypeAndMerchantIdAndIsDiscountedAndDiscountValueGreaterThan("MERCHANT", merchantId, 1, BigDecimal.ZERO);

        List<JSONObject> details = new ArrayList<>();
        products.forEach(pro -> {
            JSONObject detail = productService.getProductListDetailsAlready(pro);
            if (!detail.get("DiscountType").equals("NotDiscounted"))
                details.add(detail);
        });

        return details;
    }

    private Long getMerchantId() {
        LoginValidation user = globalMethods.fetchUserDetails();
        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());

        if (merchantId == null || merchantId == 0L)
            throw new ForbiddenException("This operation is exclusive to merchant accounts. " +
                    "Please log in with a valid merchant account.");

        return merchantId;
//        return 672L;
    }

}
