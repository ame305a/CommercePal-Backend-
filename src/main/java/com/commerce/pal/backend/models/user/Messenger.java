package com.commerce.pal.backend.models.user;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Data
@Entity
public class Messenger {
    @Id
    @Column(name = "MessengerId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long messengerId;
    @Basic
    @Column(name = "EmailAddress")
    private String emailAddress;
    @Basic
    @Column(name = "OwnerType")
    private String ownerType;
    @Basic
    @Column(name = "OwnerId")
    private Integer ownerId;
    @Basic
    @Column(name = "OwnerPhoneNumber")
    private String ownerPhoneNumber;
    @Basic
    @Column(name = "Language")
    private String language;
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
    @Column(name = "Longitude")
    private String longitude;
    @Basic
    @Column(name = "Latitude")
    private String latitude;
    @Basic
    @Column(name = "IdNumber")
    private String idNumber;
    @Basic
    @Column(name = "IdNumberImage")
    private String idNumberImage;
    @Basic
    @Column(name = "DrivingLicenceNumber")
    private String drivingLicenceNumber;
    @Basic
    @Column(name = "DrivingLicenceImage")
    private String drivingLicenceImage;
    @Basic
    @Column(name = "InsuranceExpiry")
    private String insuranceExpiry;
    @Basic
    @Column(name = "PoliceClearanceImage")
    private String policeClearanceImage;
    @Basic
    @Column(name = "MessengerPhoto")
    private String messengerPhoto;
    @Basic
    @Column(name = "CarrierType")
    private String carrierType;
    @Basic
    @Column(name = "CarrierLicencePlate")
    private String carrierLicencePlate;
    @Basic
    @Column(name = "CarrierImage")
    private String carrierImage;
    @Basic
    @Column(name = "OwnerPhoto")
    private String ownerPhoto;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "NextKinNames")
    private String nextKinNames;
    @Basic
    @Column(name = "NextKinPhone")
    private String nextKinPhone;
    @Basic
    @Column(name = "NextKinEmail")
    private String nextKinEmail;
    @Basic
    @Column(name = "NextKinId")
    private String nextKinId;
    @Basic
    @Column(name = "NextKinPhoto")
    private String nextKinPhoto;
    @Basic
    @Column(name = "TermsOfServiceStatus")
    private Integer termsOfServiceStatus;
    @Basic
    @Column(name = "TermsOfServiceDate")
    private Date termsOfServiceDate;
    @Basic
    @Column(name = "RegisteredBy")
    private String registeredBy;
    @Basic
    @Column(name = "RegisteredDate")
    private Date registeredDate;
    @Basic
    @Column(name = "AuthorizedBy")
    private String authorizedBy;
    @Basic
    @Column(name = "AuthorizedDate")
    private Date authorizedDate;
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
