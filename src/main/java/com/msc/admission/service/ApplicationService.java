package com.msc.admission.service;

import com.msc.admission.dto.ApplicationCreateRequest;
import com.msc.admission.dto.ReviewRequest;
import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.enums.ReviewDecision;
import com.msc.admission.repository.ApplicationRepository;
import com.msc.admission.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ReviewRecordRepository reviewRecordRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ReviewRecordRepository reviewRecordRepository) {
        this.applicationRepository = applicationRepository;
        this.reviewRecordRepository = reviewRecordRepository;
    }

    public Application createApplication(ApplicationCreateRequest request) {
        Application application = new Application(
                request.getApplicantId(),
                request.getProgramName(),
                request.getPersonalStatement(),
                request.getDocumentUrl()
        );

        return applicationRepository.save(application);
    }

    public Application submitApplication(Long applicationId) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.DRAFT
                && application.getStatus() != ApplicationStatus.NEED_MORE_DOCUMENTS) {
            throw new IllegalStateException("Only draft or document-required applications can be submitted.");
        }

        if (application.getProgramName() == null || application.getProgramName().isBlank()) {
            throw new IllegalStateException("Program name is required.");
        }

        if (application.getDocumentUrl() == null || application.getDocumentUrl().isBlank()) {
            throw new IllegalStateException("Required document has not been uploaded.");
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public Application startReview(Long applicationId) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted applications can enter review.");
        }

        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public Application reviewApplication(Long applicationId, ReviewRequest request) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only applications under review can be reviewed.");
        }

        ReviewRecord record = new ReviewRecord(
                applicationId,
                request.getReviewerId(),
                request.getDecision(),
                request.getComment()
        );

        reviewRecordRepository.save(record);

        if (request.getDecision() == ReviewDecision.APPROVE) {
            application.setStatus(ApplicationStatus.ACCEPTED);
        } else if (request.getDecision() == ReviewDecision.REJECT) {
            application.setStatus(ApplicationStatus.REJECTED);
        } else if (request.getDecision() == ReviewDecision.REQUEST_MORE_DOCUMENTS) {
            application.setStatus(ApplicationStatus.NEED_MORE_DOCUMENTS);
        }

        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public Application updateDocuments(Long applicationId, String documentUrl) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.DRAFT
                && application.getStatus() != ApplicationStatus.NEED_MORE_DOCUMENTS) {
            throw new IllegalStateException("Documents can only be updated before final decision.");
        }

        application.setDocumentUrl(documentUrl);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found."));
    }

    public List<Application> getApplicationsByApplicant(Long applicantId) {
        return applicationRepository.findByApplicantId(applicantId);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<ReviewRecord> getReviewHistory(Long applicationId) {
        return reviewRecordRepository.findByApplicationId(applicationId);
    }
}