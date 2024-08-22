package com.commerce.pal.backend.repo.user.referral_codes;

import com.commerce.pal.backend.models.user.referral_codes.Referrals;
import com.commerce.pal.backend.models.user.referral_codes.ReferringUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralsRepository extends JpaRepository<Referrals, Long> {

    List<Referrals> findByReferringUserIdAndReferringUserTypeOrderByCreatedAtDesc(Long referringUserId, ReferringUserType referringUserType);

}
