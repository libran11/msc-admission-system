package com.msc.admission.controller;

import com.msc.admission.dto.ApplicationCreateRequest;
import com.msc.admission.dto.ReviewRequest;
import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public Application createApplication(@RequestBody ApplicationCreateRequest request) {
        return applicationService.createApplication(request);
    }

    @PutMapping("/{id}/submit")
    public Application submitApplication(@PathVariable Long id) {
        return applicationService.submitApplication(id);
    }

    @PutMapping("/{id}/start-review")
    public Application startReview(@PathVariable Long id) {
        return applicationService.startReview(id);
    }

    @PutMapping("/{id}/review")
    public Application reviewApplication(@PathVariable Long id,
                                         @RequestBody ReviewRequest request) {
        return applicationService.reviewApplication(id, request);
    }

    @PutMapping("/{id}/documents")
    public Application updateDocuments(@PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        return applicationService.updateDocuments(id, body.get("documentUrl"));
    }

    @GetMapping("/{id}")
    public Application getApplication(@PathVariable Long id) {
        return applicationService.getApplicationById(id);
    }

    @GetMapping("/applicant/{applicantId}")
    public List<Application> getApplicationsByApplicant(@PathVariable Long applicantId) {
        return applicationService.getApplicationsByApplicant(applicantId);
    }

    @GetMapping("/status/{status}")
    public List<Application> getApplicationsByStatus(@PathVariable ApplicationStatus status) {
        return applicationService.getApplicationsByStatus(status);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewRecord> getReviewHistory(@PathVariable Long id) {
        return applicationService.getReviewHistory(id);
    }
}