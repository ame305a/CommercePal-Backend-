package com.commerce.pal.backend.repo.user.business;

import com.commerce.pal.backend.models.user.business.BusinessCollateralDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessCollateralDocumentRepository extends JpaRepository<BusinessCollateralDocument, Integer> {
    
    List<BusinessCollateralDocument> findBusinessCollateralDocumentsByCollateralId(Integer collateral);
}
