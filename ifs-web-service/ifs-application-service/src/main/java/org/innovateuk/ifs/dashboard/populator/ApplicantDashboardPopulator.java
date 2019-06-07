package org.innovateuk.ifs.dashboard.populator;

import org.innovateuk.ifs.applicant.resource.dashboard.ApplicantDashboardResource;
import org.innovateuk.ifs.applicant.resource.dashboard.DashboardApplicationForEuGrantTransferResource;
import org.innovateuk.ifs.applicant.resource.dashboard.DashboardApplicationInProgressResource;
import org.innovateuk.ifs.applicant.resource.dashboard.DashboardApplicationInSetupResource;
import org.innovateuk.ifs.applicant.resource.dashboard.DashboardPreviousApplicationResource;
import org.innovateuk.ifs.applicant.service.ApplicantRestService;
import org.innovateuk.ifs.dashboard.viewmodel.ApplicantDashboardViewModel;
import org.innovateuk.ifs.dashboard.viewmodel.EuGrantTransferDashboardRowViewModel;
import org.innovateuk.ifs.dashboard.viewmodel.InProgressDashboardRowViewModel;
import org.innovateuk.ifs.dashboard.viewmodel.InSetupDashboardRowViewModel;
import org.innovateuk.ifs.dashboard.viewmodel.PreviousDashboardRowViewModel;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.dashboard.populator.ApplicationDashboardUtils.toEuGrantTransferViewModel;
import static org.innovateuk.ifs.dashboard.populator.ApplicationDashboardUtils.toInProgressViewModel;
import static org.innovateuk.ifs.dashboard.populator.ApplicationDashboardUtils.toInSetupViewModel;
import static org.innovateuk.ifs.dashboard.populator.ApplicationDashboardUtils.toPreviousViewModel;

/**
 * Populator for the applicant dashboard, it populates an {@link org.innovateuk.ifs.dashboard.viewmodel.ApplicantDashboardViewModel}
 */
@Service
public class ApplicantDashboardPopulator {

    private ApplicantRestService applicantRestService;

    public ApplicantDashboardPopulator(ApplicantRestService applicantRestService) {
        this.applicantRestService = applicantRestService;
    }

    public ApplicantDashboardViewModel populate(Long userId, String originQuery) {
        ApplicantDashboardResource applicantDashboardResource = applicantRestService.getApplicantDashboard(userId);
        return getApplicantDashboardViewModel(originQuery, applicantDashboardResource);
    }

    private ApplicantDashboardViewModel getApplicantDashboardViewModel(String originQuery, ApplicantDashboardResource applicantDashboardResource) {
        List<InSetupDashboardRowViewModel> applicationsInSetUp = getViewModelForInSetup(applicantDashboardResource.getInSetup());
        List<EuGrantTransferDashboardRowViewModel> applicationsForEuGrantTransfers = getViewModelForEuGrantTransfers(applicantDashboardResource.getEuGrantTransfer());
        List<InProgressDashboardRowViewModel> applicationsInProgress = getViewModelForInProgress(applicantDashboardResource.getInProgress());
        List<PreviousDashboardRowViewModel> applicationsPreviouslySubmitted = getViewModelForPrevious(applicantDashboardResource.getPrevious());

        return new ApplicantDashboardViewModel(applicationsInSetUp, applicationsForEuGrantTransfers, applicationsInProgress, applicationsPreviouslySubmitted, originQuery);
    }

    private List<InSetupDashboardRowViewModel> getViewModelForInSetup(List<DashboardApplicationInSetupResource> inSetupResources){
        return inSetupResources
                .stream()
                .map(toInSetupViewModel())
                .sorted(comparing(InSetupDashboardRowViewModel::getTargetStartDate, nullsLast(reverseOrder())))
                .collect(toList());
    }

    private List<EuGrantTransferDashboardRowViewModel> getViewModelForEuGrantTransfers(List<DashboardApplicationForEuGrantTransferResource> euGrantTransferResources){
        return euGrantTransferResources
                .stream()
                .map(toEuGrantTransferViewModel())
                .sorted()
                .collect(toList());
    }

    private List<InProgressDashboardRowViewModel> getViewModelForInProgress(List<DashboardApplicationInProgressResource> dashboardApplicationInProgressResources){
        return dashboardApplicationInProgressResources
                .stream()
                .map(toInProgressViewModel())
                .sorted(comparing(InProgressDashboardRowViewModel::getEndDate, nullsLast(naturalOrder())).thenComparing(InProgressDashboardRowViewModel::getStartDate, nullsLast(reverseOrder())))
                .collect(toList());
    }

    private List<PreviousDashboardRowViewModel> getViewModelForPrevious(List<DashboardPreviousApplicationResource> applicantDashboardResource){
        return applicantDashboardResource
                .stream()
                .map(toPreviousViewModel())
                .sorted(comparing(PreviousDashboardRowViewModel::getStartDate, nullsLast(reverseOrder())))
                .collect(toList());
    }

}