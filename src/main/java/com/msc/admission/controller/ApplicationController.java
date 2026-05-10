package com.msc.admission.controller;

import com.msc.admission.dto.ApplicationCreateRequest;
import com.msc.admission.dto.ReviewRequest;
import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.repository.ApplicationRepository;
import com.msc.admission.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // Replace the full tag set for the specified application
    @PutMapping("/{id}/tags")
    public Application updateTags(@PathVariable Long id,
                                  @RequestBody Set<String> tags) {
        return applicationService.updateTags(id, tags);
    }

    // Add a single tag to the specified application
    @PostMapping("/{id}/tags")
    public Application addTag(@PathVariable Long id,
                              @RequestBody Map<String, String> body) {
        return applicationService.addTag(id, body.get("tag"));
    }

    // Search and filter applications by keyword, status, program, applicant or tag.
    // Supports sorting by createdAt, updatedAt, applicantId, programName, or status.
    @GetMapping("/search")
    public List<Application> searchApplications(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String programName,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder
    ) {
        return applicationService.searchApplications(keyword, status, programName, applicantId, tag, sortBy, sortOrder);
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

    // Return counts of applications grouped by status
    @GetMapping("/analytics/status-counts")
    public Map<String, Long> getStatusCounts() {
        return applicationService.getStatusCounts();
    }

    // Return most popular programs by application volume
    @GetMapping("/analytics/program-popularity")
    public List<ApplicationRepository.ProgramPopularity> getProgramPopularity() {
        return applicationService.getProgramPopularity();
    }

    // Return the most frequently used tags across applications
    @GetMapping("/analytics/tag-popularity")
    public List<ApplicationRepository.TagPopularity> getTagPopularity() {
        return applicationService.getTagPopularity();
    }

    // Recommend programs based on accepted program popularity, excluding programs already applied to by the applicant
    @GetMapping("/recommendations")
    public List<String> getRecommendations(@RequestParam(required = false) Long applicantId) {
        return applicationService.getRecommendedPrograms(applicantId);
    }
}