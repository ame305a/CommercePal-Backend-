package com.commerce.pal.backend.models.app_settings;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class SchemaCatalogue {
    @Id@Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Basic@Column(name = "SchemaSettingId")
    private Integer schemaSettingId;
    @Basic@Column(name = "TargetSchemaId")
    private Integer targetSchemaId;
    @Basic@Column(name = "CategoryType")
    private String categoryType;
    @Basic@Column(name = "CategoryId")
    private Integer categoryId;
    @Basic@Column(name = "Description")
    private String description;
    @Basic@Column(name = "Status")
    private Integer status;
    @Basic@Column(name = "CreatedDate")
    private Timestamp createdDate;

}
