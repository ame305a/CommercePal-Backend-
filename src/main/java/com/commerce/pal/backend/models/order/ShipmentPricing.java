package com.commerce.pal.backend.models.order;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "[ShipmentPricing]")
public class ShipmentPricing {
    @Id
    @Basic
    @Column(name = "Id")
    private Integer id;
    @Basic
    @Column(name = "Source")
    private Integer source;
    @Basic
    @Column(name = "Destination")
    private Integer destination;
    @Basic
    @Column(name = "Currency")
    private String currency;
    @Basic
    @Column(name = "ShipmentType")
    private String shipmentType;
    @Basic
    @Column(name = "DeliveryType")
    private String deliveryType;
    @Basic
    @Column(name = "ServiceMode")
    private String serviceMode;
    @Basic
    @Column(name = "Rate")
    private BigDecimal rate;
    @Basic
    @Column(name = "UUID")
    private String uniqueUid;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
    @Basic
    @Column(name = "UpdatedDate")
    private Timestamp updatedDate;

}
