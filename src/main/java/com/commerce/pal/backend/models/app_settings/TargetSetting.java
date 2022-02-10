package com.commerce.pal.backend.models.app_settings;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class TargetSetting {
    @Id@Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Basic@Column(name = "Target")
    private String target;
    @Basic@Column(name = "TargetDisplay")
    private String targetDisplay;
    @Basic@Column(name = "Status")
    private Integer status;
    @Basic@Column(name = "CreatedDate")
    private Timestamp createdDate;

}
