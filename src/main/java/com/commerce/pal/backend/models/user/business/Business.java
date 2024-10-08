package com.commerce.pal.backend.models.user.business;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class Business {
    @Id
    @Column(name = "BusinessId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long businessId;
    @Basic
    @Column(name = "OwnerType")
    private String ownerType;
    @Basic
    @Column(name = "OwnerId")
    private Integer ownerId;
    @Basic
    @Column(name = "EmailAddress")
    private String emailAddress;
    @Basic
    @Column(name = "BusinessName")
    private String businessName;
    @Basic
    @Column(name = "BusinessPhoneNumber")
    private String businessPhoneNumber;
    @Basic
    @Column(name = "OwnerPhoneNumber")
    private String ownerPhoneNumber;
    @Basic
    @Column(name = "BusinessSector")
    private Integer businessSector;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "Language")
    private String language;
    @Basic
    @Column(name = "Country")
    private String country;
    @Basic
    @Column(name = "City")
    private Integer city;
    @Basic
    @Column(name = "District")
    private String district;
    @Basic
    @Column(name = "Location")
    private String location;
    @Basic
    @Column(name = "RegionId")
    private Integer regionId;
    @Basic
    @Column(name = "ServiceCodeId")
    private Integer serviceCodeId;
    @Basic
    @Column(name = "PhysicalAddress")
    private String physicalAddress;
    @Basic
    @Column(name = "Longitude")
    private String longitude;
    @Basic
    @Column(name = "Latitude")
    private String latitude;
    @Basic
    @Column(name = "TermsOfServiceStatus")
    private Integer termsOfServiceStatus;
    @Basic
    @Column(name = "TermsOfServiceDate")
    private Timestamp termsOfServiceDate;
    @Basic
    @Column(name = "ShopImage")
    private String shopImage;
    @Basic
    @Column(name = "CommercialCertNo")
    private String commercialCertNo;
    @Basic
    @Column(name = "CommercialCertImage")
    private String commercialCertImage;
    @Basic
    @Column(name = "TillNumber")
    private String tillNumber;
    @Basic
    @Column(name = "TillNumberImage")
    private String tillNumberImage;
    @Basic
    @Column(name = "OwnerPhoto")
    private String ownerPhoto;
    @Basic
    @Column(name = "BusinessRegistrationPhoto")
    private String businessRegistrationPhoto;
    @Basic
    @Column(name = "TaxPhoto")
    private String taxPhoto;
    @Basic
    @Column(name = "FinancialInstitution")
    private Integer financialInstitution;
    @Basic
    @Column(name = "LoanLimit")
    private BigDecimal loanLimit;
    @Basic
    @Column(name = "LimitStatus")
    private Integer limitStatus;
    @Basic
    @Column(name = "LimitBy")
    private Long limitBy;
    @Basic
    @Column(name = "LimitComment")
    private String limitComment;
    @Basic
    @Column(name = "LimitDate")
    private Timestamp limitDate;

    @Basic
    @Column(name = "ReviewedBy")
    private Long reviewedBy;
    @Basic
    @Column(name = "ReviewComment")
    private String reviewComment;
    @Basic
    @Column(name = "ReviewedDate")
    private Timestamp reviewedDate;

    @Basic
    @Column(name = "CreatedBy")
    private String createdBy;
    @Basic
    @Column(name = "CreatedDate")
    private Date createdDate;
    @Basic
    @Column(name = "VerifiedBy")
    private String verifiedBy;
    @Basic
    @Column(name = "VerifiedDate")
    private Date verifiedDate;
    @Basic
    @Column(name = "DeactivatedBy")
    private String deactivatedBy;
    @Basic
    @Column(name = "DeactivatedComment")
    private String deactivatedComment;
    @Basic
    @Column(name = "DeactivatedDate")
    private Date deactivatedDate;
    @Basic
    @Column(name = "ActivatedBy")
    private String activatedBy;
    @Basic
    @Column(name = "ActivatedComment")
    private String activatedComment;
    @Basic
    @Column(name = "ActivatedDate")
    private Date activatedDate;

}
