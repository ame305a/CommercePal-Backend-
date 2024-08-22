package com.commerce.pal.backend.module.users.referral_codes;

import com.commerce.pal.backend.dto.CommercePalCoinTransferRequest;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.models.user.referral_codes.*;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import com.commerce.pal.backend.repo.user.referral_codes.ReferralsRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Log
@Service
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class ReferralCodeService {
    private final ReferralsRepository referralsRepository;
    private final CustomerRepository customerRepository;
    private final GlobalMethods globalMethods;

    public JSONObject retrieveCustomerReferralsHistory() {
        Customer customer = globalMethods.getCurrentLogginedCustomer();
        Long customerId = customer.getCustomerId();

        List<Referrals> referrals = referralsRepository
                .findByReferringUserIdAndReferringUserTypeOrderByCreatedAtDesc(customerId, ReferringUserType.CUSTOMER);

        JSONArray referralArray = new JSONArray();
        referrals.forEach(referral -> referralArray.put(referralToJson(referral)));

        JSONObject responseObject = new JSONObject();
        responseObject.put("referralCode", customer.getReferralCode());
        responseObject.put("referrals", referralArray);

        return GlobalMethods.enhanceWithSuccessStatus(responseObject);
    }

    private JSONObject referralToJson(Referrals referral) {
        String customerName = customerRepository.findById(referral.getReferredUserId())
                .map(customer -> customer.getFirstName() + " " + customer.getLastName())
                .orElse("");

        JSONObject referralJsonObject = new JSONObject();
        referralJsonObject.put("referralId", referral.getId());
        referralJsonObject.put("referredUser", customerName);
        referralJsonObject.put("referralType", referral.getReferralType());
        referralJsonObject.put("birrEarned", referral.getBirrEarned() + " ETB");
        referralJsonObject.put("usedAt", referral.getCreatedAt());

        return referralJsonObject;
    }

    public JSONObject transferCommercePalCoins(CommercePalCoinTransferRequest transferRequest) {
        globalMethods.fetchUserDetails();
        return null;
    }

//    @Transactional
//    public JSONObject transferReferralPoints(ReferralTransferDTO referralTransferDTO) {
//        String phoneOrEmail = referralTransferDTO.getPhoneOrEmail();
//        int pointsToTransfer = referralTransferDTO.getPointsToTransfer();
//
//        Customer senderCustomer = globalMethods.getCurrentLogginedCustomer();
//        Customer receiverCustomer = customerRepository.findCustomerByEmailAddressOrPhoneNumber(phoneOrEmail, phoneOrEmail)
//                .orElseThrow(() -> new ResourceNotFoundException("Receiver Customer is not found"));
//
//        Long senderCustomerId = senderCustomer.getCustomerId();
//        Long receiverCustomerId = receiverCustomer.getCustomerId();
//
//        if (senderCustomerId.equals(receiverCustomerId))
//            throw new BadRequestException("Transfer of referral points to your own account is not allowed.");
//
//        List<ReferralPointsTransaction> referralPointsTransactions = referralPointsTransactionRepository.findByUserIdAndUserType(senderCustomerId, ReferringUserType.CUSTOMER);
//        JSONObject referralPoints = referralCodeUtils.calculateReferralPoints(referralPointsTransactions);
//        int availablePoints = referralPoints.getInt("availablePoints");
//
//
//        if (availablePoints < pointsToTransfer) {
//            String errorMessage = String.format("Insufficient referral points: your available referral points is %d.", availablePoints);
//            throw new BadRequestException(errorMessage);
//        }
//
//
//        referralCodeUtils.createReferralTransaction(receiverCustomerId, pointsToTransfer, ReferralTransactionType.TRANSFER_RECEIVED);
//        referralCodeUtils.createReferralTransaction(senderCustomerId, pointsToTransfer, ReferralTransactionType.TRANSFER_SENT);
//
//        JSONObject responseObject = new JSONObject();
//        responseObject.put("statusCode", ResponseCodes.SUCCESS)
//                .put("statusDescription", "Success")
//                .put("statusMessage", String.format("%d referral points have been successfully transferred to %s %s.",
//                        pointsToTransfer,
//                        receiverCustomer.getFirstName(),
//                        receiverCustomer.getLastName()));
//
//        return responseObject;
//    }
//
//    public JSONObject retrieveReferralTransactions() {
//        Long customerId = globalMethods.getCurrentLogginedCustomer().getCustomerId();
//
//        List<ReferralPointsTransaction> referralPointsTransactions = referralPointsTransactionRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(customerId, ReferringUserType.CUSTOMER);
//
//        JSONArray transactionsArray = new JSONArray();
//        referralPointsTransactions.forEach(transaction -> {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("TransactionType", transaction.getReferralTransactionType());
//            jsonObject.put("pointsChange", transaction.getPointsChange());
//            jsonObject.put("transactionDate", transaction.getCreatedAt());
//
//            transactionsArray.put(jsonObject);
//        });
//
//        JSONObject responseObject = new JSONObject();
//        responseObject.put("referralPointsTransactions", transactionsArray);
//        return GlobalMethods.enhanceWithSuccessStatus(responseObject);
//    }

}
