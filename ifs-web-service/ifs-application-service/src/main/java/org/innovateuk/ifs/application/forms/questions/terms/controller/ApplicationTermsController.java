package org.innovateuk.ifs.application.forms.questions.terms.controller;

import org.innovateuk.ifs.application.common.populator.ApplicationTermsModelPopulator;
import org.innovateuk.ifs.application.common.populator.ApplicationTermsPartnerModelPopulator;
import org.innovateuk.ifs.application.common.viewmodel.ApplicationTermsViewModel;
import org.innovateuk.ifs.application.forms.questions.terms.form.ApplicationTermsForm;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.QuestionStatusRestService;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.APPLICATION_BASE_URL;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;

@Controller
@RequestMapping(APPLICATION_BASE_URL + "{applicationId}/form/question/{questionId}/terms-and-conditions")
@PreAuthorize("hasAnyAuthority('applicant', 'project_finance', 'ifs_administrator', 'comp_admin', 'support', 'innovation_lead', 'monitoring_officer', 'assessor', 'stakeholder', 'external_finance', 'supporter')")
@SecuredBySpring(value = "Controller",
        description = "Most roles are allowed to view the application terms",
        securedType = ApplicationTermsController.class)
public class ApplicationTermsController {

    private ProcessRoleRestService processRoleRestService;
    private ApplicationTermsModelPopulator applicationTermsModelPopulator;
    private QuestionStatusRestService questionStatusRestService;
    private ApplicationRestService applicationRestService;
    private ApplicationTermsPartnerModelPopulator applicationTermsPartnerModelPopulator;

    public ApplicationTermsController() {

    }

    @Autowired
    public ApplicationTermsController(ProcessRoleRestService processRoleRestService,
                                      QuestionStatusRestService questionStatusRestService,
                                      ApplicationRestService applicationRestService,
                                      ApplicationTermsPartnerModelPopulator applicationTermsPartnerModelPopulator,
                                      ApplicationTermsModelPopulator applicationTermsModelPopulator) {
        this.processRoleRestService = processRoleRestService;
        this.questionStatusRestService = questionStatusRestService;
        this.applicationRestService = applicationRestService;
        this.applicationTermsModelPopulator = applicationTermsModelPopulator;
        this.applicationTermsPartnerModelPopulator = applicationTermsPartnerModelPopulator;
    }

    @GetMapping
    public String getTerms(@PathVariable long applicationId,
                           @PathVariable long questionId,
                           UserResource user,
                           Model model,
                           @ModelAttribute(name = "form", binding = false) ApplicationTermsForm form,
                           @RequestParam(value = "readonly", defaultValue = "false") Boolean readOnly) {

        ApplicationTermsViewModel viewModel = applicationTermsModelPopulator.populate(user, applicationId, questionId, readOnly);
        model.addAttribute("model", viewModel);

        return "application/sections/terms-and-conditions/terms-and-conditions";
    }

    @PostMapping
    public String acceptTerms(@PathVariable long applicationId,
                              @PathVariable long questionId,
                              UserResource user,
                              Model model,
                              @ModelAttribute(name = "form", binding = false) ApplicationTermsForm form,
                              @SuppressWarnings("unused") BindingResult bindingResult,
                              ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> getTerms(applicationId, questionId, user, model, form, false);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            ProcessRoleResource processRole = processRoleRestService.findProcessRole(user.getId(), applicationId).getSuccess();
            RestResult<List<ValidationMessages>> result = questionStatusRestService.markAsComplete(questionId, applicationId, processRole.getId());

            return validationHandler.addAnyErrors(result, fieldErrorsToFieldErrors(), asGlobalErrors())
                    .failNowOrSucceedWith(
                            failureView,
                            () -> format("redirect:%s%d/form/question/%d/terms-and-conditions#terms-accepted", APPLICATION_BASE_URL, applicationId, questionId));
        });
    }

}