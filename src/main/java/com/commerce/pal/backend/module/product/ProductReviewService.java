package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.product.ProductReview;
import com.commerce.pal.backend.repo.product.ProductReviewRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProductReviewService {
    private final ProductReviewRepository productReviewRepository;
    private final GlobalMethods globalMethods;
    private final CustomerRepository customerRepository;

    public ProductReviewService(ProductReviewRepository productReviewRepository, GlobalMethods globalMethods, CustomerRepository customerRepository) {
        this.productReviewRepository = productReviewRepository;
        this.globalMethods = globalMethods;
        this.customerRepository = customerRepository;
    }

    public JSONObject addProductReview(String request) {
        JSONObject responseMap = new JSONObject();
        JSONObject reqBody = new JSONObject(request);
        LoginValidation user = globalMethods.fetchUserDetails();
        Long customerId = globalMethods.getCustomerId(user.getEmailAddress());

//        TODO: check the user is already bought it before. write stored procedure to get it.
        boolean verified = false;

        AtomicReference<ProductReview> productReview = new AtomicReference<>(new ProductReview());

        // Adjust the rating to be within the range [0.5, 5.0]
        // If the input rating is less than 0.5, set it to 0.5
        // If the input rating is more than 5.0, set it to 5.0
        double inputRating = reqBody.getDouble("rating");
        double adjustedRating = Math.max(0.5, Math.min(inputRating, 5.0));

        productReview.get().setCustomerId(customerId);
        productReview.get().setProductId(reqBody.getLong("productId"));
        productReview.get().setRating(adjustedRating);
        productReview.get().setContent(reqBody.getString("content"));
        productReview.get().setImageUrl(reqBody.optString("imageUrl", null));
        productReview.get().setVideoUrl(reqBody.optString("videoUrl", null));
        productReview.get().setVerified(verified);
        productReview.get().setHelpfulCount(0);
        productReview.get().setStatus(1);
        productReview.get().setCreatedAt(Timestamp.from(Instant.now()));
        productReview.set(productReviewRepository.save(productReview.get()));

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("productReviewId", productReview.get().getReviewId())
                .put("statusMessage", "Request processed successful.");

        return responseMap;
    }

    public JSONObject getReviewsByProductId(Long productId) {
        List<JSONObject> productReviews = new ArrayList<>();
        productReviewRepository
                .findByProductIdAndStatusOrderByHelpfulCountDescRatingDescCreatedAtDescVerifiedDesc(productId, 1)
                .forEach(productReview -> {

                    JSONObject payload = new JSONObject();
                    payload.put("reviewId", productReview.getReviewId());
                    payload.put("customerName", globalMethods.getCustomerName(productReview.getCustomerId()));
                    payload.put("productId", productReview.getProductId());
                    payload.put("rating", productReview.getRating());
                    payload.put("content", productReview.getContent());
                    payload.put("imageUrl", productReview.getImageUrl());
                    payload.put("videoUrl", productReview.getVideoUrl());
                    payload.put("verified", productReview.isVerified());
                    payload.put("helpfulCount", productReview.getHelpfulCount());
                    payload.put("status", productReview.getStatus());
                    payload.put("createdAt", productReview.getCreatedAt());
                    productReviews.add(payload);
                });

        return new JSONObject().put("statusCode", ResponseCodes.SUCCESS)
                .put("data", productReviews)
                .put("statusDescription", "Success")
                .put("statusMessage", "Request processed successful.");
    }

    // Increment the helpful count for a given product review
    public JSONObject incrementHelpfulCount(Long reviewId) {
        ProductReview productReview = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview not found with id: " + reviewId));

        productReview.setHelpfulCount(productReview.getHelpfulCount() + 1);
        productReviewRepository.save(productReview);

        return new JSONObject().put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Request processed successful.");
    }


    // Calculate the average rating considering all reviews for a given product
    public OptionalDouble calculateAverageRatingForProduct(Long productId) {
        List<ProductReview> allReviews = productReviewRepository.findByProductId(productId);

        return allReviews.stream()
                .mapToDouble(ProductReview::getRating)
                .average();
    }

    // Calculate the average rating considering only verified reviews for a given product
    public OptionalDouble calculateAverageVerifiedRatingForProduct(Long productId) {
        List<ProductReview> verifiedReviews = productReviewRepository
                .findByProductIdAndVerifiedTrue(productId);

        return verifiedReviews.stream()
                .mapToDouble(ProductReview::getRating)
                .average();
    }

}
