package com.commerce.pal.backend.models.setting;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class Country {
    @Id
    @Column(name = "CountryId")
    private long countryId;
    @Basic
    @Column(name = "Country")
    private String country;
    @Basic
    @Column(name = "CountryCode")
    private String countryCode;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
