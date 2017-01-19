package org.innovateuk.ifs.management.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.user.resource.BusinessType;

import java.util.List;

/**
 * Holder of model attributes for an assigned assessor in the Application Progress view.
 */
public class ApplicationAssessmentProgressAssignedRowViewModel extends ApplicationAssessmentProgressRowViewModel {

    private BusinessType businessType;
    private List<String> innovationAreas;
    private boolean notified;
    private boolean accepted;
    private boolean started;
    private boolean submitted;

    public ApplicationAssessmentProgressAssignedRowViewModel(String name, long totalApplicationsCount, long assignedCount, BusinessType businessType, List<String> innovationAreas, boolean notified, boolean accepted, boolean started, boolean submitted) {
        super(name, totalApplicationsCount, assignedCount);
        this.businessType = businessType;
        this.innovationAreas = innovationAreas;
        this.notified = notified;
        this.accepted = accepted;
        this.started = started;
        this.submitted = submitted;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public List<String> getInnovationAreas() {
        return innovationAreas;
    }

    public boolean isNotified() {
        return notified;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationAssessmentProgressAssignedRowViewModel that = (ApplicationAssessmentProgressAssignedRowViewModel) o;

        return new EqualsBuilder()
                .append(notified, that.notified)
                .append(accepted, that.accepted)
                .append(started, that.started)
                .append(submitted, that.submitted)
                .append(businessType, that.businessType)
                .append(innovationAreas, that.innovationAreas)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(businessType)
                .append(innovationAreas)
                .append(notified)
                .append(accepted)
                .append(started)
                .append(submitted)
                .toHashCode();
    }
}
