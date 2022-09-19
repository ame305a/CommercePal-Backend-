package com.commerce.pal.backend.models.setting;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class Region {
    @Id
    @Column(name = "Id")
    private Integer id;
    @Basic
    @Column(name = "RegionCode")
    private String regionCode;
    @Basic
    @Column(name = "RegionName")
    private String regionName;
    @Basic
    @Column(name = "Country")
    private String country;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
