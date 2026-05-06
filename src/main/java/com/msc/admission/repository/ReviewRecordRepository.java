package com.msc.admission.repository;
//操作 review_records 表
import com.msc.admission.entity.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    List<ReviewRecord> findByApplicationId(Long applicationId);
}