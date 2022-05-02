package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "ProductMore")
public class ProductMore {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic
    @Column(name = "Template")
    private String template;
    @Basic
    @Column(name = "PickType")
    private String pickType;
    @Basic
    @Column(name = "CatalogueType")
    private String catalogueType;
    @Basic
    @Column(name = "DisplayName")
    private String displayName;
    @Basic
    @Column(name = "MoreKey")
    private String moreKey;
    @Basic
    @Column(name = "ReturnNumber")
    private Integer returnNumber;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
