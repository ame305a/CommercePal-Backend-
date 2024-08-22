package com.commerce.pal.backend.models.product;


import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "ProductReview")
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewId")
    private Long reviewId;

    @Column(name = "CustomerId")
    private Long customerId;

    @Column(name = "ProductId")
    private Long productId;

    @Column(name = "Rating")
    private double rating;

    @Column(name = "Content")
    private String content;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "VideoUrl")
    private String videoUrl;

    @Column(name = "Verified")
    private boolean verified;

    @Column(name = "HelpfulCount")
    private int helpfulCount;

    @Column(name = "Status")
    private int status;

    @Column(name = "CreatedAt")
    private Timestamp createdAt;
}
