package com.commerce.pal.backend.models;

import com.commerce.pal.backend.utils.enumValues.SocialMedia;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

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
    @Column(name = "PhoneNumber")
    private String phoneNumber;
    @Basic
    @Column(name = "PinHash")
    private String pinHash;
    @Basic
    @Column(name = "DeviceId")
    private String deviceId;
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
    @Column(name = "IsPhoneValidated")
    private Integer isPhoneValidated;
    @Basic
    @Column(name = "IsEmailValidated")
    private Integer isEmailValidated;
    @Basic
    @Column(name = "OTPHash")
    private String otpHash;
    @Basic
    @Column(name = "EmailOtpHash")
    private String emailOtpHash;
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
    @Column(name = "UserOneSignalId")
    private String userOneSignalId;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
    @Basic
    @Column(name = "OAuthProvider")
    @Enumerated(value = EnumType.STRING)
    private SocialMedia oAuthProvider;
    @Basic
    @Column(name = "ProviderUserId")
    private String providerUserId;
}
