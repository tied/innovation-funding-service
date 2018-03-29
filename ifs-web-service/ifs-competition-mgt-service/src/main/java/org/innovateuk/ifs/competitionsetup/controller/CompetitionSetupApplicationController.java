
package org.innovateuk.ifs.competitionsetup.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.QuestionSetupRestService;
import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.service.CompetitionSetupRestService;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.GuidanceRowForm;
import org.innovateuk.ifs.competitionsetup.form.LandingPageForm;
import org.innovateuk.ifs.competitionsetup.form.application.*;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupQuestionService;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupService;
import org.innovateuk.ifs.competitionsetup.service.populator.CompetitionSetupPopulator;
import org.innovateuk.ifs.competitionsetup.viewmodel.CompetitionSetupSubsectionViewModel;
import org.innovateuk.ifs.competitionsetup.viewmodel.QuestionSetupViewModel;
import org.innovateuk.ifs.competitionsetup.viewmodel.fragments.GeneralSetupViewModel;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupSection.APPLICATION_FORM;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection.*;
import static org.innovateuk.ifs.competitionsetup.controller.CompetitionSetupController.COMPETITION_ID_KEY;
import static org.innovateuk.ifs.competitionsetup.controller.CompetitionSetupController.COMPETITION_SETUP_FORM_KEY;

/**
 * Controller to manage the Application Questions and it's sub-sections in the
 * competition setup process
 */
