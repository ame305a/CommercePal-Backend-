package com.commerce.pal.backend.controller.merchant;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.module.product.ProductServiceV2;
import com.commerce.pal.backend.module.product.SubProductService;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/merchant/products"})
@SuppressWarnings("Duplicates")
public class MerchantProductControllerV2 {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final ProductServiceV2 productServiceV2;
    private final MerchantRepository merchantRepository;
    private final SubProductService subProductService;

    public MerchantProductControllerV2(GlobalMethods globalMethods, ProductService productService, ProductServiceV2 productServiceV2, MerchantRepository merchantRepository, SubProductService subProductService) {
        this.globalMethods = globalMethods;
        this.productService = productService;
        this.productServiceV2 = productServiceV2;
        this.merchantRepository = merchantRepository;
        this.subProductService = subProductService;
    }

    @PostMapping(value = "/main-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProduct(@RequestBody String requestPayload) {
        log.log(Level.INFO, "Received request: " + requestPayload);
        JSONObject responseMap = new JSONObject();

        JSONObject request = new JSONObject(requestPayload);
        LoginValidation user = globalMethods.fetchUserDetails();

        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    // Set default values for some parameters
                    setDefaultProductValues(request, merchant.getMerchantId());

                    if (subProductService.validateFeature(request.getLong("productSubCategoryId"), request.getJSONArray("productFeature")).equals(1)) {
                        JSONObject retDet = productService.doAddProduct(request);
                        int returnValue = retDet.getInt("productId");
                        if (returnValue == 0) {
                            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                    .put("statusDescription", "Failed to process request")
                                    .put("statusMessage", "Internal system error occurred");

                        } else {
                            JSONObject slackBody = new JSONObject();
                            slackBody.put("TemplateId", "7");
                            slackBody.put("product_name", request.getString("productName"));
                            slackBody.put("product_id", String.valueOf(retDet.getInt("productId")));
                            globalMethods.sendSlackNotification(slackBody);

                            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                    .put("statusDescription", "Success")
                                    .put("productId", retDet.getInt("productId"))
                                    .put("subProductId", retDet.getInt("subProductId"))
                                    .put("statusMessage", "The product has been successfully added.");

                            subProductService.updateInsertFeatures(retDet.getLong("subProductId"), request.getJSONArray("productFeature"));
                        }
                    } else {
                        responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                                .put("statusDescription", "Product features not defined well")
                                .put("statusMessage", "Product features are not defined correctly");
                    }
                }, () -> responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "Merchant Does not exists")
                        .put("statusMessage", "Merchant Does not exists"));
        return ResponseEntity.ok(responseMap.toString());
    }

    @PostMapping(value = "/inventory-order-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateInventoryAndOrder(@RequestBody String requestPayload) {
        log.info("Received inventory-order product request: " + requestPayload);
        JSONObject responseMap = new JSONObject();

        JSONObject request = new JSONObject(requestPayload);
        LoginValidation user = globalMethods.fetchUserDetails();

        Long merchantId = globalMethods.getMerchantId(user.getEmailAddress());
        Long productId = request.getLong("productId");

        if (merchantId != null && merchantId != 0L) {
            if (globalMethods.validateMerchantProduct(merchantId, productId)) {
                Product product = productServiceV2.updateInventoryAndOrder(request);

                JSONObject slackBody = new JSONObject();
                slackBody.put("TemplateId", "8");
                slackBody.put("product_name", product.getProductName());
                slackBody.put("product_id", productId);
                globalMethods.sendSlackNotification(slackBody);

                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "Success")
                        .put("statusMessage", "Product inventory and order details updated successfully.");
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", "Unauthorized Product Access")
                        .put("statusMessage", "This product does not belong to your merchant account.");
            }
        } else {
            responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                    .put("statusDescription", "Invalid User Type")
                    .put("statusMessage", "This operation is exclusive to merchant accounts. Please log in with a valid merchant account.");

        }
        return ResponseEntity.ok(responseMap.toString());
    }

    private void setDefaultProductValues(JSONObject request, Long merchantId) {
        request.put("merchantId", String.valueOf(merchantId));
        request.put("productImage", "defaultImage.png");
        request.put("isPromoted", "0");
        request.put("isPrioritized", "0");
        request.put("ownerType", "MERCHANT");
        request.put("quantity", "0");
        request.put("unitOfMeasure", "0");
        request.put("unitPrice", "0.0");
        request.put("currency", "ETB");
        request.put("tax", "0.0");
        request.put("minOrder", "0");
        request.put("maxOrder", "10");
        request.put("soldQuantity", "0");
        request.put("isDiscounted", "0");
        request.put("discountType", "FIXED");
        request.put("discountValue", "0.0");
        request.put("createdBy", "Merchant");
    }

}
