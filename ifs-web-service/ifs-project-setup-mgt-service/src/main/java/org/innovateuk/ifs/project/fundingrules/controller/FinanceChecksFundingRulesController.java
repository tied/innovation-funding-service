package org.innovateuk.ifs.project.fundingrules.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.FundingRules;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.project.finance.service.FinanceCheckRestService;
import org.innovateuk.ifs.project.finance.service.ProjectFinanceRestService;
import org.innovateuk.ifs.project.fundingrules.form.FinanceChecksFundingRulesForm;
import org.innovateuk.ifs.project.fundingrules.populator.FinanceChecksFundingRulesViewModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * This controller serves the Funding Rules page where internal users can confirm the funding rules of a partner organisation's
 * financial position on a Project
 */
@Controller
@PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin', 'external_finance')")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = FinanceChecksFundingRulesController.class)
@RequestMapping("/project/{projectId}/finance-check/organisation/{organisationId}/funding-rules")
public class FinanceChecksFundingRulesController {

    @Autowired
    private FinanceCheckRestService financeCheckRestService;

    @Autowired
    private FinanceChecksFundingRulesViewModelPopulator financeChecksFundingRulesViewModelPopulator;

    @Autowired
    private ProjectFinanceRestService projectFinanceRestService;

    @GetMapping
    public String viewFundingRules(@PathVariable("projectId") Long projectId,
                                   @PathVariable("organisationId") Long organisationId, Model model) {

        return doViewFundingRules(projectId, organisationId, model, getFundingRulesForm(projectId, organisationId), false);
    }

    @GetMapping("/edit")
    public String editFundingRules(@PathVariable("projectId") Long projectId,
                                   @PathVariable("organisationId") Long organisationId, Model model) {

        return doViewFundingRules(projectId, organisationId, model, getFundingRulesForm(projectId, organisationId), true);
    }

    @PostMapping(value = "/edit", params = "save-and-continue")
    public String saveAndContinue(@PathVariable("projectId") Long projectId,
                                  @PathVariable("organisationId") Long organisationId,
                                  @ModelAttribute("form") FinanceChecksFundingRulesForm form,
                                  @SuppressWarnings("unused") BindingResult bindingResult,
                                  ValidationHandler validationHandler,
                                  Model model) {

        Supplier<String> successView = () -> "redirect:/project/" + projectId + "/finance-check";

        return doSaveFundingRules(projectId, organisationId, form, validationHandler, model, successView);
    }

    @PostMapping(params = "approve-funding-rules")
    public String approveFundingRules(@PathVariable("projectId") Long projectId,
                                      @PathVariable("organisationId") Long organisationId,
                                      @ModelAttribute("form") FinanceChecksFundingRulesForm form,
                                      @SuppressWarnings("unused") BindingResult bindingResult,
                                      ValidationHandler validationHandler,
                                      Model model) {

        Supplier<String> successView = () ->
                "redirect:/project/" + projectId + "/finance-check/organisation/" + organisationId + "/funding-rules";

        return doSaveFundingRules(projectId, organisationId, form, validationHandler, model, successView);
    }

    private String doSaveFundingRules(Long projectId, Long organisationId, FinanceChecksFundingRulesForm form,
                                      ValidationHandler validationHandler, Model model, Supplier<String> successView) {

        Supplier<String> failureView = () -> doViewFundingRules(projectId, organisationId, model, form, false);

        boolean northernIreland = Boolean.TRUE.equals(projectFinanceRestService.getProjectFinance(projectId, organisationId).getSuccess().getNorthernIrelandDeclaration());
        FundingRules fundingRules = northernIreland ? FundingRules.SUBSIDY_CONTROL : FundingRules.STATE_AID;

        return validationHandler.
                failNowOrSucceedWith(failureView, () -> {

                    FundingRules fundingRulesToSet;
                    if (form.isOverrideFundingRules()) {
                        fundingRulesToSet = FundingRules.STATE_AID == fundingRules ? FundingRules.SUBSIDY_CONTROL : FundingRules.STATE_AID;
                    } else {
                        fundingRulesToSet = fundingRules;
                    }

                    RestResult<Void> saveFundingRulesResult = financeCheckRestService.saveFundingRules(projectId, organisationId, fundingRules);

                    return validationHandler.
                    addAnyErrors(saveFundingRulesResult).
                            failNowOrSucceedWith(failureView, successView);
                });
    }

    private String doViewFundingRules(Long projectId, Long organisationId, Model model, FinanceChecksFundingRulesForm form, boolean editMode) {
        model.addAttribute("model", financeChecksFundingRulesViewModelPopulator.populateFundingRulesViewModel(projectId, organisationId, editMode));
        model.addAttribute("form", form);

        return "project/financecheck/fundingrules";
    }

    private FinanceChecksFundingRulesForm getFundingRulesForm(Long projectId, Long organisationId) {
        return new FinanceChecksFundingRulesForm();
    }

}