@Controller
@RequestMapping("/competition/setup/{competitionId}/section/application")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = CompetitionSetupApplicationController.class)
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class CompetitionSetupApplicationController {

    private static final Log LOG = LogFactory.getLog(CompetitionSetupApplicationController.class);
    public static final String APPLICATION_LANDING_REDIRECT = "redirect:/competition/setup/%d/section/application/landing-page";
    private static final String questionView = "competition/setup/question";
    private static final String MODEL = "model";
    private static final String ASSESSED_QUESTIONS_SECTION_NAME = "Application questions";
    private static final String PROJECT_DETAILS_SECTION_NAME = "Project details";

    @Autowired
    private CompetitionSetupService competitionSetupService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private CompetitionSetupRestService competitionSetupRestService;

    @Autowired
    private CompetitionSetupQuestionService competitionSetupQuestionService;

    @Autowired
    private QuestionSetupRestService questionSetupRestService;

    @Autowired
    private CompetitionSetupPopulator competitionSetupPopulator;

    @Autowired
    @Qualifier("mvcValidator")
    private Validator validator;

    @PostMapping(value = "/landing-page", params = "createQuestion")
    public String createQuestion(@PathVariable(COMPETITION_ID_KEY) long competitionId) {
        ServiceResult<CompetitionSetupQuestionResource> restResult = competitionSetupQuestionService.createDefaultQuestion(competitionId);

        Function<CompetitionSetupQuestionResource, String> successViewFunction =
                (question) -> String.format("redirect:/competition/setup/%d/section/application/question/%d/edit", competitionId, question.getQuestionId());
        Supplier<String> successView = () -> successViewFunction.apply(restResult.getSuccess());

        return successView.get();
    }

    @ZeroDowntime(reference = "IFS-2832", description = "Changed the endpoint. TODO: This endpoint needs to be removed")
    @PostMapping(value = "/landing-page", params = "deleteQuestion")
    public String deleteQuestion(@ModelAttribute("deleteQuestion") DeleteQuestionForm deleteQuestionForm,
                                         @PathVariable(COMPETITION_ID_KEY) long competitionId) {
        return deleteQuestionForSection(deleteQuestionForm.getDeleteQuestion(), ASSESSED_QUESTIONS_SECTION_NAME, competitionId);
    }

    @PostMapping(value = "/landing-page", params = "deleteAssessedQuestion")
    public String deleteAssessedQuestion(@ModelAttribute("deleteAssessedQuestion") DeleteAssessedQuestionForm deleteAssessedQuestionForm,
                                 @PathVariable(COMPETITION_ID_KEY) long competitionId) {
        return deleteQuestionForSection(deleteAssessedQuestionForm.getDeleteAssessedQuestion(), ASSESSED_QUESTIONS_SECTION_NAME, competitionId);
    }

    @PostMapping(value = "/landing-page", params = "deleteProjectDetailsQuestion")
    public String deleteProjectDetailsQuestion(@ModelAttribute("deleteAssessedQuestion") DeleteProjectDetailsQuestionForm deleteProjectDetailsQuestionForm,
                                               @PathVariable(COMPETITION_ID_KEY) long competitionId) {
        return deleteQuestionForSection(deleteProjectDetailsQuestionForm.getDeleteProjectDetailsQuestion(), PROJECT_DETAILS_SECTION_NAME, competitionId);
    }

    private String deleteQuestionForSection(Long deleteQuestion, String sectionName, long competitionId) {
        competitionSetupQuestionService.deleteQuestionForSection(deleteQuestion, sectionName);

        Supplier<String> view = () -> String.format(APPLICATION_LANDING_REDIRECT, competitionId);

        return view.get();
    }

    @GetMapping("/landing-page")
    public String applicationProcessLandingPage(Model model, @PathVariable(COMPETITION_ID_KEY) long competitionId) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        model.addAttribute(MODEL, competitionSetupService.populateCompetitionSectionModelAttributes(competitionResource, APPLICATION_FORM));
        model.addAttribute(COMPETITION_SETUP_FORM_KEY, new LandingPageForm());
        return "competition/setup";
    }

    @PostMapping("/landing-page")
    public String setApplicationProcessAsComplete(Model model,
                                                  @PathVariable(COMPETITION_ID_KEY) long competitionId,
                                                  @ModelAttribute(COMPETITION_SETUP_FORM_KEY) LandingPageForm form,
                                                  BindingResult bindingResult,
                                                  ValidationHandler validationHandler) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionId;
        }

        Supplier<String> failureView = () -> {
            model.addAttribute(MODEL, competitionSetupService.populateCompetitionSectionModelAttributes(competitionResource, APPLICATION_FORM));
            model.addAttribute(COMPETITION_SETUP_FORM_KEY, form);
            return "competition/setup";
        };
        Supplier<String> successView = () -> String.format(APPLICATION_LANDING_REDIRECT, competitionId);

        return validationHandler.performActionOrBindErrorsToField("", failureView, successView, () ->
                competitionSetupQuestionService.validateApplicationQuestions(competitionResource, form, bindingResult));
    }

    @GetMapping("/question/finance")
    public String seeApplicationFinances(@PathVariable(COMPETITION_ID_KEY) long competitionId,
                                         Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        return getFinancePage(model, competitionResource, false, null);
    }


    @GetMapping("/question/finance/edit")
    public String editApplicationFinances(@PathVariable(COMPETITION_ID_KEY) long competitionId,
                                          Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        return ifUserCanAccessEditPageMarkSectionAsIncomplete(competitionResource,
                () -> getFinancePage(model, competitionResource, true, null),
                Optional.of(FINANCES), Optional.empty());
    }

    @PostMapping("/question/finance/edit")
    public String submitApplicationFinances(@Valid @ModelAttribute(COMPETITION_SETUP_FORM_KEY) ApplicationFinanceForm form,
                                            BindingResult bindingResult,
                                            ValidationHandler validationHandler,
                                            @PathVariable(COMPETITION_ID_KEY) long competitionId,
                                            Model model) {

        return handleFinanceSaving(competitionId, model, form, validationHandler);
    }

    @PostMapping("/question/finance/none/edit")
    public String submitApplicationNoFinances(@ModelAttribute(COMPETITION_SETUP_FORM_KEY) ApplicationFinanceForm form,
                                              BindingResult bindingResult,
                                              ValidationHandler validationHandler,
                                              @PathVariable(COMPETITION_ID_KEY) long competitionId,
                                              Model model) {

       return handleFinanceSaving(competitionId, model, form, validationHandler);
    }

    private String handleFinanceSaving(long competitionId, Model model, ApplicationFinanceForm form, ValidationHandler validationHandler) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        Supplier<String> failureView = () -> getFinancePage(model, competitionResource, true, form);
        Supplier<String> successView = () -> String.format(APPLICATION_LANDING_REDIRECT, competitionId);

        return validationHandler.performActionOrBindErrorsToField("", failureView, successView,
                () -> competitionSetupService.saveCompetitionSetupSubsection(form, competitionResource, APPLICATION_FORM, FINANCES));
    }

    @GetMapping("/question/{questionId}")
    public String seeQuestionInCompSetup(@PathVariable(COMPETITION_ID_KEY) long competitionId,
                                         @PathVariable("questionId") Long questionId,
                                         Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        return getQuestionPage(model, competitionResource, questionId, false, null);
    }

    @GetMapping("/question/{questionId}/edit")
    public String editQuestionInCompSetup(@PathVariable(COMPETITION_ID_KEY) long competitionId,
                                          @PathVariable("questionId") Long questionId,
                                          Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);
        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }
        return ifUserCanAccessEditPageMarkSectionAsIncomplete(competitionResource,
                () -> getQuestionPage(model, competitionResource, questionId, true, null),
                Optional.empty(),
                Optional.ofNullable(questionId));
    }

    @PostMapping(value = "/question/{questionId}/edit", params = "question.type=ASSESSED_QUESTION")
    public String submitAssessedQuestion(@Valid @ModelAttribute(COMPETITION_SETUP_FORM_KEY) ApplicationQuestionForm competitionSetupForm,
                                            BindingResult bindingResult,
                                            ValidationHandler validationHandler,
                                            @PathVariable(COMPETITION_ID_KEY) long competitionId,
                                            Model model) {
        validateAssessmentGuidanceRows(competitionSetupForm, bindingResult);

        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        Supplier<String> failureView = () -> getQuestionPage(model, competitionResource, competitionSetupForm.getQuestion().getQuestionId(), true, competitionSetupForm);
        Supplier<String> successView = () -> String.format(APPLICATION_LANDING_REDIRECT, competitionId);

        return validationHandler.performActionOrBindErrorsToField("", failureView, successView,
                () -> competitionSetupService.saveCompetitionSetupSubsection(competitionSetupForm, competitionResource, APPLICATION_FORM, QUESTIONS));
    }

    @PostMapping("/question/{questionId}/edit")
    public String submitProjectDetailsQuestion(@Valid @ModelAttribute(COMPETITION_SETUP_FORM_KEY) ApplicationProjectForm competitionSetupForm,
                                               BindingResult bindingResult,
                                               ValidationHandler validationHandler,
                                               @PathVariable(COMPETITION_ID_KEY) long competitionId,
                                               Model model) {
        validateScopeGuidanceRows(competitionSetupForm, bindingResult);

        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        Supplier<String> failureView = () -> getQuestionPage(model, competitionResource, competitionSetupForm.getQuestion().getQuestionId(), true, competitionSetupForm);
        Supplier<String> successView = () -> String.format(APPLICATION_LANDING_REDIRECT, competitionId);

        return validationHandler.performActionOrBindErrorsToField("", failureView, successView,
                () -> competitionSetupService.saveCompetitionSetupSubsection(competitionSetupForm, competitionResource, APPLICATION_FORM, PROJECT_DETAILS));
    }

    @GetMapping(value = "/detail")
    public String viewApplicationDetails(@PathVariable(COMPETITION_ID_KEY) long competitionId,
                                         Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        return getDetailsPage(model, competitionResource, false, null);
    }

    @GetMapping(value = "/detail/edit")
    public String getEditApplicationDetails(@PathVariable(COMPETITION_ID_KEY) long competitionId,
                                            Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if(competitionResource.isNonIfs()) {
            return "redirect:/non-ifs-competition/setup/" + competitionId;
        }

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        return ifUserCanAccessEditPageMarkSectionAsIncomplete(competitionResource,
                () ->  getDetailsPage(model, competitionResource, true, null),
                Optional.of(APPLICATION_DETAILS),
                Optional.empty());

    }

    @PostMapping("/detail/edit")
    public String submitApplicationDetails(@ModelAttribute(COMPETITION_SETUP_FORM_KEY) ApplicationDetailsForm form,
                                           BindingResult bindingResult,
                                           ValidationHandler validationHandler,
                                           @PathVariable(COMPETITION_ID_KEY) long competitionId,
                                           Model model) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        if (!competitionSetupService.isInitialDetailsCompleteOrTouched(competitionId)) {
            return "redirect:/competition/setup/" + competitionResource.getId();
        }

        Supplier<String> failureView = () -> getDetailsPage(model, competitionResource, true, form);
        Supplier<String> successView = () -> String.format(APPLICATION_LANDING_REDIRECT, competitionId);

        return validationHandler.performActionOrBindErrorsToField("", failureView, successView,
                () -> competitionSetupService.saveCompetitionSetupSubsection(form, competitionResource, APPLICATION_FORM, APPLICATION_DETAILS));

    }

    private void validateAssessmentGuidanceRows(ApplicationQuestionForm applicationQuestionForm, BindingResult bindingResult) {
        if (Boolean.TRUE.equals(applicationQuestionForm.getQuestion().getWrittenFeedback())) {
            ValidationUtils.invokeValidator(validator, applicationQuestionForm, bindingResult, GuidanceRowForm.GuidanceRowViewGroup.class);
        }
    }

    private void validateScopeGuidanceRows(ApplicationProjectForm applicationProjectForm, BindingResult bindingResult) {
        if (Boolean.TRUE.equals(applicationProjectForm.getQuestion().getWrittenFeedback())) {
            ValidationUtils.invokeValidator(validator, applicationProjectForm, bindingResult, GuidanceRowResource.GuidanceRowGroup.class);
        }
    }

    private String getFinancePage(Model model, CompetitionResource competitionResource, boolean isEditable, CompetitionSetupForm form) {
        model.addAttribute(MODEL, setupQuestionViewModel(competitionResource, Optional.empty(), FINANCES, isEditable));
        model.addAttribute(COMPETITION_SETUP_FORM_KEY, setupQuestionForm(competitionResource, Optional.empty(), FINANCES, form));
        return "competition/finances";
    }

    private String getDetailsPage(Model model, CompetitionResource competitionResource, boolean isEditable, CompetitionSetupForm form) {
        model.addAttribute(MODEL, setupQuestionViewModel(competitionResource, Optional.empty(), APPLICATION_DETAILS, isEditable));
        model.addAttribute(COMPETITION_SETUP_FORM_KEY, setupQuestionForm(competitionResource, Optional.empty(), APPLICATION_DETAILS, form));
        return "competition/application-details";
    }

    private String getQuestionPage(Model model, CompetitionResource competitionResource, Long questionId, boolean isEditable, CompetitionSetupForm form) {
        ServiceResult<String> view = competitionSetupQuestionService.getQuestion(questionId).andOnSuccessReturn(
                questionResource -> {
                    CompetitionSetupQuestionType type = questionResource.getType();
                    CompetitionSetupSubsection setupSubsection;

                    if (type.equals(CompetitionSetupQuestionType.ASSESSED_QUESTION)) {
                        setupSubsection = CompetitionSetupSubsection.QUESTIONS;
                    } else {
                        setupSubsection = CompetitionSetupSubsection.PROJECT_DETAILS;
                    }

                    model.addAttribute(MODEL, setupQuestionViewModel(competitionResource, Optional.of(questionId), setupSubsection, isEditable));
                    model.addAttribute(COMPETITION_SETUP_FORM_KEY, setupQuestionForm(competitionResource, Optional.of(questionId), setupSubsection, form));

                    return questionView;
                }).andOnFailure(() -> serviceSuccess("redirect:/non-ifs-competition/setup/" + questionId));

        return view.getSuccess();
    }

    private QuestionSetupViewModel setupQuestionViewModel(final CompetitionResource competition, final Optional<Long> questionId, CompetitionSetupSubsection subsection, boolean isEditable) {
        CompetitionSetupSection section = APPLICATION_FORM;

        CompetitionSetupSubsectionViewModel subsectionViewModel = competitionSetupService.populateCompetitionSubsectionModelAttributes(competition, section,
                subsection, questionId);
        GeneralSetupViewModel generalViewModel = competitionSetupPopulator.populateGeneralModelAttributes(competition, section);

        return new QuestionSetupViewModel(generalViewModel, subsectionViewModel, competition.getName(), isEditable);
    }

    private CompetitionSetupForm setupQuestionForm(final CompetitionResource competition, final Optional<Long> questionId, CompetitionSetupSubsection subsection, CompetitionSetupForm competitionSetupForm) {
        CompetitionSetupSection section = APPLICATION_FORM;

        if (competitionSetupForm == null) {
            competitionSetupForm = competitionSetupService.getSubsectionFormData(
                    competition,
                    section,
                    subsection,
                    questionId);
        }

        return competitionSetupForm;
    }

    private String ifUserCanAccessEditPageMarkSectionAsIncomplete(CompetitionResource competition, Supplier<String> successAction,
                                                                  Optional<CompetitionSetupSubsection> subsectionOpt,
                                                                  Optional<Long> questionIdOpt) {
        if(CompetitionSetupSection.APPLICATION_FORM.preventEdit(competition)) {
            LOG.error(String.format("Competition with id %1$d cannot edit section %2$s: ", competition.getId(), CompetitionSetupSection.APPLICATION_FORM));
            return "redirect:/dashboard";
        } else {
            questionIdOpt.ifPresent(questionId -> questionSetupRestService.markQuestionSetupIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM, questionId));
            subsectionOpt.ifPresent(competitionSetupSubsection -> competitionSetupRestService.markSubSectionIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM, competitionSetupSubsection));
            competitionSetupRestService.markSectionIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM);
            return successAction.get();
        }
    }
}
