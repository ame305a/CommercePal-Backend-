package com.commerce.pal.backend.models.order;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
public class SpecialProductOrderBid {

    @Id
    @Column(name = "BidId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;

    @Column(name = "SpecialOrderId")
    private Long specialOrderId;

    @Column(name = "MerchantId")
    private Long merchantId;

    @Column(name = "IsMerchantAccepted")
    private Integer isMerchantAccepted;

    @Column(name = "IsMerchantProceedOk")
    private Integer isMerchantProceedOk;

    @Column(name = "OfferDetails")
    private String offerDetails;

    @Column(name = "OfferPrice")
    private BigDecimal offerPrice;

    @Column(name = "OfferStatus")
    private Integer offerStatus;

    @Column(name = "MerchantResponseDate")
    private Timestamp merchantResponseDate;

    @Column(name = "OfferExpirationDate")
    private Timestamp offerExpirationDate;

    @Column(name = "SelectionDate")
    private Timestamp selectionDate;

    @Column(name = "SelectionStatus")
    private Integer selectionStatus;

    @Column(name = "CustomerFeedback")
    private String customerFeedback;

    @Column(name = "CustomerAdditionalNotes")
    private String customerAdditionalNotes;

    @Column(name = "MerchantAdditionalNotes")
    private String merchantAdditionalNotes;

    @Column(name = "AssignedDate")
    private Timestamp assignedDate;

    @Column(name = "UpdatedDate")
    private Timestamp updatedDate;

    @Column(name = "updatedBy")
    private Long updatedBy;
}

