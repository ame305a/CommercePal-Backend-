package com.commerce.pal.backend.models.user;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class Agent {
    @Id
    @Column(name = "AgentId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long agentId;
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
    @Column(name = "OwnerPhoneNumber")
    private String ownerPhoneNumber;
    @Basic
    @Column(name = "BusinessPhoneNumber")
    private String businessPhoneNumber;
    @Basic
    @Column(name = "AgentName")
    private String agentName;
    @Basic
    @Column(name = "AgentCode")
    private String agentCode;
    @Basic
    @Column(name = "AgentCategory")
    private String agentCategory;
    @Basic
    @Column(name = "AgentType")
    private Boolean agentType;
    @Basic
    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;
    @Basic
    @Column(name = "Branch")
    private String branch;
    @Basic
    @Column(name = "TaxNumber")
    private String taxNumber;
    @Basic
    @Column(name = "Country")
    private String country;
    @Basic
    @Column(name = "City")
    private String city;
    @Basic
    @Column(name = "District")
    private String district;
    @Basic
    @Column(name = "Location")
    private String location;
    @Basic
    @Column(name = "Language")
    private String language;
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
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "RegisteredBy")
    private String registeredBy;
    @Basic
    @Column(name = "AuthorizedBy")
    private String authorizedBy;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
    @Basic
    @Column(name = "DeactivatedBy")
    private String deactivatedBy;
    @Basic
    @Column(name = "DeactivatedComment")
    private String deactivatedComment;
    @Basic
    @Column(name = "DeactivatedDate")
    private Timestamp deactivatedDate;
    @Basic
    @Column(name = "ActivatedBy")
    private String activatedBy;
    @Basic
    @Column(name = "ActivatedComment")
    private String activatedComment;
    @Basic
    @Column(name = "ActivatedDate")
    private Timestamp activatedDate;

}
