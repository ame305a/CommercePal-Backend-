package com.commerce.pal.backend.models.setting;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.util.Objects;

@Data
@Entity
public class Bank {
    @Id
    @Column(name = "BankCode")
    private String bankCode;
    @Basic@Column(name = "BankName")
    private String bankName;
    @Basic@Column(name = "Country")
    private String country;
    @Basic@Column(name = "Status")
    private Integer status;
    @Basic@Column(name = "CreatedDate")
    private Date createdDate;

}
