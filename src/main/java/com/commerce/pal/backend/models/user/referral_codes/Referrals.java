package com.commerce.pal.backend.models.user.referral_codes;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "Referrals")
@Setter
@Getter
public class Referrals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    //Referral code owner info
    @Column(nullable = false, name = "ReferringUserType")
    @Enumerated(EnumType.STRING)
    private ReferringUserType referringUserType; //(CUSTOMER, AGENT)

    @Column(name = "ReferringUserId", nullable = false)
    private Long referringUserId;  //Referral code Owner

    //Referred user info
    @Column(name = "ReferredUserId", nullable = false)
    private Long referredUserId;

    @Column(nullable = false, name = "ReferralType")
    @Enumerated(EnumType.STRING)
    private ReferralType referralType; //(SIGNUP, CHECKOUT)

    @Column(nullable = false, name = "BirrEarned")
    private BigDecimal birrEarned;

    @Column(nullable = false, name = "CreatedAt")
    private Timestamp createdAt;
}
