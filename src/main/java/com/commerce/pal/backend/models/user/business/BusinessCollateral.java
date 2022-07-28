package com.commerce.pal.backend.models.user.business;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class BusinessCollateral {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic
    @Column(name = "BusinessId")
    private long businessId;
    @Basic
    @Column(name = "FinancialInstitution")
    private Long financialInstitution;
    @Basic
    @Column(name = "CollateralName")
    private String collateralName;
    @Basic
    @Column(name = "CollateralType")
    private String collateralType;
    @Basic
    @Column(name = "CollateralDescription")
    private String collateralDescription;
    @Basic
    @Column(name = "EstimateWorth")
    private BigDecimal estimateWorth;
    @Basic
    @Column(name = "ApprovedAmount")
    private BigDecimal approvedAmount;
    @Basic
    @Column(name = "Comments")
    private String comments;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
    @Basic
    @Column(name = "ReviewedBy")
    private Integer reviewedBy;
    @Basic
    @Column(name = "ReviewComments")
    private String reviewComments;
    @Basic
    @Column(name = "ReviewedDate")
    private Timestamp reviewedDate;
    @Basic
    @Column(name = "ApprovedBy")
    private Integer approvedBy;
    @Basic
    @Column(name = "ApprovalRemarks")
    private String approvalRemarks;
    @Basic
    @Column(name = "ApprovalDate")
    private Timestamp approvalDate;
}
