package org.innovateuk.ifs.cofunder.dashboard.viewmodel;

import org.innovateuk.ifs.cofunder.resource.CofunderDashboardApplicationPageResource;
import org.innovateuk.ifs.cofunder.resource.CofunderDashboardApplicationResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.pagination.PaginationViewModel;

import java.time.ZonedDateTime;
import java.util.List;

import static org.innovateuk.ifs.util.TimeZoneUtil.toUkTimeZone;

public class CofunderCompetitionDashboardViewModel {

    private final long competitionId;
    private final String competitionName;
    private final ZonedDateTime deadline;
    private final List<CofunderDashboardApplicationResource> applications;
    private final boolean assessmentClosed;
    private final PaginationViewModel pagination;

    public CofunderCompetitionDashboardViewModel(CofunderDashboardApplicationPageResource pageResource, CompetitionResource competition) {
        this.competitionId = competition.getId();
        this.competitionName = competition.getName();
        this.deadline = toUkTimeZone(competition.getAssessorDeadlineDate());
        this.applications = pageResource.getContent();
        this.assessmentClosed = competition.getCompetitionStatus().isLaterThan(CompetitionStatus.IN_ASSESSMENT);
        this.pagination = new PaginationViewModel(pageResource);
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public ZonedDateTime getDeadline() {
        return deadline;
    }

    public List<CofunderDashboardApplicationResource> getApplications() {
        return applications;
    }

    public boolean isAssessmentClosed() {
        return assessmentClosed;
    }

    public PaginationViewModel getPagination() {
        return pagination;
    }
}
