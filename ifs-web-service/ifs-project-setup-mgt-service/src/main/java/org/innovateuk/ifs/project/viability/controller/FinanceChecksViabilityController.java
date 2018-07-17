package org.innovateuk.ifs.project.viability.controller;

import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.finance.resource.OrganisationSizeResource;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.service.OrganisationDetailsRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.finance.ProjectFinanceService;
import org.innovateuk.ifs.project.finance.resource.Viability;
import org.innovateuk.ifs.project.finance.resource.ViabilityRagStatus;
import org.innovateuk.ifs.project.finance.resource.ViabilityResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.viability.form.FinanceChecksViabilityForm;
import org.innovateuk.ifs.project.viability.viewmodel.FinanceChecksViabilityViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.VIABILITY_CHECKS_NOT_APPLICABLE;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * This controller serves the Viability page where internal users can confirm the viability of a partner organisation's
 * financial position on a Project
 */
@Controller
@PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = FinanceChecksViabilityController.class)
@RequestMapping("/project/{projectId}/finance-check/organisation/{organisationId}/viability")
public class FinanceChecksViabilityController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationDetailsRestService organisationDetailsService;

    @Autowired
    private ProjectFinanceService financeService;

    @GetMapping
    public String viewViability(@PathVariable("projectId") Long projectId,
                                @PathVariable("organisationId") Long organisationId, Model model) {

        return doViewViability(projectId, organisationId, model, getViabilityForm(projectId, organisationId));
    }

    @PostMapping(params = "save-and-continue")
    public String saveAndContinue(@PathVariable("projectId") Long projectId,
                                  @PathVariable("organisationId") Long organisationId,
                                  @ModelAttribute("form") FinanceChecksViabilityForm form,
                                  @SuppressWarnings("unused") BindingResult bindingResult,
                                  ValidationHandler validationHandler,
                                  Model model) {

        Supplier<String> successView = () -> "redirect:/project/" + projectId + "/finance-check";

        return doSaveViability(projectId, organisationId, Viability.REVIEW, form, validationHandler, model, successView);
    }

    @PostMapping(params = "confirm-viability")
    public String confirmViability(@PathVariable("projectId") Long projectId,
                                   @PathVariable("organisationId") Long organisationId,
                                   @ModelAttribute("form") FinanceChecksViabilityForm form,
                                   @SuppressWarnings("unused") BindingResult bindingResult,
                                   ValidationHandler validationHandler,
                                   Model model) {

        Supplier<String> successView = () ->
                "redirect:/project/" + projectId + "/finance-check/organisation/" + organisationId + "/viability";

        return doSaveViability(projectId, organisationId, Viability.APPROVED, form, validationHandler, model, successView);
    }

    private String doSaveViability(Long projectId, Long organisationId, Viability viability, FinanceChecksViabilityForm form,
                                   ValidationHandler validationHandler, Model model, Supplier<String> successView) {

        Supplier<String> failureView = () -> doViewViability(projectId, organisationId, model, form);

        ServiceResult<Void> saveCreditReportResult = financeService.saveCreditReportConfirmed(projectId, organisationId, form.isCreditReportConfirmed());

        return validationHandler.
               addAnyErrors(saveCreditReportResult).
               failNowOrSucceedWith(failureView, () -> {

            ViabilityRagStatus statusToSend = getRagStatusDependantOnConfirmationCheckboxSelection(form);

            ServiceResult<Void> saveViabilityResult = financeService.saveViability(projectId, organisationId, viability, statusToSend);

            return validationHandler.
                   addAnyErrors(saveViabilityResult).
                   failNowOrSucceedWith(failureView, successView);
        });
    }

    private ViabilityRagStatus getRagStatusDependantOnConfirmationCheckboxSelection(FinanceChecksViabilityForm form) {
        ViabilityRagStatus statusToSend;

        if (form.isConfirmViabilityChecked()) {
            statusToSend = form.getRagStatus();
        } else {
            statusToSend = ViabilityRagStatus.UNSET;
        }
        return statusToSend;
    }

    private String doViewViability(Long projectId, Long organisationId, Long applicationId, Model model, FinanceChecksViabilityForm form) {
        model.addAttribute("model", getViewModel(projectId, organisationId, applicationId));
        model.addAttribute("form", form);
        return "project/financecheck/viability";
    }

    private FinanceChecksViabilityViewModel getViewModel(Long projectId, Long organisationId, Long applicationId) {

        ViabilityResource viability = financeService.getViability(projectId, organisationId);
        OrganisationResource organisation = organisationService.getOrganisationById(organisationId);
        ProjectResource application = projectService.getByApplicationId(applicationId);

        if(viability.getViability().isNotApplicable()){
            throw new ObjectNotFoundException(VIABILITY_CHECKS_NOT_APPLICABLE.getErrorKey(), singletonList(organisation.getName()));
        }

        boolean viabilityConfirmed = viability.getViability() == Viability.APPROVED;

        OrganisationResource leadOrganisation = projectService.getLeadOrganisation(projectId);
        List<ProjectFinanceResource> projectFinances = financeService.getProjectFinances(projectId);
        ProjectFinanceResource financesForOrganisation = simpleFindFirst(projectFinances,
                finance -> finance.getOrganisation().equals(organisationId)).get();

        String organisationName = organisation.getName();
        boolean leadPartnerOrganisation = leadOrganisation.getId().equals(organisation.getId());

        Integer totalCosts = toZeroScaleInt(financesForOrganisation.getTotal());
        Integer percentageGrant = financesForOrganisation.getGrantClaimPercentage();
        Integer fundingSought = toZeroScaleInt(financesForOrganisation.getTotalFundingSought());
        Integer otherPublicSectorFunding = toZeroScaleInt(financesForOrganisation.getTotalOtherFunding());
        Integer contributionToProject = toZeroScaleInt(financesForOrganisation.getTotalContribution());

        String companyRegistrationNumber = organisation.getCompanyHouseNumber();

        Long headCount = null;
        RestResult<Long> headCountResult = organisationDetailsService.getHeadCount(projectService.getById(projectId).getApplication(), organisationId);
        if (headCountResult.isSuccess()) {
            headCount = headCountResult.getSuccess();
        }
        Long turnover = null;
        RestResult<Long> turnOverResult = organisationDetailsService.getTurnover(projectService.getById(projectId).getApplication(), organisationId);
        if (turnOverResult.isSuccess()) {
            turnover = turnOverResult.getSuccess();
        }

        String approver = viability.getViabilityApprovalUserFirstName() + " " + viability.getViabilityApprovalUserLastName();
        LocalDate approvalDate = viability.getViabilityApprovalDate();

        List<OrganisationSizeResource> sizes = organisationDetailsService.getOrganisationSizes().getSuccess();
        Optional<OrganisationSizeResource> organisationSizeResource = sizes.stream().filter(size -> size.getId().equals(financesForOrganisation.getOrganisationSize())).findAny();
        String organisationSizeDescription = organisationSizeResource.map(OrganisationSizeResource::getDescription).orElse(null);
        return new FinanceChecksViabilityViewModel(organisationName, leadPartnerOrganisation,
                totalCosts, percentageGrant, fundingSought, otherPublicSectorFunding, contributionToProject,
                companyRegistrationNumber, turnover, headCount, projectId, viabilityConfirmed,
                viabilityConfirmed, approver, approvalDate, organisationId,
                organisationSizeDescription, applicationId);
    }

    private FinanceChecksViabilityForm getViabilityForm(Long projectId, Long organisationId) {

        ViabilityResource viability = financeService.getViability(projectId, organisationId);
        boolean creditReportConfirmed = financeService.isCreditReportConfirmed(projectId, organisationId);
        boolean confirmViabilityChecked = viability.getViabilityRagStatus() != ViabilityRagStatus.UNSET;

        return new FinanceChecksViabilityForm(creditReportConfirmed, viability.getViabilityRagStatus(), confirmViabilityChecked);
    }

    private int toZeroScaleInt(BigDecimal value) {
        return value.setScale(0, HALF_EVEN).intValueExact();
    }
}
