package com.msc.admission.service;

import com.msc.admission.dto.ApplicationCreateRequest;
import com.msc.admission.dto.ReviewRequest;
import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.enums.ReviewDecision;
import com.msc.admission.repository.ApplicationRepository;
import com.msc.admission.repository.ReviewRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ReviewRecordRepository reviewRecordRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Captor
    private ArgumentCaptor<Application> applicationCaptor;

    private Application draftApp;
    private Application submittedApp;
    private Application underReviewApp;
    private Application acceptedApp;
    private Application rejectedApp;
    private Application needDocsApp;

    @BeforeEach
    void setUp() {
        draftApp = createApplicationWithId(1L, 1001L, "MSc in Computer Science", ApplicationStatus.DRAFT);
        submittedApp = createApplicationWithId(2L, 1002L, "MSc in Data Science", ApplicationStatus.SUBMITTED);
        underReviewApp = createApplicationWithId(3L, 1003L, "MSc in AI", ApplicationStatus.UNDER_REVIEW);
        acceptedApp = createApplicationWithId(4L, 1004L, "MSc in CS", ApplicationStatus.ACCEPTED);
        rejectedApp = createApplicationWithId(5L, 1005L, "MSc in DS", ApplicationStatus.REJECTED);
        needDocsApp = createApplicationWithId(6L, 1006L, "MSc in SE", ApplicationStatus.NEED_MORE_DOCUMENTS);
    }

    private Application createApplicationWithId(Long id, Long applicantId, String programName, ApplicationStatus status) {
        Application app = new Application(applicantId, programName, "Personal statement", "https://docs.example.com/test.pdf");
        ReflectionTestUtils.setField(app, "id", id);
        app.setStatus(status);
        return app;
    }

    @Nested
    @DisplayName("Create application")
    class Create {

        @Test
        @DisplayName("should create application with DRAFT status")
        void createApplication_setsDraftStatus() {
            ApplicationCreateRequest request = new ApplicationCreateRequest();
            request.setApplicantId(1001L);
            request.setProgramName("MSc in Computer Science");
            request.setPersonalStatement("Statement");
            request.setDocumentUrl("https://docs.example.com/doc.pdf");

            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.createApplication(request);

            assertThat(result.getApplicantId()).isEqualTo(1001L);
            assertThat(result.getProgramName()).isEqualTo("MSc in Computer Science");
            assertThat(result.getPersonalStatement()).isEqualTo("Statement");
            assertThat(result.getDocumentUrl()).isEqualTo("https://docs.example.com/doc.pdf");
            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.DRAFT);
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Submit application")
    class Submit {

        @Test
        @DisplayName("should submit from DRAFT status")
        void submitApplication_fromDraft_succeeds() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.submitApplication(1L);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        }

        @Test
        @DisplayName("should submit from NEED_MORE_DOCUMENTS status")
        void submitApplication_fromNeedMoreDocuments_succeeds() {
            when(applicationRepository.findById(6L)).thenReturn(Optional.of(needDocsApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.submitApplication(6L);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        }

        @Test
        @DisplayName("should throw when submitting from non-draft status")
        void submitApplication_fromInvalidStatus_throws() {
            when(applicationRepository.findById(4L)).thenReturn(Optional.of(acceptedApp));

            assertThatThrownBy(() -> applicationService.submitApplication(4L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only draft");
        }

        @Test
        @DisplayName("should throw when program name is blank")
        void submitApplication_withBlankProgramName_throws() {
            Application app = createApplicationWithId(7L, 1001L, "", ApplicationStatus.DRAFT);
            when(applicationRepository.findById(7L)).thenReturn(Optional.of(app));

            assertThatThrownBy(() -> applicationService.submitApplication(7L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Program name");
        }

        @Test
        @DisplayName("should throw when document URL is blank")
        void submitApplication_withBlankDocumentUrl_throws() {
            Application app = createApplicationWithId(7L, 1001L, "MSc in CS", ApplicationStatus.DRAFT);
            ReflectionTestUtils.setField(app, "documentUrl", "");
            when(applicationRepository.findById(7L)).thenReturn(Optional.of(app));

            assertThatThrownBy(() -> applicationService.submitApplication(7L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("document");
        }
    }

    @Nested
    @DisplayName("Start review")
    class StartReview {

        @Test
        @DisplayName("should start review from SUBMITTED status")
        void startReview_fromSubmitted_succeeds() {
            when(applicationRepository.findById(2L)).thenReturn(Optional.of(submittedApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.startReview(2L);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        }

        @Test
        @DisplayName("should throw when starting review from non-submitted status")
        void startReview_fromInvalidStatus_throws() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));

            assertThatThrownBy(() -> applicationService.startReview(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only submitted");
        }
    }

    @Nested
    @DisplayName("Review application")
    class Review {

        @Test
        @DisplayName("APPROVE should set ACCEPTED status and save review record")
        void reviewApplication_approve_setsAccepted() {
            ReviewRequest request = new ReviewRequest();
            request.setReviewerId(201L);
            request.setDecision(ReviewDecision.APPROVE);
            request.setComment("Approved");

            when(applicationRepository.findById(3L)).thenReturn(Optional.of(underReviewApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.reviewApplication(3L, request);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
            verify(reviewRecordRepository).save(any(ReviewRecord.class));
        }

        @Test
        @DisplayName("REJECT should set REJECTED status and save review record")
        void reviewApplication_reject_setsRejected() {
            ReviewRequest request = new ReviewRequest();
            request.setReviewerId(201L);
            request.setDecision(ReviewDecision.REJECT);
            request.setComment("Rejected");

            when(applicationRepository.findById(3L)).thenReturn(Optional.of(underReviewApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.reviewApplication(3L, request);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        }

        @Test
        @DisplayName("REQUEST_MORE_DOCUMENTS should set NEED_MORE_DOCUMENTS status")
        void reviewApplication_requestDocs_setsNeedMoreDocuments() {
            ReviewRequest request = new ReviewRequest();
            request.setReviewerId(201L);
            request.setDecision(ReviewDecision.REQUEST_MORE_DOCUMENTS);
            request.setComment("Need more");

            when(applicationRepository.findById(3L)).thenReturn(Optional.of(underReviewApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.reviewApplication(3L, request);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.NEED_MORE_DOCUMENTS);
        }

        @Test
        @DisplayName("should throw when reviewing from non-under-review status")
        void reviewApplication_fromInvalidStatus_throws() {
            ReviewRequest request = new ReviewRequest();
            request.setReviewerId(201L);
            request.setDecision(ReviewDecision.APPROVE);
            request.setComment("Ok");

            when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));

            assertThatThrownBy(() -> applicationService.reviewApplication(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only applications under review");
        }
    }

    @Nested
    @DisplayName("Update documents")
    class UpdateDocuments {

        @Test
        @DisplayName("should update documents in DRAFT status")
        void updateDocuments_fromDraft_succeeds() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.updateDocuments(1L, "https://docs.example.com/new.pdf");

            assertThat(result.getDocumentUrl()).isEqualTo("https://docs.example.com/new.pdf");
        }

        @Test
        @DisplayName("should update documents in NEED_MORE_DOCUMENTS status")
        void updateDocuments_fromNeedMoreDocuments_succeeds() {
            when(applicationRepository.findById(6L)).thenReturn(Optional.of(needDocsApp));
            when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Application result = applicationService.updateDocuments(6L, "https://docs.example.com/new.pdf");

            assertThat(result.getDocumentUrl()).isEqualTo("https://docs.example.com/new.pdf");
        }

        @Test
        @DisplayName("should throw when updating documents in final status")
        void updateDocuments_fromFinalStatus_throws() {
            when(applicationRepository.findById(4L)).thenReturn(Optional.of(acceptedApp));

            assertThatThrownBy(() -> applicationService.updateDocuments(4L, "https://docs.example.com/new.pdf"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documents can only be updated");
        }
    }

    @Nested
    @DisplayName("Query")
    class Query {

        @Test
        @DisplayName("get by ID returns application when found")
        void getById_found_returnsApplication() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));

            Application result = applicationService.getApplicationById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("get by ID throws when not found")
        void getById_notFound_throws() {
            when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.getApplicationById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("get by applicant returns list")
        void getByApplicant_returnsList() {
            when(applicationRepository.findByApplicantId(1001L)).thenReturn(List.of(draftApp));

            List<Application> result = applicationService.getApplicationsByApplicant(1001L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("get by status returns list")
        void getByStatus_returnsList() {
            when(applicationRepository.findByStatus(ApplicationStatus.ACCEPTED)).thenReturn(List.of(acceptedApp));

            List<Application> result = applicationService.getApplicationsByStatus(ApplicationStatus.ACCEPTED);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("get review history returns records")
        void getReviewHistory_returnsRecords() {
            ReviewRecord record = new ReviewRecord(3L, 201L, ReviewDecision.APPROVE, "Good");
            when(reviewRecordRepository.findByApplicationId(3L)).thenReturn(List.of(record));

            List<ReviewRecord> result = applicationService.getReviewHistory(3L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApplicationId()).isEqualTo(3L);
        }
    }
}
