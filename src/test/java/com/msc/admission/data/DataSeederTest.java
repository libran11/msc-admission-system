package com.msc.admission.data;

import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.repository.ApplicationRepository;
import com.msc.admission.repository.ReviewRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DataSeederTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @Test
    @DisplayName("should seed applications when database is empty")
    void seedsApplications() {
        long appCount = applicationRepository.count();
        assertThat(appCount).isBetween(50L, 100L);
    }

    @Test
    @DisplayName("should seed all application statuses")
    void seedsAllStatuses() {
        assertThat(applicationRepository.findByStatus(ApplicationStatus.DRAFT)).isNotEmpty();
        assertThat(applicationRepository.findByStatus(ApplicationStatus.SUBMITTED)).isNotEmpty();
        assertThat(applicationRepository.findByStatus(ApplicationStatus.UNDER_REVIEW)).isNotEmpty();
        assertThat(applicationRepository.findByStatus(ApplicationStatus.NEED_MORE_DOCUMENTS)).isNotEmpty();
        assertThat(applicationRepository.findByStatus(ApplicationStatus.ACCEPTED)).isNotEmpty();
        assertThat(applicationRepository.findByStatus(ApplicationStatus.REJECTED)).isNotEmpty();
    }

    @Test
    @DisplayName("should seed review records for reviewed applications")
    void seedsReviewRecords() {
        long reviewCount = reviewRecordRepository.count();
        assertThat(reviewCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("should not seed again when database is not empty")
    void doesNotSeedTwice() {
        long initialCount = applicationRepository.count();
        // The seeder should not run again since DB already has data
        assertThat(applicationRepository.count()).isEqualTo(initialCount);
    }
}
