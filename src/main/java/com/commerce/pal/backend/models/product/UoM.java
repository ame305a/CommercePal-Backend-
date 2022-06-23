package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class UoM {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic
    @Column(name = "UoM")
    private String uoM;
    @Basic
    @Column(name = "Description")
    private String description;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
}
