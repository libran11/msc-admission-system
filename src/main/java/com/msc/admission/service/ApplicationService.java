package com.msc.admission.service;

import com.msc.admission.dto.ApplicationCreateRequest;
import com.msc.admission.dto.ReviewRequest;
import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.enums.ReviewDecision;
import com.msc.admission.repository.ApplicationRepository;
import com.msc.admission.repository.ReviewRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
                request.getDocumentUrl(),
                request.getTags() != null ? new HashSet<>(request.getTags()) : null
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

    /**
     * Replace the full tag set for an application.
     * If tags is null, the application tags are cleared.
     */
    public Application updateTags(Long applicationId, Set<String> tags) {
        Application application = getApplicationById(applicationId);
        application.setTags(tags != null ? new HashSet<>(tags) : new HashSet<>());
        application.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    /**
     * Add a single tag to an application. The tag is trimmed and deduplicated.
     */
    public Application addTag(Long applicationId, String tag) {
        Application application = getApplicationById(applicationId);
        if (tag != null && !tag.isBlank()) {
            application.getTags().add(tag.trim());
            application.setUpdatedAt(LocalDateTime.now());
            return applicationRepository.save(application);
        }
        return application;
    }

    /**
     * Search applications using dynamic filters and optional sorting.
     * Supports keyword search, status, programName, applicantId, and tag membership.
     */
    public List<Application> searchApplications(String keyword,
                                                ApplicationStatus status,
                                                String programName,
                                                Long applicantId,
                                                String tag,
                                                String sortBy,
                                                String sortOrder) {
        Specification<Application> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            String normalized = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("programName")), normalized),
                    cb.like(cb.lower(root.get("personalStatement")), normalized)
            ));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (programName != null && !programName.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("programName"), programName));
        }

        if (applicantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("applicantId"), applicantId));
        }

        if (tag != null && !tag.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.isMember(tag.trim(), root.get("tags")));
        }

        String normalizedSortBy = sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        List<String> allowedSortFields = Arrays.asList("createdAt", "updatedAt", "applicantId", "programName", "status");
        if (!allowedSortFields.contains(normalizedSortBy)) {
            normalizedSortBy = "createdAt";
        }

        return applicationRepository.findAll(spec, Sort.by(direction, normalizedSortBy));
    }

    /**
     * Return counts of applications grouped by their current status.
     */
    public Map<String, Long> getStatusCounts() {
        return applicationRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        statusCount -> statusCount.getStatus().name(),
                        ApplicationRepository.StatusCount::getCount,
                        Long::sum,
                        LinkedHashMap::new
                ));
    }

    /**
     * Return the most popular programs by application volume.
     */
    public List<ApplicationRepository.ProgramPopularity> getProgramPopularity() {
        return applicationRepository.countByProgramName();
    }

    /**
     * Return the most popular tags used on applications.
     */
    public List<ApplicationRepository.TagPopularity> getTagPopularity() {
        return applicationRepository.countByTags();
    }

    /**
     * Recommend popular programs that the applicant has not yet applied to.
     * Based on programs with accepted applications.
     */
    public List<String> getRecommendedPrograms(Long applicantId) {
        List<String> existingPrograms = applicantId == null ? Collections.emptyList()
                : applicationRepository.findByApplicantId(applicantId).stream()
                .map(Application::getProgramName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return applicationRepository.countByProgramNameAndStatus(ApplicationStatus.ACCEPTED).stream()
                .map(ApplicationRepository.ProgramPopularity::getProgramName)
                .filter(program -> existingPrograms.isEmpty() || !existingPrograms.contains(program))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
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