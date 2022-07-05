package com.commerce.pal.backend.repo.user.business;

import com.commerce.pal.backend.models.user.business.BusinessCollateral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessCollateralRepository extends JpaRepository<BusinessCollateral, Integer> {
    List<BusinessCollateral> findBusinessCollateralsByBusinessId(Long business);
    List<BusinessCollateral> findBusinessCollateralsByBusinessIdAndStatus (Long business, Integer status);
}
