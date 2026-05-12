package com.msc.admission.entity;

import com.msc.admission.enums.ApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
//entity里的两个java会通过 JPA 自动映射成 MySQL 数据库里的两张表
//Application.java 表示一条 学生申请记录。
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicantId;

    private String programName;

    @Column(length = 1000)
    private String personalStatement;

    private String documentUrl;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Application() {
    }

    public Application(Long applicantId, String programName, String personalStatement, String documentUrl) {
        this.applicantId = applicantId;
        this.programName = programName;
        this.personalStatement = personalStatement;
        this.documentUrl = documentUrl;
        this.status = ApplicationStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getPersonalStatement() {
        return personalStatement;
    }

    public void setPersonalStatement(String personalStatement) {
        this.personalStatement = personalStatement;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}