package com.commerce.pal.backend.models.app_settings;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class TargetSectionChildren {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Basic
    @Column(name = "TargetSectionId")
    private int targetSectionId;
    @Basic
    @Column(name = "Type")
    private String type;
    @Basic
    @Column(name = "ItemId")
    private long itemId;
    @Basic
    @Column(name = "Description")
    private String description;
    @Basic
    @Column(name = "Status")
    private int status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
