package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.models.order.SpecialProductOrder;
import com.commerce.pal.backend.models.order.SpecialProductOrderBid;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.product.categories.ProductCategoryRepository;
import com.commerce.pal.backend.repo.product.categories.ProductSubCategoryRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SpecialProductService {
    private final ProductService productService;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final GlobalMethods globalMethods;
    private final ProductRepository productRepository;

    public SpecialProductService(ProductService productService, ProductSubCategoryRepository productSubCategoryRepository, ProductCategoryRepository productCategoryRepository, GlobalMethods globalMethods, ProductRepository productRepository) {
        this.productService = productService;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.globalMethods = globalMethods;
        this.productRepository = productRepository;
    }

    public JSONObject addSpecialOrderProduct(SpecialProductOrder specialProductOrder, SpecialProductOrderBid specialProductOrderBid) {

        JSONObject request = new JSONObject();
        Long subCategoryId = specialProductOrder.getProductSubCategoryId();
        AtomicReference<Long> categoryId = new AtomicReference<>(0L);
        AtomicReference<Long> parentCategoryId = new AtomicReference<>(0L);

        if (subCategoryId != null)
            productSubCategoryRepository.findById(subCategoryId)
                    .ifPresent(productSubCategory -> {
                        categoryId.set(productSubCategory.getProductCategoryId());
                        productCategoryRepository.findById(categoryId.get())
                                .ifPresent(productCategory -> parentCategoryId.set(productCategory.getParentCategoryId()));
                    });


        BigDecimal unitPrice = specialProductOrderBid.getOfferPrice()
                .divide(new BigDecimal(specialProductOrder.getQuantity()), 2, BigDecimal.ROUND_HALF_UP);

        request.put("merchantId", specialProductOrderBid.getMerchantId().toString());
        request.put("productImage", specialProductOrder.getImageOne());
        request.put("isPromoted", "0");
        request.put("isPrioritized", "0");
        request.put("ownerType", "MERCHANT");
        request.put("quantity", specialProductOrder.getQuantity().toString());
        request.put("unitOfMeasure", "0");
        request.put("unitPrice", unitPrice.toString()); //Todo: fix this
        request.put("currency", "ETB");
        request.put("tax", "0.0");
        request.put("minOrder", "0");
        request.put("maxOrder", specialProductOrder.getQuantity().toString());
        request.put("soldQuantity", "0");
        request.put("productParentCateoryId", parentCategoryId.get().toString());
        request.put("productCategoryId", categoryId.get().toString());
        request.put("productSubCategoryId", subCategoryId != null ? subCategoryId.toString() : "0");
        request.put("productName", specialProductOrder.getProductName());
        request.put("productDescription", specialProductOrder.getProductDescription());
        request.put("specialInstruction", "");
        request.put("shortDescription", specialProductOrder.getProductDescription().substring(0, specialProductOrder.getProductDescription().length() / 4));
        request.put("countryOfOrigin", "ET");
        request.put("manufucturer", "0");
        request.put("productType", "SPECIAL-ORDER");
        request.put("isDiscounted", "0");
        request.put("discountType", "FIXED");
        request.put("discountValue", "0.0");
        request.put("createdBy", "Merchant");

        JSONObject retDet = productService.doAddProduct(request);
        long productId = retDet.getLong("productId");

        if (productId == 0) {
            throw new RuntimeException("Error while adding special product.");
        } else {
            //approve the product TODO: ask amin
            productRepository.findById(productId)
                    .ifPresent(product -> {
                        product.setStatus(1);
                        productRepository.save(product);
                    });
            JSONObject slackBody = new JSONObject();
            slackBody.put("TemplateId", "7");
            slackBody.put("product_name", request.getString("productName"));
            slackBody.put("product_id", String.valueOf(productId));
            globalMethods.sendSlackNotification(slackBody);

            return new JSONObject()
                    .put("productId", productId)
                    .put("subProductId", retDet.getLong("subProductId"))
                    .put("quantity", specialProductOrder.getQuantity())
                    .put("productImage", specialProductOrder.getImageOne())
                    .put("unitPrice", unitPrice)
                    .put("unique_id", globalMethods.generateUniqueString(String.valueOf(productId)))
                    .put("totalPrice", specialProductOrderBid.getOfferPrice());
        }
    }
}
