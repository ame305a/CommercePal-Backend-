package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class SubProductImage {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "SubProductId")
    private Long subProductId;
    @Basic
    @Column(name = "Type")
    private String type;
    @Basic
    @Column(name = "ImageUrl")
    private String imageUrl;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
