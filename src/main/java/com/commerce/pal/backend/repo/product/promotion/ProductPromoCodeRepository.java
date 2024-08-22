package com.commerce.pal.backend.repo.product.promotion;

import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCode;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import com.commerce.pal.backend.models.product.promotion.PromoCodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductPromoCodeRepository extends JpaRepository<ProductPromoCode, Long>, JpaSpecificationExecutor<ProductPromoCode> {

    List<ProductPromoCode> findByOwnerAndMerchantIdOrderByUpdatedDateDesc(ProductPromoCodeOwner owner, Long merchantId);

    List<ProductPromoCode> findByOwnerOrderByUpdatedDateDesc(ProductPromoCodeOwner owner);

    List<ProductPromoCode> findByPromoCodeStatusOrderByUpdatedDateDesc(PromoCodeStatus owner);

    Optional<ProductPromoCode> findByIdAndOwnerAndMerchantId(Long id, ProductPromoCodeOwner owner, Long merchantId);

    Optional<ProductPromoCode> findByIdAndOwner(Long id, ProductPromoCodeOwner owner);

    Optional<ProductPromoCode> findByCode(String code);

    boolean existsByCode(String code);
}
