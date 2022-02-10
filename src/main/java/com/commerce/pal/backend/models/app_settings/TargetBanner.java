package com.commerce.pal.backend.models.app_settings;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class TargetBanner {
    @Id@Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Basic@Column(name = "SchemaSettingId")
    private int schemaSettingId;
    @Basic@Column(name = "type")
    private String type;
    @Basic@Column(name = "BannerUrl")
    private String bannerUrl;
    @Basic@Column(name = "Status")
    private Integer status;
    @Basic@Column(name = "CreatedDate")
    private Timestamp createdDate;

}
