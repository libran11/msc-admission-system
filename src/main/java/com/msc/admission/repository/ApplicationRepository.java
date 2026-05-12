package com.msc.admission.repository;

import com.msc.admission.entity.Application;
import com.msc.admission.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    List<Application> findByApplicantId(Long applicantId);

    List<Application> findByStatus(ApplicationStatus status);

    // Count applications grouped by status for analytics
    @Query("SELECT a.status AS status, COUNT(a) AS count FROM Application a GROUP BY a.status")
    List<StatusCount> countByStatus();

    // Count applications grouped by program name, ordered by popularity
    @Query("SELECT a.programName AS programName, COUNT(a) AS applicationCount FROM Application a GROUP BY a.programName ORDER BY COUNT(a) DESC")
    List<ProgramPopularity> countByProgramName();

    // Count tags across applications and order by how many times each tag appears
    @Query("SELECT t AS tag, COUNT(t) AS tagCount FROM Application a JOIN a.tags t GROUP BY t ORDER BY COUNT(t) DESC")
    List<TagPopularity> countByTags();

    // Count programs grouped by status, used for recommendation based on accepted programs
    @Query("SELECT a.programName AS programName, COUNT(a) AS applicationCount FROM Application a WHERE a.status = :status GROUP BY a.programName ORDER BY COUNT(a) DESC")
    List<ProgramPopularity> countByProgramNameAndStatus(@Param("status") ApplicationStatus status);

    interface StatusCount {
        ApplicationStatus getStatus();

        Long getCount();
    }

    interface ProgramPopularity {
        String getProgramName();

        Long getApplicationCount();
    }

    interface TagPopularity {
        String getTag();

        Long getTagCount();
    }
}
