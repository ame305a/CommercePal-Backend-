package com.commerce.pal.backend.module.users.business;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.user.business.BusinessCollateral;
import com.commerce.pal.backend.models.user.business.BusinessCollateralDocument;
import com.commerce.pal.backend.repo.user.business.BusinessCollateralDocumentRepository;
import com.commerce.pal.backend.repo.user.business.BusinessCollateralRepository;
import com.commerce.pal.backend.repo.user.business.BusinessRepository;
import com.commerce.pal.backend.service.amazon.UploadService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class BusinessCollateralService {
    @Autowired
    private UploadService uploadService;

    private final BusinessRepository businessRepository;
    private final BusinessCollateralRepository businessCollateralRepository;
    private final BusinessCollateralDocumentRepository businessCollateralDocumentRepository;

    @Autowired
    public BusinessCollateralService(BusinessRepository businessRepository,
                                     BusinessCollateralRepository businessCollateralRepository,
                                     BusinessCollateralDocumentRepository businessCollateralDocumentRepository) {
        this.businessRepository = businessRepository;
        this.businessCollateralRepository = businessCollateralRepository;
        this.businessCollateralDocumentRepository = businessCollateralDocumentRepository;
    }

    public JSONObject addCollateral(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(reqBody.getLong("BusinessId"))
                    .ifPresentOrElse(business -> {
                        AtomicReference<BusinessCollateral> collateral = new AtomicReference<>(new BusinessCollateral());
                        collateral.get().setBusinessId(reqBody.getLong("BusinessId"));
                        collateral.get().setFinancialInstitution(reqBody.getLong("FinancialInstitution"));
                        collateral.get().setCollateralName(reqBody.getString("CollateralName"));
                        collateral.get().setCollateralType(reqBody.getString("CollateralType"));
                        collateral.get().setCollateralDescription(reqBody.getString("Description"));
                        collateral.get().setEstimateWorth(new BigDecimal(reqBody.getString("EstimateWorth")));
                        collateral.get().setApprovedAmount(new BigDecimal(0));
                        collateral.get().setComments(reqBody.getString("Comments"));
                        collateral.get().setStatus(0);
                        collateral.get().setCreatedDate(Timestamp.from(Instant.now()));
                        collateral.set(businessCollateralRepository.save(collateral.get()));
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("CollateralId", collateral.get().getId())
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject updateCollateral(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            businessCollateralRepository.findById(reqBody.getInt("CollateralId"))
                    .ifPresentOrElse(collateral -> {
                        collateral.setCollateralName(reqBody.getString("CollateralName"));
                        collateral.setFinancialInstitution(reqBody.getLong("FinancialInstitution"));
                        collateral.setCollateralType(reqBody.getString("CollateralType"));
                        collateral.setCollateralDescription(reqBody.getString("Description"));
                        collateral.setEstimateWorth(new BigDecimal(reqBody.getString("EstimateWorth")));
                        collateral.setComments(reqBody.getString("Comments"));
                        businessCollateralRepository.save(collateral);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("CollateralId", collateral.getId())
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Collatera does not exist")
                                .put("statusMessage", "Collatera does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject addCollateralDocument(MultipartFile multipartFile, Integer CollateralId) {
        JSONObject responseMap = new JSONObject();
        try {
            String documentUrl = uploadService.uploadFileAlone(multipartFile, "Web", "Collateral");
            businessCollateralRepository.findById(CollateralId)
                    .ifPresentOrElse(businessCollateral -> {
                        AtomicReference<BusinessCollateralDocument> collateral = new AtomicReference<>(new BusinessCollateralDocument());
                        collateral.get().setCollateralId(CollateralId);
                        collateral.get().setDocumentUrl(documentUrl);
                        collateral.get().setStatus(0);
                        collateral.get().setCreatedDate(Timestamp.from(Instant.now()));
                        businessCollateralDocumentRepository.save(collateral.get());

                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("documentUrl", documentUrl)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Collateral does not exist")
                                .put("statusMessage", "Collateral does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getBusinessCollateral(Long businessId) {
        JSONObject responseMap = new JSONObject();
        try {
            List<JSONObject> collaterals = new ArrayList<>();
            businessCollateralRepository.findBusinessCollateralsByBusinessId(businessId)
                    .forEach(businessCollateral -> {
                        JSONObject collateral = new JSONObject();
                        collateral.put("CollateralId", businessCollateral.getId());
                        collateral.put("CollateralName", businessCollateral.getCollateralName());
                        collateral.put("CollateralType", businessCollateral.getCollateralType());
                        collateral.put("FinancialInstitution", businessCollateral.getFinancialInstitution());
                        collateral.put("Description", businessCollateral.getCollateralDescription());
                        collateral.put("EstimateWorth", businessCollateral.getEstimateWorth());
                        collateral.put("ApprovedAmount", businessCollateral.getApprovedAmount());
                        collateral.put("Comments", businessCollateral.getComments());
                        collateral.put("Status", businessCollateral.getStatus());
                        collateral.put("CreatedDate", businessCollateral.getCreatedDate());
                        collateral.put("ReviewComments", businessCollateral.getReviewComments());
                        collateral.put("ReviewedDate", businessCollateral.getReviewedDate());
                        collateral.put("ReviewedBy", businessCollateral.getReviewedBy());
                        collateral.put("ApprovalRemarks", businessCollateral.getApprovalRemarks());
                        collateral.put("ApprovalDate", businessCollateral.getApprovalDate());
                        collateral.put("ApprovalBy", businessCollateral.getApprovedBy());
                        ArrayList<String> documents = new ArrayList<String>();
                        businessCollateralDocumentRepository.findBusinessCollateralDocumentsByCollateralId(
                                businessCollateral.getId()
                        ).forEach(collateralDocument -> {
                            documents.add(collateralDocument.getDocumentUrl());
                        });
                        collateral.put("documents", documents);
                        collaterals.add(collateral);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", collaterals)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject collateralLoanLimit(Long businessId) {
        JSONObject responseMap = new JSONObject();
        try {
            AtomicReference<BigDecimal> limit = new AtomicReference<>(new BigDecimal(0));
            businessCollateralRepository.findBusinessCollateralsByBusinessIdAndStatus(businessId, 3)
                    .forEach(businessCollateral -> {
                        limit.set(new BigDecimal(businessCollateral.getApprovedAmount().doubleValue() + limit.get().doubleValue()));
                    });
            responseMap.put("Limit", limit.get());
        } catch (Exception e) {
            responseMap.put("Limit", 0.00);
        }
        return responseMap;
    }

    public JSONObject reviewCollateralLimit(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(reqBody.getLong("BusinessId"))
                    .ifPresentOrElse(business -> {
                        businessCollateralRepository.findById(reqBody.getInt("CollateralId"))
                                .ifPresent(businessCollateral -> {
                                    businessCollateral.setStatus(1);
                                    businessCollateral.setReviewedBy(reqBody.getInt("UserId"));
                                    businessCollateral.setReviewComments(reqBody.getString("Comments"));
                                    businessCollateral.setReviewedDate(Timestamp.from(Instant.now()));
                                    businessCollateralRepository.save(businessCollateral);
                                });
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject approveCollateralLimit(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(reqBody.getLong("BusinessId"))
                    .ifPresentOrElse(business -> {
                        businessCollateralRepository.findById(reqBody.getInt("CollateralId"))
                                .ifPresent(businessCollateral -> {
                                    businessCollateral.setStatus(reqBody.getInt("Status"));
                                    businessCollateral.setApprovedAmount(reqBody.getBigDecimal("ApprovedAmount"));
                                    businessCollateral.setApprovedBy(reqBody.getInt("UserId"));
                                    businessCollateral.setApprovalRemarks(reqBody.getString("Comments"));
                                    businessCollateral.setApprovalDate(Timestamp.from(Instant.now()));
                                    businessCollateralRepository.save(businessCollateral);
                                });
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject updateLoanLimit(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(reqBody.getLong("BusinessId"))
                    .ifPresentOrElse(business -> {
                        business.setFinancialInstitution(reqBody.getInt("FinancialInstitution"));
                        business.setLoanLimit(new BigDecimal(reqBody.getString("LoanLimit")));
                        business.setLimitStatus(0);
                        business.setLimitComment(reqBody.getString("Comments"));
                        business.setLimitDate(Timestamp.from(Instant.now()));
                        businessRepository.save(business);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getLoanLimit(Long businessId) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(businessId)
                    .ifPresentOrElse(business -> {
                        responseMap.put("LoanLimit", business.getLoanLimit());
                        responseMap.put("FinancialInstitution", business.getFinancialInstitution());
                        responseMap.put("LimitStatus", business.getLimitStatus());
                        responseMap.put("LimitDate", business.getLimitDate());
                        responseMap.put("LimitComment", business.getLimitComment());
                        responseMap.put("ReviewComment", business.getReviewComment());
                        responseMap.put("ReviewedDate", business.getReviewedDate());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject reviewLoanLimit(JSONObject reqBody) {
        JSONObject responseMap = new JSONObject();
        try {
            businessRepository.findBusinessByBusinessId(reqBody.getLong("BusinessId"))
                    .ifPresentOrElse(business -> {
                        business.setLoanLimit(reqBody.getBigDecimal("LoanLimit"));
                        business.setLimitStatus(reqBody.getInt("Status"));
                        business.setReviewComment(reqBody.getString("Comments"));
                        business.setReviewedDate(Timestamp.from(Instant.now()));
                        businessRepository.save(business);
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Business does not exist")
                                .put("statusMessage", "Business does not exist");
                    });
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage());
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", e.getMessage())
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }


}
