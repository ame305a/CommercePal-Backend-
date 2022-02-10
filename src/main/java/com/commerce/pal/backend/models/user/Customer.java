package com.commerce.pal.backend.models.user;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class Customer {
    @Id@Column(name = "CustomerId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long customerId;
    @Basic@Column(name = "EmailAddress")
    private String emailAddress;
    @Basic@Column(name = "PhoneNumber")
    private String phoneNumber;
    @Basic@Column(name = "FirstName")
    private String firstName;
    @Basic@Column(name = "MiddleName")
    private String middleName;
    @Basic@Column(name = "LastName")
    private String lastName;
    @Basic@Column(name = "Language")
    private String language;
    @Basic@Column(name = "Country")
    private String country;
    @Basic@Column(name = "City")
    private String city;
    @Basic@Column(name = "District")
    private String district;
    @Basic@Column(name = "Location")
    private String location;
    @Basic@Column(name = "Status")
    private Integer status;
    @Basic@Column(name = "RegisteredBy")
    private String registeredBy;
    @Basic@Column(name = "RegisteredDate")
    private Timestamp registeredDate;
    @Basic@Column(name = "CreatedAt")
    private Timestamp createdAt;

}
