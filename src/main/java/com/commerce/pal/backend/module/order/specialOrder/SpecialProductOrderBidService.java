package com.commerce.pal.backend.module.order.specialOrder;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.order.SpecialProductOrder;
import com.commerce.pal.backend.models.order.SpecialProductOrderBid;
import com.commerce.pal.backend.module.product.SpecialProductService;
import com.commerce.pal.backend.module.users.MerchantService;
import com.commerce.pal.backend.repo.order.SpecialProductOrderBidRepository;
import com.commerce.pal.backend.repo.order.SpecialProductOrderRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log
@Service
@RequiredArgsConstructor
public class SpecialProductOrderBidService {
    private final GlobalMethods globalMethods;
    private final SpecialProductOrderBidRepository specialProductOrderBidRepository;
    private final MerchantRepository merchantRepository;
    private final SpecialProductOrderRepository specialProductOrderRepository;
    private final SpecialProductOrderService specialProductOrderService;
    private final SpecialProductService specialProductService;
    private final MerchantService merchantService;

    public JSONObject getSpecialOrderBids(Long specialProductOrderId) {
        globalMethods.fetchUserDetails(); // Force authentication
        JSONObject responseMap = new JSONObject();
        List<SpecialProductOrderBid> specialProductOrderBids = specialProductOrderBidRepository
                .findBySpecialOrderIdAndIsMerchantAccepted(specialProductOrderId, 1);

        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", specialProductOrderBids)
                .put("statusDescription", "Success")
                .put("statusMessage", "Request processed successfully");

        return responseMap;
    }

