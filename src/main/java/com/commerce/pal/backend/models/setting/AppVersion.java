package com.commerce.pal.backend.models.setting;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Data
@Entity
public class AppVersion {
    @Id
    @Column(name = "Id")
    private Integer id;
    @Basic
    @Column(name = "AndroidVersion")
    private String androidVersion;
    @Basic
    @Column(name = "IosVersion")
    private String iosVersion;
    @Basic
    @Column(name = "AndroidUpdate")
    private String androidUpdate;
    @Basic
    @Column(name = "AndroidUpdateType")
    private String androidUpdateType;
    @Basic
    @Column(name = "AndroidComment")
    private String androidComment;
    @Basic
    @Column(name = "IosUpdate")
    private String iosUpdate;
    @Basic
    @Column(name = "IosUpdateType")
    private String iosUpdateType;
    @Basic
    @Column(name = "IosComment")
    private String iosComment;
    @Basic
    @Column(name = "SessionTimeout")
    private Integer sessionTimeout;
    @Basic
    @Column(name = "SmsHash")
    private String smsHash;
}
