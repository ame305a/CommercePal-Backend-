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
public class City {
    @Id
    @Column(name = "CityId")
    private long cityId;
    @Basic
    @Column(name = "CountryId")
    private Long countryId;
    @Basic
    @Column(name = "City")
    private String city;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
