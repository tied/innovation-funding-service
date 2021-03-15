package org.innovateuk.ifs.project.yourorganisation.viewmodel;

import org.innovateuk.ifs.application.forms.sections.yourorganisation.viewmodel.ApplicationYourOrganisationViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.Authority;
import org.innovateuk.ifs.user.resource.UserResource;

import java.util.Optional;

public class ProjectYourOrganisationViewModel extends ApplicationYourOrganisationViewModel {
    private final long projectId;
    private final String projectName;
    private final long organisationId;
    private final boolean readOnly;
    private final UserResource loggedInUser;
    private final boolean isAllEligibilityAndViabilityInReview;
    private final boolean subsidyBasisRequiredAndNotCompleted;
    private final Optional<Long> subsidyBasisQuestionId;

    public ProjectYourOrganisationViewModel(long applicationId,
                                            CompetitionResource competition,
                                            OrganisationResource organisation,
                                            boolean maximumFundingLevelConstant,
                                            boolean showOrganisationSizeAlert,
                                            long projectId,
                                            String projectName,
                                            boolean readOnly,
                                            UserResource loggedInUser,
                                            boolean isAllEligibilityAndViabilityInReview,
                                            boolean subsidyBasisRequiredAndNotCompleted,
                                            Optional<Long> subsidyBasisQuestionId) {
        super(applicationId, competition, organisation.getOrganisationTypeEnum(), maximumFundingLevelConstant, showOrganisationSizeAlert, false);
        this.projectId = projectId;
        this.projectName = projectName;
        this.organisationId = organisation.getId();
        this.readOnly = readOnly;
        this.loggedInUser = loggedInUser;
        this.isAllEligibilityAndViabilityInReview = isAllEligibilityAndViabilityInReview;
        this.subsidyBasisRequiredAndNotCompleted = subsidyBasisRequiredAndNotCompleted;
        this.subsidyBasisQuestionId = subsidyBasisQuestionId;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public long getOrganisationId() {
        return organisationId;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public UserResource getLoggedInUser() {
        return loggedInUser;
    }

    public boolean isAllowedToEditOrganisationSize() {
        return getLoggedInUser().hasAuthority(Authority.PROJECT_FINANCE) && isAllEligibilityAndViabilityInReview;
    }

    public boolean isSubsidyBasisRequiredAndNotCompleted() {
        return subsidyBasisRequiredAndNotCompleted;
    }

    public boolean isOrganisationSectionLocked() {
        return subsidyBasisRequiredAndNotCompleted;
    }

    public Long getSubsidyBasisQuestionId() {
        return subsidyBasisQuestionId.orElse(null);
    }
}
