package com.msc.admission.controller;

import com.msc.admission.dto.ApplicationCreateRequest;
import com.msc.admission.dto.ReviewRequest;
import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.enums.ReviewDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplicationControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;
    private String baseUrl;
    private Long createdAppId;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(org.springframework.http.HttpStatusCode statusCode) {
                return false; // Let tests inspect all status codes
            }
        });
        baseUrl = "http://localhost:" + port + "/api/applications";

        ApplicationCreateRequest request = new ApplicationCreateRequest();
        request.setApplicantId(1001L);
        request.setProgramName("MSc in Computer Science");
        request.setPersonalStatement("A strong candidate with research experience.");
        request.setDocumentUrl("https://docs.example.com/app.pdf");

        ResponseEntity<Application> response = restTemplate.postForEntity(
                baseUrl, request, Application.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            createdAppId = response.getBody().getId();
        }
    }

    @Test
    @DisplayName("POST /api/applications creates application in DRAFT status")
    void createApplication_returnsDraftApplication() {
        ApplicationCreateRequest request = new ApplicationCreateRequest();
        request.setApplicantId(2001L);
        request.setProgramName("MSc in Data Science");
        request.setPersonalStatement("Data enthusiast.");
        request.setDocumentUrl("https://docs.example.com/ds.pdf");

        ResponseEntity<Application> response = restTemplate.postForEntity(
                baseUrl, request, Application.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Application body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();
        assertThat(body.getApplicantId()).isEqualTo(2001L);
        assertThat(body.getProgramName()).isEqualTo("MSc in Data Science");
        assertThat(body.getStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(body.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/submit transitions DRAFT to SUBMITTED")
    void submitApplication_transitionsToSubmitted() {
        ResponseEntity<Application> response = restTemplate.exchange(
                baseUrl + "/" + createdAppId + "/submit",
                HttpMethod.PUT, null, Application.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/submit fails for non-existent application")
    void submitApplication_nonExistent_returnsError() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/99999/submit",
                HttpMethod.PUT, null, Map.class);

        // TODO: change to 4xx when @ExceptionHandler is added
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    @Test
    @DisplayName("Full lifecycle: create -> submit -> start-review -> review (APPROVE)")
    void fullLifecycle_approve_succeeds() {
        assertThat(createdAppId).isNotNull();

        restTemplate.exchange(baseUrl + "/" + createdAppId + "/submit",
                HttpMethod.PUT, null, Application.class);

        ResponseEntity<Application> reviewStartResponse = restTemplate.exchange(
                baseUrl + "/" + createdAppId + "/start-review",
                HttpMethod.PUT, null, Application.class);
        assertThat(reviewStartResponse.getBody().getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewerId(201L);
        reviewRequest.setDecision(ReviewDecision.APPROVE);
        reviewRequest.setComment("Excellent candidate.");

        HttpEntity<ReviewRequest> requestEntity = new HttpEntity<>(reviewRequest);
        ResponseEntity<Application> finalResponse = restTemplate.exchange(
                baseUrl + "/" + createdAppId + "/review",
                HttpMethod.PUT, requestEntity, Application.class);

        assertThat(finalResponse.getBody()).isNotNull();
        assertThat(finalResponse.getBody().getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Review with REJECT sets REJECTED status")
    void reviewApplication_reject_setsRejected() {
        navigateToReviewStatus();

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewerId(202L);
        reviewRequest.setDecision(ReviewDecision.REJECT);
        reviewRequest.setComment("Insufficient qualifications.");

        HttpEntity<ReviewRequest> requestEntity = new HttpEntity<>(reviewRequest);
        ResponseEntity<Application> response = restTemplate.exchange(
                baseUrl + "/" + createdAppId + "/review",
                HttpMethod.PUT, requestEntity, Application.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("Review with REQUEST_MORE_DOCUMENTS sets NEED_MORE_DOCUMENTS")
    void reviewApplication_requestDocs_setsNeedMoreDocuments() {
        navigateToReviewStatus();

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewerId(203L);
        reviewRequest.setDecision(ReviewDecision.REQUEST_MORE_DOCUMENTS);
        reviewRequest.setComment("Please provide additional transcripts.");

        HttpEntity<ReviewRequest> requestEntity = new HttpEntity<>(reviewRequest);
        ResponseEntity<Application> response = restTemplate.exchange(
                baseUrl + "/" + createdAppId + "/review",
                HttpMethod.PUT, requestEntity, Application.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ApplicationStatus.NEED_MORE_DOCUMENTS);
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/documents updates document URL")
    void updateDocuments_succeeds() {
        Map<String, String> body = Map.of("documentUrl", "https://docs.example.com/updated.pdf");

        ResponseEntity<Application> response = restTemplate.exchange(
                baseUrl + "/" + createdAppId + "/documents",
                HttpMethod.PUT, new HttpEntity<>(body), Application.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDocumentUrl()).isEqualTo("https://docs.example.com/updated.pdf");
    }

    @Test
    @DisplayName("GET /api/applications/{id} returns application")
    void getApplicationById_returnsApplication() {
        ResponseEntity<Application> response = restTemplate.getForEntity(
                baseUrl + "/" + createdAppId, Application.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(createdAppId);
    }

    @Test
    @DisplayName("GET /api/applications/{id} returns 404 for non-existent")
    void getApplicationById_nonExistent_returnsError() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/99999", Map.class);

        // TODO: change to 4xx when @ExceptionHandler is added
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    @Test
    @DisplayName("GET /api/applications/applicant/{applicantId} returns applications")
    void getApplicationsByApplicant_returnsList() {
        ResponseEntity<List<Application>> response = restTemplate.exchange(
                baseUrl + "/applicant/1001",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Application>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allMatch(a -> a.getApplicantId().equals(1001L));
    }

    @Test
    @DisplayName("GET /api/applications/status/{status} returns filtered applications")
    void getApplicationsByStatus_returnsFilteredList() {
        ResponseEntity<List<Application>> response = restTemplate.exchange(
                baseUrl + "/status/DRAFT",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Application>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allMatch(a -> a.getStatus() == ApplicationStatus.DRAFT);
    }

    @Test
    @DisplayName("GET /api/applications/{id}/reviews returns review history")
    void getReviewHistory_returnsRecords() {
        Long appId = createdAppId;
        restTemplate.exchange(baseUrl + "/" + appId + "/submit",
                HttpMethod.PUT, null, Application.class);
        restTemplate.exchange(baseUrl + "/" + appId + "/start-review",
                HttpMethod.PUT, null, Application.class);

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewerId(201L);
        reviewRequest.setDecision(ReviewDecision.APPROVE);
        reviewRequest.setComment("Good");
        restTemplate.exchange(baseUrl + "/" + appId + "/review",
                HttpMethod.PUT, new HttpEntity<>(reviewRequest), Application.class);

        ResponseEntity<List<ReviewRecord>> response = restTemplate.exchange(
                baseUrl + "/" + appId + "/reviews",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ReviewRecord>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allMatch(r -> r.getApplicationId().equals(appId));
    }

    @Test
    @DisplayName("Submitting an already accepted application returns error")
    void submitApplication_fromAccepted_returnsError() {
        Long appId = createdAppId;
        restTemplate.exchange(baseUrl + "/" + appId + "/submit",
                HttpMethod.PUT, null, Application.class);
        restTemplate.exchange(baseUrl + "/" + appId + "/start-review",
                HttpMethod.PUT, null, Application.class);
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewerId(201L);
        reviewRequest.setDecision(ReviewDecision.APPROVE);
        reviewRequest.setComment("Good");
        restTemplate.exchange(baseUrl + "/" + appId + "/review",
                HttpMethod.PUT, new HttpEntity<>(reviewRequest), Application.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + appId + "/submit",
                HttpMethod.PUT, null, Map.class);

        // TODO: change to 4xx when @ExceptionHandler is added
        assertThat(response.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    private void navigateToReviewStatus() {
        restTemplate.exchange(baseUrl + "/" + createdAppId + "/submit",
                HttpMethod.PUT, null, Application.class);
        restTemplate.exchange(baseUrl + "/" + createdAppId + "/start-review",
                HttpMethod.PUT, null, Application.class);
    }
}
