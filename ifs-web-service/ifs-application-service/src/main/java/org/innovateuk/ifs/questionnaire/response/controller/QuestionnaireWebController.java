package org.innovateuk.ifs.questionnaire.response.controller;


import org.innovateuk.ifs.commons.exception.IFSRuntimeException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.questionnaire.config.service.QuestionnaireOptionRestService;
import org.innovateuk.ifs.questionnaire.config.service.QuestionnaireQuestionRestService;
import org.innovateuk.ifs.questionnaire.config.service.QuestionnaireRestService;
import org.innovateuk.ifs.questionnaire.config.service.QuestionnaireTextOutcomeRestService;
import org.innovateuk.ifs.questionnaire.response.form.QuestionnaireQuestionForm;
import org.innovateuk.ifs.questionnaire.response.populator.QuestionnaireQuestionViewModelPopulator;
import org.innovateuk.ifs.questionnaire.resource.*;
import org.innovateuk.ifs.questionnaire.response.service.QuestionnaireQuestionResponseRestService;
import org.innovateuk.ifs.questionnaire.response.service.QuestionnaireResponseRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.util.EncryptedCookieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;
import java.util.function.Supplier;

import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;

@Controller
@RequestMapping("/questionnaire")
@PreAuthorize("permitAll")
public class QuestionnaireWebController {

    protected static final String REDIRECT_URL_COOKIE_KEY = "QUESTIONNAIRE_REDIRECT_URL";

    @Autowired
    private QuestionnaireRestService questionnaireRestService;

    @Autowired
    private QuestionnaireQuestionRestService questionnaireQuestionRestService;

    @Autowired
    private QuestionnaireOptionRestService questionnaireOptionRestService;

    @Autowired
    private QuestionnaireResponseRestService questionnaireResponseRestService;

    @Autowired
    private QuestionnaireQuestionResponseRestService questionnaireQuestionResponseRestService;

    @Autowired
    private QuestionnaireTextOutcomeRestService questionnaireTextOutcomeRestService;

    @Autowired
    private QuestionnaireQuestionViewModelPopulator questionnaireQuestionViewModelPopulator;

    @Autowired
    private EncryptedCookieService encryptedCookieService;

    @GetMapping("/{questionnaireResponseId}")
    public String welcomeScreen(Model model,
                                HttpServletRequest request,
                                UserResource user,
                                @PathVariable String questionnaireResponseId,
                                @RequestParam(required = false) String redirectUrl,
                                HttpServletResponse httpServletResponse) {
        if (redirectUrl != null) {
            encryptedCookieService.saveToCookie(httpServletResponse, REDIRECT_URL_COOKIE_KEY, redirectUrl);
        }
        QuestionnaireResponseResource response = questionnaireResponseRestService.get(questionnaireResponseId).getSuccess();
        QuestionnaireResource questionnaire = questionnaireRestService.get(response.getQuestionnaire()).getSuccess();
        model.addAttribute("questionnaire", questionnaire);
        return "questionnaire/welcome";
    }

    @PostMapping("/{questionnaireResponseId}")
    public String start(Model model,
                        HttpServletRequest request,
                        UserResource user,
                        @PathVariable String questionnaireResponseId) {
        QuestionnaireResponseResource response = questionnaireResponseRestService.get(questionnaireResponseId).getSuccess();
        QuestionnaireResource questionnaire = questionnaireRestService.get(response.getQuestionnaire()).getSuccess();
        return String.format("redirect:/questionnaire/%s/question/%d", questionnaireResponseId, questionnaire.getQuestions().get(0));
    }

