package org.innovateuk.ifs.application.finance.populator;

import org.innovateuk.ifs.application.finance.viewmodel.ApplicationProcurementMilestonesSummaryViewModel;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.procurement.milestone.resource.ApplicationProcurementMilestoneResource;
import org.innovateuk.ifs.procurement.milestone.service.ApplicationProcurementMilestoneRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationProcurementMilestoneSummaryViewModelPopulator {

    @Autowired
    private ApplicationProcurementMilestoneRestService applicationProcurementMilestoneRestService;

    public ApplicationProcurementMilestonesSummaryViewModel populate(ApplicationResource application) {
        return viewModel(application);
    }

    private ApplicationProcurementMilestonesSummaryViewModel viewModel(ApplicationResource application) {
        List<ApplicationProcurementMilestoneResource> applicationProcurementMilestoneResources = applicationProcurementMilestoneRestService.getByApplicationIdAndOrganisationId(application.getId(), application.getLeadOrganisationId()).getSuccess();

        return new ApplicationProcurementMilestonesSummaryViewModel(applicationProcurementMilestoneResources);
    }
}