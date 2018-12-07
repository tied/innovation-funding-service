package org.innovateuk.ifs.application.forms.sections.yourorganisation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.innovateuk.ifs.application.forms.sections.common.viewmodel.CommonYourFinancesViewModel;
import org.innovateuk.ifs.application.forms.sections.common.viewmodel.CommonYourFinancesViewModelPopulator;
import org.innovateuk.ifs.application.forms.sections.yourorganisation.form.YourOrganisationForm;
import org.innovateuk.ifs.application.forms.sections.yourorganisation.form.YourOrganisationFormPopulator;
import org.innovateuk.ifs.application.forms.sections.yourorganisation.service.YourOrganisationService;
import org.innovateuk.ifs.application.forms.sections.yourorganisation.viewmodel.YourOrganisationViewModel;
import org.innovateuk.ifs.application.forms.sections.yourorganisation.viewmodel.YourOrganisationViewModelPopulator;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.async.annotations.AsyncMethod;
import org.innovateuk.ifs.async.generation.AsyncAdaptor;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.APPLICATION_BASE_URL;

/**
 * The Controller for the "Your organisation" page in the Application Form process.
 */
@Controller
@RequestMapping(APPLICATION_BASE_URL + "{applicationId}/form/your-organisation/competition/{competitionId}/organisation/{organisationId}/section/{sectionId}")
public class YourOrganisationController extends AsyncAdaptor {

    private static final String VIEW_PAGE = "application/sections/your-organisation/your-organisation";

    private CommonYourFinancesViewModelPopulator commonFinancesViewModelPopulator;
    private YourOrganisationViewModelPopulator viewModelPopulator;
    private YourOrganisationFormPopulator formPopulator;
    private SectionService sectionService;
    private UserRestService userRestService;
    private YourOrganisationService yourOrganisationService;

    @Autowired
    YourOrganisationController(
            CommonYourFinancesViewModelPopulator commonFinancesViewModelPopulator,
            YourOrganisationViewModelPopulator viewModelPopulator,
            YourOrganisationFormPopulator formPopulator,
            SectionService sectionService,
            UserRestService userRestService, YourOrganisationService yourOrganisationService) {

        this.commonFinancesViewModelPopulator = commonFinancesViewModelPopulator;
        this.viewModelPopulator = viewModelPopulator;
        this.formPopulator = formPopulator;
        this.sectionService = sectionService;
        this.userRestService = userRestService;
        this.yourOrganisationService = yourOrganisationService;
    }

    // for ByteBuddy
    YourOrganisationController() {
    }

