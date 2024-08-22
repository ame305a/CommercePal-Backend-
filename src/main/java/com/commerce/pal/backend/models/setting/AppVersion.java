package com.commerce.pal.backend.models.setting;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity
public class AppVersion {
    @Id
    @Column(name = "Id")
    private Integer id;

    @Column(name = "AndroidVersion")
    private String androidVersion;

    @Column(name = "IosVersion")
    private String iosVersion;

    @Column(name = "AndroidUpdate")
    private String androidUpdate;

    @Column(name = "AndroidUpdateType")
    private String androidUpdateType;

    @Column(name = "AndroidComment")
    private String androidComment;

    @Column(name = "IosUpdate")
    private String iosUpdate;

    @Column(name = "IosUpdateType")
    private String iosUpdateType;

    @Column(name = "IosComment")
    private String iosComment;

    @Column(name = "SessionTimeout")
    private Integer sessionTimeout;

    @Column(name = "SmsHash")
    private String smsHash;
}
