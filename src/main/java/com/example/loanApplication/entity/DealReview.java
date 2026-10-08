package com.example.loanApplication.entity;

import com.example.loanApplication.enumeration.ReviewStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DealReviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealReview {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewId")
    private Integer reviewId;

    @Column(name = "DealId", nullable = false)
    private Integer dealId;

    @Column(name = "OfficerId", nullable = false)
    private Integer officerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    private ReviewStatus status;
}