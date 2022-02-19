package com.commerce.pal.backend.models.user;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Data
@Entity
public class Distributor {
    @Id
    @Column(name = "DistributorId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long distributorId;
    @Basic
    @Column(name = "EmailAddress")
    private String emailAddress;
    @Basic
    @Column(name = "PhoneNumber")
    private String phoneNumber;
    @Basic
    @Column(name = "DistributorName")
    private String distributorName;
    @Basic
    @Column(name = "DistributorType")
    private String distributorType;
    @Basic
    @Column(name = "Country")
    private String country;
    @Basic
    @Column(name = "City")
    private String city;
    @Basic
    @Column(name = "Branch")
    private String branch;
    @Basic
    @Column(name = "District")
    private String district;
    @Basic
    @Column(name = "Location")
    private String location;
    @Basic
    @Column(name = "IdNumber")
    private String idNumber;
    @Basic
    @Column(name = "IdImage")
    private String idImage;
    @Basic
    @Column(name = "PhotoImage")
    private String photoImage;
    @Basic
    @Column(name = "Longitude")
    private String longitude;
    @Basic
    @Column(name = "Latitude")
    private String latitude;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CanRegAgent")
    private Integer canRegAgent;
    @Basic
    @Column(name = "CanRegMerchant")
    private Integer canRegMerchant;
    @Basic
    @Column(name = "CanRegBusiness")
    private Integer canRegBusiness;
    @Basic
    @Column(name = "CreatedBy")
    private String createdBy;
    @Basic
    @Column(name = "CreatedDate")
    private Date createdDate;
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