    //TODO: add detail info like images from special order => combine from 2 tables
    public JSONObject getBidsAssignedToMerchant() {
        JSONObject responseMap = new JSONObject();
        LoginValidation user = globalMethods.fetchUserDetails();

        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    Long merchantId = merchant.getMerchantId();

                    List<JSONObject> jsonObjectList = new ArrayList<>();
                    specialProductOrderBidRepository.findByMerchantIdOrderByAssignedDateDesc(merchantId)
                            .forEach(specialProductOrderBid -> {
                                SpecialProductOrder specialProductOrder = specialProductOrderRepository.findById(specialProductOrderBid.getSpecialOrderId()).get();

                                JSONObject order = new JSONObject();
                                order.put("bidId", specialProductOrderBid.getBidId());
                                order.put("assignedDate", specialProductOrderBid.getAssignedDate());
                                order.put("specialOrderId", specialProductOrder.getId());
                                order.put("productName", specialProductOrder.getProductName());
                                order.put("estimatePrice", specialProductOrder.getEstimatePrice());
                                order.put("quantity", specialProductOrder.getQuantity());
                                order.put("productDescription", specialProductOrder.getProductDescription());
                                order.put("status", specialProductOrder.getStatus());
                                order.put("linkToProduct", specialProductOrder.getLinkToProduct() != null ? specialProductOrder.getLinkToProduct() : "");
                                order.put("imageOne", specialProductOrder.getImageOne() != null ? specialProductOrder.getImageOne() : "");
                                order.put("imageTwo", specialProductOrder.getImageTwo() != null ? specialProductOrder.getImageTwo() : "");
                                order.put("imageThree", specialProductOrder.getImageThree() != null ? specialProductOrder.getImageThree() : "");
                                order.put("imageFour", specialProductOrder.getImageFour() != null ? specialProductOrder.getImageFour() : "");
                                order.put("imageFive", specialProductOrder.getImageFive() != null ? specialProductOrder.getImageFive() : "");
                                order.put("subCategoryName", specialProductOrderService.subCategoryName(specialProductOrder));
                                order.put("isBidProvided", specialProductOrderBid.getIsMerchantAccepted() != null ? 1 : 0);

                                jsonObjectList.add(order);
                            });

                    responseMap.put("statusCode", ResponseCodes.SUCCESS)
                            .put("data", jsonObjectList)
                            .put("statusDescription", "Success")
                            .put("statusMessage", "Request processed successfully");

                }, () -> responseMap.put("statusCode", ResponseCodes.RECORD_NOT_FOUND)
                        .put("statusDescription", "Failed to process request")
                        .put("statusMessage", "Customer not found."));

        return responseMap;
    }

    public JSONObject processMerchantResponseToBidAssignment(String requestBody) {
        JSONObject responseMap = new JSONObject();
        JSONObject request = new JSONObject(requestBody);
        LoginValidation user = globalMethods.fetchUserDetails();

        merchantRepository.findMerchantByEmailAddress(user.getEmailAddress())
                .ifPresentOrElse(merchant -> {
                    Long merchantId = merchant.getMerchantId();

                    specialProductOrderBidRepository.findByBidIdAndMerchantId(request.getLong("bidId"), merchantId)
                            .ifPresentOrElse(specialProductOrderBid -> {
                                int isMerchantAccepted = request.getInt("isMerchantAccepted");

                                if (isMerchantAccepted == 1) {
                                    specialProductOrderBid.setOfferStatus(1);
                                    specialProductOrderBid.setOfferDetails(request.optString("offerDetails", null));
                                    specialProductOrderBid.setOfferPrice(BigDecimal.valueOf(request.getLong("offerPrice")));
                                    specialProductOrderBid.setOfferExpirationDate(GlobalMethods.parseTimestampFromDateString(request.getString("offerExpireDate")));
                                    specialProductOrderBid.setMerchantResponseDate(Timestamp.from(Instant.now()));
                                } else {
                                    specialProductOrderBid.setOfferStatus(5); // 5 -> Rejected by merchant
                                    specialProductOrderBid.setMerchantResponseDate(Timestamp.from(Instant.now()));
                                }

                                specialProductOrderBid.setIsMerchantAccepted(isMerchantAccepted);
                                specialProductOrderBid.setSelectionStatus(0);
                                specialProductOrderBid.setMerchantAdditionalNotes(request.optString("additionalNotes", null));
                                specialProductOrderBidRepository.save(specialProductOrderBid);

                                // send sms, email and push notifications to customer
                                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                        .put("statusDescription", "Success")
                                        .put("statusMessage", "Merchant responded successfully");

                            }, () -> responseMap.put("statusCode", ResponseCodes.RECORD_NOT_FOUND)
                                    .put("statusDescription", "Failed to process request")
                                    .put("statusMessage", "Special order bid not found for the provided details"));

                }, () -> responseMap.put("statusCode", ResponseCodes.RECORD_NOT_FOUND)
                        .put("statusDescription", "Failed to process request")
                        .put("statusMessage", "Merchant not found."));

        return responseMap;
    }

    @Transactional
    public JSONObject processCustomerResponseToMerchantBid(String requestBody) {
        JSONObject responseMap = new JSONObject();
        globalMethods.fetchUserDetails(); //to force authentication

        JSONObject request = new JSONObject(requestBody);
        Long specialOrderId = request.getLong("specialOrderId");
        Long bidId = request.getLong("bidId");

        SpecialProductOrder specialOrder = specialProductOrderService.getById(specialOrderId);

        if (specialOrder.getStatus() == 3)
            throw new ResourceAlreadyExistsException("The merchant bid for this special order has already been selected and it not updatable.");

        //update status
        specialOrder.setStatus(3);
        specialProductOrderRepository.save(specialOrder);

        specialProductOrderBidRepository
                .findByBidIdAndSpecialOrderIdAndIsMerchantAccepted(bidId, specialOrderId, 1)
                .ifPresentOrElse(specialOrderBid -> {
                    specialOrderBid.setSelectionStatus(1);
                    specialOrderBid.setSelectionDate(Timestamp.from(Instant.now()));
                    specialOrderBid.setCustomerFeedback(request.optString("customerFeedback", null));
                    specialOrderBid.setCustomerAdditionalNotes(request.optString("additionalNote", null));
                    specialOrderBid.setUpdatedDate(Timestamp.from(Instant.now()));
                    specialProductOrderBidRepository.save(specialOrderBid);

                    JSONObject data = specialProductService.addSpecialOrderProduct(specialOrder, specialOrderBid);

                    specialProductOrderBidRepository.findBySpecialOrderIdAndIsMerchantAccepted(specialOrderId, 1)
                            .forEach(otherBid -> {
                                if (!Objects.equals(otherBid.getBidId(), bidId)) {
                                    otherBid.setSelectionStatus(0);
                                    otherBid.setSelectionDate(Timestamp.from(Instant.now()));
                                    otherBid.setUpdatedDate(Timestamp.from(Instant.now()));
                                    specialProductOrderBidRepository.save(otherBid);
                                }
                            });

                    // Send notifications to merchants for all bids (selected and non-selected)
                    sendNotificationsToMerchants(); //TODO: to prepare for product delivery

                    responseMap.put("data", data)
                            .put("statusCode", ResponseCodes.SUCCESS)
                            .put("statusDescription", "Success")
                            .put("statusMessage", "Customer selection processed successfully.");

                }, () -> {
                    throw new ResourceNotFoundException("Special order bid not found for the provided details");
                });

        return responseMap;
    }


    private void sendNotificationsToMerchants() {
        // Implement notification logic for merchants (SMS, email, push notifications)
        // ...
    }


}
