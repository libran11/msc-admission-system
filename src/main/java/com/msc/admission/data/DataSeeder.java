package com.msc.admission.data;

import com.msc.admission.entity.Application;
import com.msc.admission.entity.ReviewRecord;
import com.msc.admission.enums.ApplicationStatus;
import com.msc.admission.enums.ReviewDecision;
import com.msc.admission.repository.ApplicationRepository;
import com.msc.admission.repository.ReviewRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;
    private final ReviewRecordRepository reviewRecordRepository;

    private static final List<MscProgram> PROGRAMS = List.of(
            new MscProgram("MSc in Computer Science"),
            new MscProgram("MSc in Data Science"),
            new MscProgram("MSc in Artificial Intelligence"),
            new MscProgram("MSc in Business Analytics"),
            new MscProgram("MSc in Software Engineering"),
            new MscProgram("MSc in Cybersecurity"),
            new MscProgram("MSc in Financial Technology"),
            new MscProgram("MSc in Information Systems"),
            new MscProgram("MSc in Machine Learning"),
            new MscProgram("MSc in Cloud Computing"),
            new MscProgram("MSc in Human-Computer Interaction"),
            new MscProgram("MSc in Robotics")
    );

    private static final List<Long> APPLICANT_IDS = List.of(101L, 102L, 103L, 104L, 105L,
            106L, 107L, 108L, 109L, 110L);
    private static final List<Long> REVIEWER_IDS = List.of(201L, 202L, 203L);

    public DataSeeder(ApplicationRepository applicationRepository,
                      ReviewRecordRepository reviewRecordRepository) {
        this.applicationRepository = applicationRepository;
        this.reviewRecordRepository = reviewRecordRepository;
    }

    @Override
    public void run(String... args) {
        if (applicationRepository.count() > 0) {
            return;
        }

        Random random = new Random(42);
        List<Application> allApplications = new ArrayList<>();

        for (int i = 0; i < 80; i++) {
            MscProgram program = PROGRAMS.get(i % PROGRAMS.size());
            Long applicantId = APPLICANT_IDS.get(i % APPLICANT_IDS.size());

            Application app = new Application(
                    applicantId,
                    program.name(),
                    "Personal statement for " + program.name() + " by applicant " + applicantId,
                    "https://docs.example.com/applications/" + (i + 1) + ".pdf"
            );

            LocalDateTime created = LocalDateTime.now().minusDays(90 - i);
            app.setCreatedAt(created);
            app.setUpdatedAt(created);

            ApplicationStatus status = assignStatus(i);
            app.setStatus(status);

            allApplications.add(app);
        }

        List<Application> saved = applicationRepository.saveAll(allApplications);

        List<ReviewRecord> reviewRecords = new ArrayList<>();
        for (Application app : saved) {
            if (app.getStatus() != ApplicationStatus.DRAFT
                    && app.getStatus() != ApplicationStatus.SUBMITTED
                    && app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
                ReviewDecision decision = mapStatusToDecision(app.getStatus());
                Long reviewerId = REVIEWER_IDS.get(random.nextInt(REVIEWER_IDS.size()));
                ReviewRecord record = new ReviewRecord(
                        app.getId(),
                        reviewerId,
                        decision,
                        "Review comment for application " + app.getId()
                );
                record.setReviewedAt(app.getUpdatedAt().plusHours(random.nextInt(48) + 1));
                reviewRecords.add(record);

                if (app.getStatus() == ApplicationStatus.NEED_MORE_DOCUMENTS && random.nextBoolean()) {
                    ReviewDecision secondDecision = random.nextBoolean()
                            ? ReviewDecision.APPROVE : ReviewDecision.REJECT;
                    ReviewRecord secondRecord = new ReviewRecord(
                            app.getId(),
                            REVIEWER_IDS.get(random.nextInt(REVIEWER_IDS.size())),
                            secondDecision,
                            "Follow-up review for application " + app.getId()
                    );
                    secondRecord.setReviewedAt(app.getUpdatedAt().plusDays(7));
                    reviewRecords.add(secondRecord);
                }
            }
        }

        reviewRecordRepository.saveAll(reviewRecords);
    }

    private ApplicationStatus assignStatus(int index) {
        if (index < 10) return ApplicationStatus.DRAFT;
        if (index < 25) return ApplicationStatus.SUBMITTED;
        if (index < 40) return ApplicationStatus.UNDER_REVIEW;
        if (index < 50) return ApplicationStatus.NEED_MORE_DOCUMENTS;
        if (index < 65) return ApplicationStatus.ACCEPTED;
        return ApplicationStatus.REJECTED;
    }

    private ReviewDecision mapStatusToDecision(ApplicationStatus status) {
        return switch (status) {
            case ACCEPTED -> ReviewDecision.APPROVE;
            case REJECTED -> ReviewDecision.REJECT;
            case NEED_MORE_DOCUMENTS -> ReviewDecision.REQUEST_MORE_DOCUMENTS;
            default -> throw new IllegalStateException("Unexpected reviewed status: " + status);
        };
    }
}
