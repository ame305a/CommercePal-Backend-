package com.commerce.pal.backend.module.users.referral_codes;

import com.commerce.pal.backend.models.user.Agent;
import com.commerce.pal.backend.models.user.Customer;
import com.commerce.pal.backend.models.user.referral_codes.ReferringUserType;
import com.commerce.pal.backend.repo.user.AgentRepository;
import com.commerce.pal.backend.repo.user.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ReferralCodeUtils {

    @Value("${org.commerce.pal.referral.code.signupPoints:2}")
    // Default value is 2 if SIGNUP_POINTS environment variable is not set
    private int signupPoints;

    @Value("${org.commerce.pal.referral.code.checkoutPoints:2}")
    // Default value is 2 if CHECKOUT_POINTS environment variable is not set
    private int checkoutPoints;

    private static final String REFERRAL_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoiding 'I', 'O', '1', '0'
    private static final int REFERRAL_CODE_LENGTH = 8;
    private static final String CUSTOMER_PREFIX = "CU";
    private static final String AGENT_PREFIX = "AG";

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;


    public String generateCustomerReferralCode() {
        // Generate initial referral code
        String referralCode = generateReferralCode(CUSTOMER_PREFIX);

        // Check if the generated code already exists, regenerate until unique
        while (customerRepository.existsByReferralCode(referralCode)) {
            referralCode = generateReferralCode(CUSTOMER_PREFIX);
        }

        return referralCode;
    }

    public String generateAgentReferralCode() {
        // Generate initial referral code
        String referralCode = generateReferralCode(AGENT_PREFIX);

        // Check if the generated code already exists, regenerate until unique
        while (agentRepository.existsByReferralCode(referralCode)) {
            referralCode = generateReferralCode(AGENT_PREFIX);
        }

        return referralCode;
    }

    public String getReferringUserType(String referralCode) {
        if (referralCode.startsWith(AGENT_PREFIX)) {
            return ReferringUserType.AGENT.toString();
        } else if (referralCode.startsWith(CUSTOMER_PREFIX)) {
            return ReferringUserType.CUSTOMER.toString();
        } else {
            throw new IllegalArgumentException("Invalid referral code : " + referralCode);
        }
    }

    public Long getReferringUserId(String referralCode) {
        if (referralCode.startsWith(AGENT_PREFIX)) {
            Agent agent = agentRepository.findByReferralCode(referralCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid referral code: " + referralCode));

            return agent.getAgentId();
        } else if (referralCode.startsWith(CUSTOMER_PREFIX)) {
            Customer customer = customerRepository.findByReferralCode(referralCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid referral code: " + referralCode));

            return customer.getCustomerId();
        } else {
            throw new IllegalArgumentException("Invalid referral code : " + referralCode);
        }
    }

    private String generateReferralCode(String prefix) {
        //CU == CUSTOMER
        //AG == AGENT

        int prefixLength = prefix.length();
        int codeLength = REFERRAL_CODE_LENGTH - prefixLength;
        char[] code = new char[REFERRAL_CODE_LENGTH];

        // Copy the prefix to the beginning of the code array
        for (int i = 0; i < prefixLength; i++) {
            code[i] = prefix.charAt(i);
        }

        // Generate the random part of the referral code
        for (int i = 0; i < codeLength; i++) {
            int index = ThreadLocalRandom.current().nextInt(REFERRAL_CODE_CHARACTERS.length());
            code[prefixLength + i] = REFERRAL_CODE_CHARACTERS.charAt(index);
        }

        return new String(code);
    }


    public void generateReferralCodesForExistingData() {
        List<Agent> agents = agentRepository.findAll();
        int agentCount = agents.size();

        Set<String> referralCodes = new HashSet<>();
        while (referralCodes.size() < agentCount) {
            String referralCode = generateReferralCode(AGENT_PREFIX);
            referralCodes.add(referralCode);
        }

        int agentIndex = 0;
        for (Agent agent : agents) {
            agent.setReferralCode(referralCodes.toArray(new String[0])[agentIndex]);
            agentIndex++;
        }

        agentRepository.saveAll(agents);


        List<Customer> customers = customerRepository.findAll();
        int customerCount = customers.size();

        Set<String> customerReferralCodes = new HashSet<>();
        while (customerReferralCodes.size() < customerCount) {
            String referralCode = generateReferralCode(CUSTOMER_PREFIX);
            customerReferralCodes.add(referralCode);
        }

        int customerIndex = 0;
        for (Customer customer : customers) {
            customer.setReferralCode(customerReferralCodes.toArray(new String[0])[customerIndex]);
            customerIndex++;
        }

        customerRepository.saveAll(customers);
    }

//    public JSONObject calculateReferralPoints(List<ReferralPointsTransaction> transactions) {
//        int[] statistics = transactions.stream()
//                .collect(
//                        () -> new int[3], // Initial array for [0] totalEarnedPoints, [1] usedPoints, [2] availableBalance
//                        (acc, transaction) -> {
//                            if (transaction.getReferralTransactionType() == ReferralTransactionType.REFERRAL ||
//                                    transaction.getReferralTransactionType() == ReferralTransactionType.TRANSFER_RECEIVED) {
//                                acc[0] += transaction.getPointsChange(); // Accumulate totalEarnedPoints
//                            } else if (transaction.getReferralTransactionType() == ReferralTransactionType.REDEMPTION ||
//                                    transaction.getReferralTransactionType() == ReferralTransactionType.TRANSFER_SENT) {
//                                acc[1] += transaction.getPointsChange(); // Accumulate usedPoints
//                            }
//                        },
//                        (acc1, acc2) -> {
//                            acc1[0] += acc2[0]; // Combine totalEarnedPoints
//                            acc1[1] += acc2[1]; // Combine usedPoints
//                        }
//                );
//
//        int totalEarnedPoints = statistics[0];
//        int usedPoints = statistics[1];
//        int availableBalance = totalEarnedPoints - usedPoints;
//
//        JSONObject result = new JSONObject();
//        result.put("availablePoints", availableBalance);
//        result.put("totalEarnedPoints", totalEarnedPoints);
//        result.put("usedPoints", usedPoints);
//
//        return result;
//    }
//
//    public void createReferralTransaction(Long userId, int pointsChange, ReferralTransactionType transactionType) {
//        ReferralPointsTransaction transaction = new ReferralPointsTransaction();
//        transaction.setUserId(userId);
//        transaction.setUserType(ReferringUserType.CUSTOMER);
//        transaction.setPointsChange(pointsChange);
//        transaction.setReferralTransactionType(transactionType);
//        transaction.setCreatedAt(Timestamp.from(Instant.now()));
//        referralPointsTransactionRepository.save(transaction);
//    }

}
