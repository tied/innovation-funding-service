package org.innovateuk.ifs.management.assessment.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.assessment.resource.CompetitionInAssessmentKeyAssessmentStatisticsResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.management.assessmentperiod.form.AssessmentPeriodForm;
import org.innovateuk.ifs.management.competition.setup.milestone.form.MilestonesForm;

import java.util.List;
import java.util.Map;

/**
 * View model for the 'Manage assessments' dashboard page.
 *
 * Contains statistics and links to other pages such as 'Manage applications' and 'Manage assessors'.
 */
public class ManageAssessmentsViewModel {

    private final long competitionId;
    private final String competitionName;
    private final boolean inAssessment;

    private final int totalAssessments;
    private final int assessmentsAwaitingResponse;
    private final int assessmentsAccepted;
    private final int assessmentsStarted;
    private final int assessmentsCompleted;
    private final boolean alwaysOpen;
    private final List<AssessmentPeriodForm> assessmentPeriods;

    public ManageAssessmentsViewModel(CompetitionResource competition,
                                      CompetitionInAssessmentKeyAssessmentStatisticsResource keyStatistics,
                                      List<AssessmentPeriodForm> assessmentPeriods) {
        this.competitionId = competition.getId();
        this.competitionName = competition.getName();
        this.inAssessment = competition.getCompetitionStatus() == CompetitionStatus.IN_ASSESSMENT;

        this.totalAssessments = keyStatistics.getAssignmentCount();
        this.assessmentsAwaitingResponse = keyStatistics.getAssignmentsWaiting();
        this.assessmentsAccepted = keyStatistics.getAssignmentsAccepted();
        this.assessmentsStarted = keyStatistics.getAssessmentsStarted();
        this.assessmentsCompleted = keyStatistics.getAssessmentsSubmitted();
        this.alwaysOpen = competition.getAlwaysOpen();
        this.assessmentPeriods = assessmentPeriods;
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public boolean isInAssessment() {
        return inAssessment;
    }

    public int getTotalAssessments() {
        return totalAssessments;
    }

    public int getAssessmentsAwaitingResponse() {
        return assessmentsAwaitingResponse;
    }

    public int getAssessmentsAccepted() {
        return assessmentsAccepted;
    }

    public int getAssessmentsCompleted() {
        return assessmentsCompleted;
    }

    public int getAssessmentsStarted() {
        return assessmentsStarted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ManageAssessmentsViewModel that = (ManageAssessmentsViewModel) o;

        return new EqualsBuilder()
                .append(competitionId, that.competitionId)
                .append(inAssessment, that.inAssessment)
                .append(totalAssessments, that.totalAssessments)
                .append(assessmentsAwaitingResponse, that.assessmentsAwaitingResponse)
                .append(assessmentsAccepted, that.assessmentsAccepted)
                .append(assessmentsCompleted, that.assessmentsCompleted)
                .append(assessmentsStarted, that.assessmentsStarted)
                .append(competitionName, that.competitionName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(competitionId)
                .append(competitionName)
                .append(inAssessment)
                .append(totalAssessments)
                .append(assessmentsAwaitingResponse)
                .append(assessmentsAccepted)
                .append(assessmentsCompleted)
                .append(assessmentsStarted)
                .toHashCode();
    }

    public boolean isAlwaysOpen() {
        return alwaysOpen;
    }

    public List<AssessmentPeriodForm> getAssessmentPeriods() {
        return assessmentPeriods;
    }
}
