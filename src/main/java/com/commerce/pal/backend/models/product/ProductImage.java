package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class ProductImage {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "ProductId")
    private Long productId;
    @Basic
    @Column(name = "Type")
    private String type;
    @Basic
    @Column(name = "File_Path")
    private String filePath;
    @Basic
    @Column(name = "MobileImage")
    private String mobileImage;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
}
