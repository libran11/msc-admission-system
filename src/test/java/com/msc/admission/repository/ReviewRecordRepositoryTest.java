package com.msc.admission.repository;

import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ReviewDecision;
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
class ReviewRecordRepositoryTest {

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @BeforeEach
    void setUp() {
        reviewRecordRepository.deleteAll();
        reviewRecordRepository.save(new ReviewRecord(1L, 201L, ReviewDecision.APPROVE, "Good"));
        reviewRecordRepository.save(new ReviewRecord(1L, 202L, ReviewDecision.REQUEST_MORE_DOCUMENTS, "Need docs"));
        reviewRecordRepository.save(new ReviewRecord(2L, 201L, ReviewDecision.REJECT, "Incomplete"));
    }

    @Test
    @DisplayName("find by application ID returns matching review records")
    void findByApplicationId_returnsMatchingRecords() {
        List<ReviewRecord> result = reviewRecordRepository.findByApplicationId(1L);
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getApplicationId().equals(1L));
    }
}
