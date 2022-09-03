package com.commerce.pal.backend.repo.user;

import com.amazonaws.services.greengrassv2.model.LambdaIsolationMode;
import com.commerce.pal.backend.models.user.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findMerchantByEmailAddress(String email);

    Optional<Merchant> findMerchantByMerchantId(Long id);

    List<Merchant> findMerchantsByOwnerIdAndOwnerType(Integer owner, String ownerType);

    List<Merchant> findMerchantsByOwnerIdAndOwnerTypeOrderByCreatedDateDesc(Integer owner, String ownerType);

    Optional<Merchant> findMerchantByOwnerIdAndOwnerTypeAndMerchantId(Integer owner, String ownerType, Long id);

    Integer countByOwnerIdAndOwnerType(Integer owner, String ownerType);
}
