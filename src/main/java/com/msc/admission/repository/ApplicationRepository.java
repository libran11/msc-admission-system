package com.msc.admission.repository;
//操作 applications 表
import com.msc.admission.entity.Application;
import com.msc.admission.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByApplicantId(Long applicantId);

    List<Application> findByStatus(ApplicationStatus status);
}
