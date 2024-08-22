package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.repo.product.ProductRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceV2 {
    private final ProductRepository productRepository;

    public ProductServiceV2(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product updateInventoryAndOrder(JSONObject reqBody) {
        Product product = getProductById(reqBody.getLong("productId"));

        product.setIsDiscounted(reqBody.has("isDiscounted") ? reqBody.getInt("isDiscounted") : product.getIsDiscounted());
        product.setDiscountType(reqBody.has("discountType") ? reqBody.getString("discountType") : product.getDiscountType());
        product.setDiscountValue(reqBody.has("discountValue") ? reqBody.getBigDecimal("discountValue") : product.getDiscountValue());

        product.setUnitOfMeasure(reqBody.has("unitOfMeasure") ? reqBody.getString("unitOfMeasure") : product.getUnitOfMeasure());
        product.setUnitPrice(reqBody.has("unitPrice") ? reqBody.getBigDecimal("unitPrice") : product.getUnitPrice());
        product.setCurrency(reqBody.has("currency") ? reqBody.getString("currency") : product.getCurrency());

        product.setTax(reqBody.has("tax") ? reqBody.getBigDecimal("tax") : product.getTax());
        product.setQuantity(reqBody.has("quantity") ? reqBody.getInt("quantity") : product.getQuantity());
        product.setSoldQuantity(reqBody.has("soldQuantity") ? reqBody.getInt("soldQuantity") : product.getSoldQuantity());
        product.setMinOrder(reqBody.has("minOrder") ? reqBody.getInt("minOrder") : product.getMinOrder());
        product.setMaxOrder(reqBody.has("maxOrder") ? reqBody.getInt("maxOrder") : product.getMaxOrder());
        return productRepository.save(product);
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
    }
}
