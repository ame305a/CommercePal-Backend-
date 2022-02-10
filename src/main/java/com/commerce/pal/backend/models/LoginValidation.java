package com.commerce.pal.backend.models;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class LoginValidation {
    @Id
    @Column(name = "LoginId")
    private long loginId;
    @Basic
    @Column(name = "EmailAddress")
    private String emailAddress;
    @Basic
    @Column(name = "PinHash")
    private String pinHash;
    @Basic
    @Column(name = "PinAttempt")
    private Integer pinAttempt;
    @Basic
    @Column(name = "LastAttemptDate")
    private Timestamp lastAttemptDate;
    @Basic
    @Column(name = "PinChange")
    private Integer pinChange;
    @Basic
    @Column(name = "OTPHash")
    private String otpHash;
    @Basic
    @Column(name = "OTPRequired")
    private Byte otpRequired;
    @Basic
    @Column(name = "PasswordResetToken")
    private String passwordResetToken;
    @Basic
    @Column(name = "PasswordResetTokenStatus")
    private Integer passwordResetTokenStatus;
    @Basic
    @Column(name = "PasswordResetTokenExpire")
    private Timestamp passwordResetTokenExpire;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
}
