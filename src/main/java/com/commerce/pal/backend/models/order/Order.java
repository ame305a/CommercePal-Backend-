package com.commerce.pal.backend.models.order;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "Order")
public class Order {
    @Id
    @Column(name = "OrderId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderId;
    @Basic
    @Column(name = "OrderRef")
    private String orderRef;
    @Basic
    @Column(name = "CustomerId")
    private Long customerId;
    @Basic
    @Column(name = "MerchantId")
    private Long merchantId;
    @Basic
    @Column(name = "AgentId")
    private Long agentId;
    @Basic
    @Column(name = "BusinessId")
    private Long businessId;
    @Basic
    @Column(name = "PaymentMethod")
    private String paymentMethod;
    @Basic
    @Column(name = "SaleType")
    private String saleType;
    @Basic
    @Column(name = "TotalPrice")
    private BigDecimal totalPrice;
    @Basic
    @Column(name = "Discount")
    private BigDecimal discount;
    @Basic
    @Column(name = "Tax")
    private BigDecimal tax;
    @Basic
    @Column(name = "DeliveryPrice")
    private BigDecimal deliveryPrice;
    @Basic
    @Column(name = "OrderDate")
    private Timestamp orderDate;
    @Basic
    @Column(name = "IsAgentInitiated")
    private Integer isAgentInitiated;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "StatusDescription")
    private String statusDescription;
    @Basic
    @Column(name = "VerifiedBy")
    private String verifiedBy;

}
