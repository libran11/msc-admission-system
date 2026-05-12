package com.msc.admission.repository;

import com.msc.admission.entity.Application;
import com.msc.admission.enums.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        applicationRepository.save(new Application(1001L, "MSc in Computer Science", "PS1", "url1"));
        Application submitted = new Application(1002L, "MSc in Data Science", "PS2", "url2");
        submitted.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.save(submitted);
        Application accepted = new Application(1002L, "MSc in Artificial Intelligence", "PS3", "url3");
        accepted.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(accepted);
    }

    @Test
    @DisplayName("find by applicant ID returns matching applications")
    void findByApplicantId_returnsMatchingApplications() {
        List<Application> result = applicationRepository.findByApplicantId(1002L);
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getApplicantId().equals(1002L));
    }

    @Test
    @DisplayName("find by applicant ID returns empty when none exist")
    void findByApplicantId_returnsEmpty_whenNoneExist() {
        List<Application> result = applicationRepository.findByApplicantId(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("find by status returns matching applications")
    void findByStatus_returnsMatchingApplications() {
        List<Application> result = applicationRepository.findByStatus(ApplicationStatus.DRAFT);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ApplicationStatus.DRAFT);
    }

    @Test
    @DisplayName("find by status returns empty when none match")
    void findByStatus_returnsEmpty_whenNoneMatch() {
        List<Application> result = applicationRepository.findByStatus(ApplicationStatus.REJECTED);
        assertThat(result).isEmpty();
    }
}
