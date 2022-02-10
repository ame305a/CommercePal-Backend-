package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class ProductImage {
    @Id
    @Column(name = "Id")
    private long id;
    @Basic
    @Column(name = "ProductId")
    private long productId;
    @Basic
    @Column(name = "Type")
    private String type;
    @Basic
    @Column(name = "File_Path")
    private String filePath;
    @Basic
    @Column(name = "Status")
    private int status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
