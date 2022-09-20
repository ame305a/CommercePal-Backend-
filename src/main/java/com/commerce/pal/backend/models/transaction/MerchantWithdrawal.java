package com.commerce.pal.backend.models.transaction;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

@Data
@Entity
public class MerchantWithdrawal {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "MerchantId")
    private Long merchantId;
    @Basic
    @Column(name = "TransRef")
    private String transRef;
    @Basic
    @Column(name = "WithdrawalMethod")
    private String withdrawalMethod;
    @Basic
    @Column(name = "WithdrawalType")
    private String withdrawalType;
    @Basic
    @Column(name = "Account")
    private String account;
    @Basic
    @Column(name = "Amount")
    private BigDecimal amount;
    @Basic
    @Column(name = "ValidationCode")
    private String validationCode;
    @Basic
    @Column(name = "ValidationDate")
    private Timestamp validationDate;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "RequestDate")
    private Timestamp requestDate;
    @Basic
    @Column(name = "VerifiedBy")
    private Integer verifiedBy;
    @Basic
    @Column(name = "VerificationComment")
    private String verificationComment;
    @Basic
    @Column(name = "VerificationDate")
    private Timestamp verificationDate;
    @Basic
    @Column(name = "ResponsePayload")
    private String responsePayload;
    @Basic
    @Column(name = "BillTransRef")
    private String billTransRef;
    @Basic
    @Column(name = "ResponseStatus")
    private Integer responseStatus;
    @Basic
    @Column(name = "ResponseDescription")
    private String responseDescription;
    @Basic
    @Column(name = "ResponseDate")
    private Timestamp responseDate;

}
