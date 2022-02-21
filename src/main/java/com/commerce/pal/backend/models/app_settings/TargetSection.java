package com.commerce.pal.backend.models.app_settings;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class TargetSection {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Basic
    @Column(name = "TargetId")
    private int targetId;
    @Basic
    @Column(name = "DisplayName")
    private String displayName;
    @Basic
    @Column(name = "SectionKey")
    private String sectionKey;
    @Basic
    @Column(name = "Description")
    private String description;
    @Basic
    @Column(name = "CatalogueType")
    private String catalogueType;
    @Basic
    @Column(name = "Template")
    private String template;
    @Basic
    @Column(name = "OrderNumber")
    private int orderNumber;
    @Basic
    @Column(name = "Status")
    private int status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
