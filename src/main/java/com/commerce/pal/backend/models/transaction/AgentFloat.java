package com.commerce.pal.backend.models.transaction;

import lombok.*;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class AgentFloat {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic
    @Column(name = "AgentId")
    private Long agentId;
    @Basic
    @Column(name = "Amount")
    private Double amount;
    @Basic
    @Column(name = "Comment")
    private String comment;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "RequestDate")
    private Timestamp requestDate;
    @Basic
    @Column(name = "ReviewedBy")
    private Integer reviewedBy;
    @Basic
    @Column(name = "Review")
    private String review;
    @Basic
    @Column(name = "ReviewDate")
    private Timestamp reviewDate;
    @Basic
    @Column(name = "TransRef")
    private String transRef;
    @Basic
    @Column(name = "ProcessedDate")
    private Timestamp processedDate;
}
