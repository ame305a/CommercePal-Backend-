package com.commerce.pal.backend.repo.product.flashSale;

import com.commerce.pal.backend.models.product.SubProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleProduct;
import com.commerce.pal.backend.models.product.flashSale.FlashSaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.*;
import java.util.Optional;

public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Long> {
    Optional<FlashSaleProduct> findBySubProductSubProductId(long subProductId);

    Optional<FlashSaleProduct> findBySubProductSubProductIdAndStatus(long subProductId, FlashSaleStatus flashSaleStatus);

    @Query(value = "SELECT * FROM FlashSaleProduct fs WHERE 1=1 " +
            "AND (:merchantId IS NULL OR fs.MerchantId = :merchantId) " +
            "AND (:status IS NULL OR fs.Status = :status)",
            nativeQuery = true)
    Page<FlashSaleProduct> findFlashSales(
            Long merchantId,
            String status,
            Pageable pageable);

    Optional<FlashSaleProduct> findByIdAndMerchantId(long id, long merchantId);

    Optional<FlashSaleProduct> findBySubProductProductIdAndStatus(long productId, FlashSaleStatus flashSaleStatus);
}
