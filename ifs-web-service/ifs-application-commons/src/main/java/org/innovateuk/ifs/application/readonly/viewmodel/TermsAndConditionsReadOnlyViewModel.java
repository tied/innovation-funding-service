package org.innovateuk.ifs.application.readonly.viewmodel;

import org.innovateuk.ifs.application.common.viewmodel.ApplicationTermsPartnerViewModel;
import org.innovateuk.ifs.application.common.viewmodel.ApplicationTermsViewModel;
import org.innovateuk.ifs.application.readonly.ApplicationReadOnlyData;
import org.innovateuk.ifs.form.resource.QuestionResource;

public class TermsAndConditionsReadOnlyViewModel extends AbstractQuestionReadOnlyViewModel {

    private final ApplicationTermsViewModel applicationTermsViewModel;
    private final ApplicationTermsPartnerViewModel applicationTermsPartnerViewModel;

    public TermsAndConditionsReadOnlyViewModel(ApplicationReadOnlyData data,
                                               QuestionResource question,
                                               ApplicationTermsViewModel applicationTermsViewModel,
                                               ApplicationTermsPartnerViewModel applicationTermsPartnerViewModel) {
        super(data, question);
        this.applicationTermsViewModel = applicationTermsViewModel;
        this.applicationTermsPartnerViewModel = applicationTermsPartnerViewModel;
    }

    @Override
    public String getFragment() {
        return "terms-and-conditions";
    }

    @Override
    public boolean shouldDisplayMarkAsComplete() {
        return false;
    }

    @Override
    public boolean shouldDisplayActions() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return applicationTermsViewModel.isTermsAcceptedByAllOrganisations();
    }

    public ApplicationTermsPartnerViewModel getApplicationTermsPartnerViewModel() {
        return applicationTermsPartnerViewModel;
    }
}