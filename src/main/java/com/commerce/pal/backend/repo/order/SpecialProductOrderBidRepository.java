package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.SpecialProductOrderBid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpecialProductOrderBidRepository extends JpaRepository<SpecialProductOrderBid, Long> {

    List<SpecialProductOrderBid> findBySpecialOrderIdAndIsMerchantAccepted(Long specialOrderId, Integer isMerchantAccepted);

    List<SpecialProductOrderBid> findBySpecialOrderIdAndIsMerchantAcceptedAndIsMerchantProceedOk(Long specialOrderId, Integer isMerchantAccepted, Integer isMerchantProceedOk);

    Optional<SpecialProductOrderBid> findByBidIdAndMerchantId(Long bidId, Long merchantId);

    List<SpecialProductOrderBid> findByMerchantIdOrderByAssignedDateDesc(Long bidId);

    Optional<SpecialProductOrderBid> findByBidIdAndSpecialOrderIdAndIsMerchantAccepted(Long bidId, Long merchantId, Integer isMerchantAccepted);

    List<SpecialProductOrderBid> findBySpecialOrderId(Long specialOrderId);

}
