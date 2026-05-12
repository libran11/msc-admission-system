package com.msc.admission.entity;
//表示一条 审核记录
import com.msc.admission.enums.ReviewDecision;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_records")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;

    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    private ReviewDecision decision;

    @Column(length = 1000)
    private String comment;

    private LocalDateTime reviewedAt;

    public ReviewRecord() {
    }

    public ReviewRecord(Long applicationId, Long reviewerId, ReviewDecision decision, String comment) {
        this.applicationId = applicationId;
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.comment = comment;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public ReviewDecision getDecision() {
        return decision;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}