    @GetMapping
    @AsyncMethod
    @PreAuthorize("hasAnyAuthority('applicant', 'support', 'innovation_lead', 'ifs_administrator', 'comp_admin', 'project_finance', 'stakeholder')")
    @SecuredBySpring(value = "VIEW_YOUR_ORGANISATION", description = "Applicants and internal users can view the Your organisation page")
    public String viewPage(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("competitionId") long competitionId,
            @PathVariable("organisationId") long organisationId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser,
            Model model) {

        Future<CommonYourFinancesViewModel> commonViewModelRequest = async(() ->
                getCommonFinancesViewModel(applicationId, sectionId, organisationId, loggedInUser.isInternalUser()));

        Future<YourOrganisationViewModel> viewModelRequest = async(() ->
                getViewModel(applicationId, competitionId, organisationId));

        Future<YourOrganisationForm> formRequest = async(() ->
                formPopulator.populate(applicationId, competitionId, organisationId));

        model.addAttribute("commonFinancesModel", commonViewModelRequest);
        model.addAttribute("model", viewModelRequest);
        model.addAttribute("form", formRequest);

        return VIEW_PAGE;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('applicant')")
    @SecuredBySpring(value = "UPDATE_YOUR_ORGANISATION", description = "Applicants can update their organisation funding details")
    public String update(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("competitionId") long competitionId,
            @PathVariable("organisationId") long organisationId,
            UserResource loggedInUser,
            @ModelAttribute YourOrganisationForm form) {

        updateYourOrganisation(applicationId, competitionId, organisationId, loggedInUser.getId(), form);
        return redirectToYourFinances(applicationId);
    }

    @PostMapping("/auto-save")
    @PreAuthorize("hasAuthority('applicant')")
    @SecuredBySpring(value = "UPDATE_YOUR_ORGANISATION", description = "Applicants can update their organisation funding details")
    public @ResponseBody JsonNode autosave(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("competitionId") long competitionId,
            @PathVariable("organisationId") long organisationId,
            UserResource loggedInUser,
            @ModelAttribute YourOrganisationForm form) {

        update(applicationId, competitionId, organisationId, loggedInUser, form);
        return new ObjectMapper().createObjectNode();
    }

    @PostMapping(params = "mark-as-complete")
    @PreAuthorize("hasAuthority('applicant')")
    @SecuredBySpring(value = "MARK_YOUR_ORGANISATION_AS_COMPLETE", description = "Applicants can mark their organisation funding details as complete")
    public String markAsComplete(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("organisationId") long organisationId,
            @PathVariable("competitionId") long competitionId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser,
            @Valid @ModelAttribute("form") YourOrganisationForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model) {

        Supplier<String> failureHandler = () -> {
            CommonYourFinancesViewModel commonViewModel = getCommonFinancesViewModel(applicationId, sectionId, organisationId, false);
            YourOrganisationViewModel viewModel = getViewModel(applicationId, competitionId, organisationId);
            model.addAttribute("commonFinancesModel", commonViewModel);
            model.addAttribute("model", viewModel);
            model.addAttribute("form", form);
            return VIEW_PAGE;
        };

        Supplier<String> successHandler = () -> {

            updateYourOrganisation(applicationId, competitionId, organisationId, loggedInUser.getId(), form);

            ProcessRoleResource processRole = userRestService.findProcessRole(loggedInUser.getId(), applicationId).getSuccess();
            List<ValidationMessages> validationMessages = sectionService.markAsComplete(sectionId, applicationId, processRole.getId());
            validationMessages.forEach(validationHandler::addAnyErrors);

            return validationHandler.failNowOrSucceedWith(failureHandler, () -> redirectToYourFinances(applicationId));
        };

        return validationHandler.
                addAnyErrors(validateYourOrganisation(form)).
                failNowOrSucceedWith(failureHandler, successHandler);
    }

    @PostMapping(params = "mark-as-incomplete")
    @PreAuthorize("hasAuthority('applicant')")
    @SecuredBySpring(value = "MARK_YOUR_ORGANISATION_AS_INCOMPLETE", description = "Applicants can mark their organisation funding details as incomplete")
    public String markAsIncomplete(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("competitionId") long competitionId,
            @PathVariable("organisationId") long organisationId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser) {

        ProcessRoleResource processRole = userRestService.findProcessRole(loggedInUser.getId(), applicationId).getSuccess();
        sectionService.markAsInComplete(sectionId, applicationId, processRole.getId());
        return redirectToViewPage(applicationId, competitionId, organisationId, sectionId);
    }

    private void updateYourOrganisation(long applicationId,
                                        long competitionId,
                                        long organisationId,
                                        long userId,
                                        YourOrganisationForm form) {

        boolean growthTableIncluded = yourOrganisationService.isIncludingGrowthTable(competitionId).getSuccess();
        boolean stateAidIncluded = yourOrganisationService.isShowStateAidAgreement(applicationId, organisationId).getSuccess();

        yourOrganisationService.updateOrganisationSize(applicationId, organisationId, form.getOrganisationSize()).getSuccess();

        if (growthTableIncluded) {
            yourOrganisationService.updateFinancialYearEnd(applicationId, competitionId, userId, form.getFinancialYearEnd()).getSuccess();

        } else {
            yourOrganisationService.updateHeadCount(applicationId, competitionId, userId, form.getHeadCount()).getSuccess();
            yourOrganisationService.updateTurnover(applicationId, competitionId, userId, form.getTurnover()).getSuccess();
        }

        if (stateAidIncluded) {
            yourOrganisationService.updateStateAidAgreed(applicationId, form.getStateAidAgreed()).getSuccess();
        }
    }

    private List<Error> validateYourOrganisation(YourOrganisationForm form) {
        return emptyList();
    }

    private YourOrganisationViewModel getViewModel(long applicationId, long competitionId, long organisationId) {
        return viewModelPopulator.populate(applicationId, competitionId, organisationId);
    }

    private CommonYourFinancesViewModel getCommonFinancesViewModel(long applicationId, long sectionId, long organisationId, boolean internalUser) {
        return commonFinancesViewModelPopulator.populate(organisationId, applicationId, sectionId, internalUser);
    }

    private String redirectToViewPage(long applicationId, long competitionId, long organisationId, long sectionId) {
        return "redirect:" + APPLICATION_BASE_URL +
                String.format("%d/form/your-organisation/competition/%d/organisation/%d/section/%d",
                        applicationId,
                        competitionId,
                        organisationId,
                        sectionId);
    }

    private String redirectToYourFinances(long applicationId) {
        // IFS-4848 - we're constructing this URL in a few places - maybe a NavigationUtil?
        return "redirect:" + String.format("%s%d/form/FINANCE", APPLICATION_BASE_URL, applicationId);
    }
}
