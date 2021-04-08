package org.innovateuk.ifs.application.review.viewmodel;

import org.innovateuk.ifs.analytics.BaseAnalyticsViewModel;
import org.innovateuk.ifs.application.readonly.viewmodel.ApplicationQuestionReadOnlyViewModel;
import org.innovateuk.ifs.application.readonly.viewmodel.ApplicationSectionReadOnlyViewModel;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.readonly.viewmodel.ApplicationReadOnlyViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;

import java.util.Set;

import static org.innovateuk.ifs.question.resource.QuestionSetupType.LOAN_BUSINESS_AND_FINANCIAL_INFORMATION;

public class ReviewAndSubmitViewModel implements BaseAnalyticsViewModel {

    private final ApplicationReadOnlyViewModel applicationReadOnlyViewModel;
    private final ApplicationResource application;
    private final CompetitionResource competition;
    private final boolean applicationReadyForSubmit;
    private final boolean userIsLeadApplicant;

    public ReviewAndSubmitViewModel(ApplicationReadOnlyViewModel applicationReadOnlyViewModel,
                                    ApplicationResource application,
                                    CompetitionResource competition,
                                    boolean applicationReadyForSubmit,
                                    boolean userIsLeadApplicant) {
        this.applicationReadOnlyViewModel = applicationReadOnlyViewModel;
        this.application = application;
        this.competition = competition;
        this.applicationReadyForSubmit = applicationReadyForSubmit;
        this.userIsLeadApplicant = userIsLeadApplicant;
    }

    @Override
    public Long getApplicationId() {
        return application.getId();
    }

    @Override
    public String getCompetitionName() {
        return competition.getName();
    }

    public ApplicationReadOnlyViewModel getApplicationReadOnlyViewModel() {
        return applicationReadOnlyViewModel;
    }

    public ApplicationResource getApplication() {
        return application;
    }

    public CompetitionResource getCompetition() {
        return competition;
    }

    public boolean isApplicationReadyForSubmit() {
        return applicationReadyForSubmit;
    }

    public boolean isUserIsLeadApplicant() {
        return userIsLeadApplicant;
    }

    public boolean hasBusinessAndFinancalInformationQuestion(){
        return getApplicationReadOnlyViewModel()
                .getSections()
                .stream()
                .map(ApplicationSectionReadOnlyViewModel::getQuestions)
                .flatMap(Set::stream)
                .map(ApplicationQuestionReadOnlyViewModel::getQuestionSetupType)
                .anyMatch(questionSetupType -> LOAN_BUSINESS_AND_FINANCIAL_INFORMATION.equals(questionSetupType ));
    }

}