    @GetMapping("/{questionnaireResponseId}/question/{questionId}")
    public String question(Model model,
                           HttpServletRequest request,
                           UserResource user,
                           @PathVariable String questionnaireResponseId,
                           @PathVariable long questionId,
                            @RequestParam(required = false) String redirectUrl,
                           HttpServletResponse httpServletResponse) {
        if (redirectUrl != null) {
            encryptedCookieService.saveToCookie(httpServletResponse, REDIRECT_URL_COOKIE_KEY, redirectUrl);
        }
        QuestionnaireQuestionForm form = new QuestionnaireQuestionForm();
        questionnaireQuestionResponseRestService.findByQuestionnaireQuestionIdAndQuestionnaireResponseId(questionId, questionnaireResponseId)
                .toOptionalIfNotFound()
                .getSuccess()
                .ifPresent(questionResponse -> {
                    form.setQuestionResponseId(questionResponse.getId());
                    form.setOption(questionResponse.getOption());
                });
        model.addAttribute("form", form);
        return viewQuestion(model, questionnaireResponseId, questionId);
    }

    @PostMapping("/{questionnaireResponseId}/question/{questionId}")
    public String saveQuestionResponse(@Valid @ModelAttribute("form") QuestionnaireQuestionForm form,
                                       BindingResult result,
                                       ValidationHandler validationHandler,
                                       Model model,
                                       HttpServletRequest request,
                                       UserResource user,
                                       @PathVariable String questionnaireResponseId,
                                       @PathVariable long questionId) {
        Supplier<String> successView = () -> {
            QuestionnaireOptionResource option = questionnaireOptionRestService.get(form.getOption()).getSuccess();
            if (option.getDecisionType() == DecisionType.QUESTION) {
                return String.format("redirect:/questionnaire/%s/question/%d", questionnaireResponseId, option.getDecision());
            } else if (option.getDecisionType() == DecisionType.TEXT_OUTCOME) {
                return String.format("redirect:/questionnaire/%s/outcome/%d", questionnaireResponseId, option.getDecision());
            }
            throw new IFSRuntimeException("Unknown decision type " + option.getDecisionType());
        };
        Supplier<String> failureView = () -> viewQuestion(model, questionnaireResponseId, questionId);

        RestResult<Void> saveResult;
        if (form.getQuestionResponseId() != null) {
            QuestionnaireQuestionResponseResource response = questionnaireQuestionResponseRestService.get(form.getQuestionResponseId()).getSuccess();
            response.setOption(form.getOption());
            saveResult = questionnaireQuestionResponseRestService.update(response.getId(), response);
        } else {
            QuestionnaireQuestionResponseResource response = new QuestionnaireQuestionResponseResource();
            response.setQuestionnaireResponse(questionnaireResponseId);
            response.setOption(form.getOption());
            saveResult = questionnaireQuestionResponseRestService.create(response).andOnSuccess(() -> restSuccess());
        }
        validationHandler.addAnyErrors(saveResult);
        return validationHandler.failNowOrSucceedWith(failureView, successView);
    }

    @GetMapping("/{questionnaireResponseId}/outcome/{outcomeId}")
    public String outcome(Model model,
                           HttpServletRequest request,
                           UserResource user,
                           @PathVariable String questionnaireResponseId,
                           @PathVariable long outcomeId) {
        QuestionnaireTextOutcomeResource outcome = questionnaireTextOutcomeRestService.get(outcomeId).getSuccess();
        if (outcome.getText() == null) {
            String redirectUrl =  "redirect:" + Optional.ofNullable(encryptedCookieService.getCookieValue(request, REDIRECT_URL_COOKIE_KEY))
                    .orElse("/");
            if (outcome.getImplementation() != null) {
                redirectUrl += "?outcome=" + outcome.getImplementation().name();
            }
            return redirectUrl;
        }

        model.addAttribute("model", outcome);
        model.addAttribute("redirectUrl", Optional.ofNullable(encryptedCookieService.getCookieValue(request, REDIRECT_URL_COOKIE_KEY))
                .orElse("/"));
        return "questionnaire/outcome";
    }

    private String viewQuestion(Model model, String questionnaireResponseId, long questionId) {
        model.addAttribute("model", questionnaireQuestionViewModelPopulator.populate(questionnaireResponseId, questionId));
        return "questionnaire/question";
    }
}
