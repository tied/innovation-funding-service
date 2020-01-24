package org.innovateuk.ifs.project.financechecks.controller;

import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.file.controller.FileDownloadControllerUtils;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.financecheck.FinanceCheckService;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckSummaryResource;
import org.innovateuk.ifs.project.financechecks.form.FinanceCheckSummaryForm;
import org.innovateuk.ifs.project.financechecks.viewmodel.ProjectFinanceCheckSummaryViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.spendprofile.SpendProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * This controller is for allowing internal users to view and update application finances entered by applicants.
 */
@Controller
@RequestMapping("/project/{projectId}/finance-check")
public class FinanceCheckController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpendProfileService spendProfileService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private FinanceCheckService financeCheckService;
//
//    @Autowired
//    private ProjectYourOrganisationRestService projectYourOrganisationRestService;
//
//    @Autowired
//    private CompetitionRestService competitionRestService;

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_FINANCE_CHECKS_SECTION')")
    @GetMapping
    public String viewFinanceCheckSummary(@PathVariable Long projectId, Model model,
                                          @ModelAttribute(binding = false, value = "form") FinanceCheckSummaryForm form) {
        return doViewFinanceCheckSummary(projectId, model);
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_FINANCE_CHECKS_SECTION')")
    @PostMapping
    public String generateSpendProfile(@PathVariable Long projectId, Model model,
                                       @ModelAttribute("form") FinanceCheckSummaryForm form,
                                       @SuppressWarnings("unused") BindingResult bindingResult,
                                       ValidationHandler validationHandler) {

        Supplier<String> failureView = () -> doViewFinanceCheckSummary(projectId, model);
        ServiceResult<Void> generateResult = spendProfileService.generateSpendProfile(projectId);

        return validationHandler.addAnyErrors(generateResult).failNowOrSucceedWith(failureView, () ->
                redirectToViewFinanceCheckSummary(projectId)
        );
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_FINANCE_CHECKS_SECTION')")
    @GetMapping("/organisation/{organisationId}/jes-file")
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadJesFile(@PathVariable("projectId") final Long projectId,
                                                                           @PathVariable("organisationId") Long organisationId) {

        ProjectResource project = projectService.getById(projectId);
        ApplicationResource application = applicationService.getById(project.getApplication());

        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceByApplicationIdAndOrganisationId(application.getId(), organisationId);

        if (applicationFinanceResource.getFinanceFileEntry() != null) {
            FileEntryResource jesFileEntryResource = financeService.getFinanceEntry(applicationFinanceResource.getFinanceFileEntry()).getSuccess();
            ByteArrayResource jesByteArrayResource = financeService.getFinanceDocumentByApplicationFinance(applicationFinanceResource.getId()).getSuccess();
            return FileDownloadControllerUtils.getFileResponseEntity(jesByteArrayResource, jesFileEntryResource);
        }

        return new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT);

    }

    private String doViewFinanceCheckSummary(Long projectId, Model model) {
        FinanceCheckSummaryResource financeCheckSummaryResource = financeCheckService.getFinanceCheckSummary(projectId).getSuccess();
        ProjectResource project = projectService.getById(projectId);
        model.addAttribute("model", new ProjectFinanceCheckSummaryViewModel(financeCheckSummaryResource, project.getProjectState().isActive(), project.isCollaborativeProject()));
        return "project/financecheck/summary";
    }

//    private boolean updateFundingLevelPercentage(long projectId, long organisationId) {
//
//        OrganisationFinancesWithGrowthTableResource finances = new OrganisationFinancesWithGrowthTableResource();
//        OrganisationFinancesWithoutGrowthTableResource finances1 = new OrganisationFinancesWithoutGrowthTableResource();
//
//        if (isIncludingGrowthTable(projectId)) {
//            return projectYourOrganisationRestService.updateOrganisationFinancesWithGrowthTable(projectId, organisationId, finances).isSuccess();
//        }
//
//        return projectYourOrganisationRestService.updateOrganisationFinancesWithoutGrowthTable(projectId, organisationId, finances1).isSuccess();
//
//    }
//
//    private boolean isIncludingGrowthTable(long projectId) {
//        long competitionId = projectService.getById(projectId).getCompetition();
//        return competitionRestService.getCompetitionById(competitionId).
//                andOnSuccessReturn(competition -> TRUE.equals(competition.getIncludeProjectGrowthTable())).getSuccess();
//    }

    private String redirectToViewFinanceCheckSummary(Long projectId) {
        return "redirect:/project/" + projectId + "/finance-check";
    }

}