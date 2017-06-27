package org.innovateuk.ifs.management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationFundingDecisionService;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.form.*;
import org.innovateuk.ifs.competition.service.ApplicationSummarySortFieldService;
import org.innovateuk.ifs.management.service.CompetitionManagementApplicationServiceImpl;
import org.innovateuk.ifs.management.viewmodel.PaginationViewModel;
import org.innovateuk.ifs.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.innovateuk.ifs.util.BackLinkUtil.buildOriginQueryString;
import static org.innovateuk.ifs.util.JsonUtil.getObjectFromJson;
import static org.innovateuk.ifs.util.JsonUtil.getSerializedObject;

/**
 * Handles the Competition Management Funding decision views and submission of funding decision.
 */
@Controller
@RequestMapping("/competition/{competitionId}/funding")
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class CompetitionManagementFundingDecisionController {

    private static final Log log = LogFactory.getLog(CompetitionManagementFundingDecisionController.class);

    private static final int PAGE_SIZE = 20;
    private static final String SELECTION_FORM = "fundingDecisionSelectionForm";
    private static final int SELECTION_LIMIT = 500;

    @Autowired
    private ApplicationSummarySortFieldService applicationSummarySortFieldService;

    @Autowired
    private ApplicationSummaryRestService applicationSummaryRestService;

    @Autowired
    private ApplicationFundingDecisionService applicationFundingDecisionService;

    @Autowired
    @Qualifier("mvcValidator")
    private Validator validator;

    @Autowired
    private CookieUtil cookieUtil;

    @GetMapping
    public String applications(Model model,
                               @PathVariable("competitionId") long competitionId,
                               @RequestParam(name = "clearFilters", required = false) boolean clearFilters,
                               @ModelAttribute @Valid FundingDecisionPaginationForm paginationForm,
                               @ModelAttribute FundingDecisionFilterForm filterForm,
                               @ModelAttribute FundingDecisionSelectionForm selectionForm,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            return "redirect:/competition/" + competitionId + "/funding";
        }

        FundingDecisionSelectionCookie selectionCookieForm = new FundingDecisionSelectionCookie();

        try {
            selectionCookieForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
            selectionForm = selectionCookieForm.getFundingDecisionSelectionForm();
            FundingDecisionFilterForm filterCookieForm = selectionCookieForm.getFundingDecisionFilterForm();

            if(clearFilters) {
                filterForm = new FundingDecisionFilterForm();
            }
            else if (!filterForm.anyFilterIsActive() && filterCookieForm.anyFilterIsActive() && selectionForm.anySelectionIsMade()) {
                filterForm.setFundingFilter(selectionCookieForm.getFundingDecisionFilterForm().getFundingFilter());
                filterForm.setStringFilter(selectionCookieForm.getFundingDecisionFilterForm().getStringFilter());
            }

            FundingDecisionSelectionForm trimmedSelectionForm = trimSelectionByFilteredResult(selectionForm, filterForm, competitionId);
            selectionForm.setApplicationIds(trimmedSelectionForm.getApplicationIds());

        } catch (Exception e) {
            log.error(e);
        }


        selectionCookieForm.setFundingDecisionFilterForm(filterForm);

        saveFormToCookie(response, competitionId, selectionCookieForm);

        return populateSubmittedModel(model, competitionId, paginationForm, filterForm, selectionForm);

    }

    @PostMapping
    public String makeDecision(Model model,
                               @PathVariable("competitionId") long competitionId,
                               @ModelAttribute FundingDecisionPaginationForm paginationForm,
                               @ModelAttribute FundingDecisionSelectionForm fundingDecisionSelectionForm,
                               @ModelAttribute @Valid FundingDecisionChoiceForm fundingDecisionChoiceForm,
                               @ModelAttribute FundingDecisionFilterForm filterForm,
                               BindingResult bindingResult,
                               HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "redirect:/competition/" + competitionId + "/funding";
        }

        try {
            FundingDecisionSelectionCookie selectionForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
            fundingDecisionSelectionForm = selectionForm.getFundingDecisionSelectionForm();
        } catch (Exception e) {
            log.error(e);
        }

        return fundersPanelCompetition(model, competitionId, fundingDecisionSelectionForm, paginationForm, fundingDecisionChoiceForm, filterForm, bindingResult);
    }

    @PostMapping(params = {"addAll"})
    public @ResponseBody
    JsonNode addAllApplicationsToFundingDecisionSelectionList(@PathVariable("competitionId") long competitionId,
                                                     @RequestParam("addAll") boolean addAll,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        FundingDecisionSelectionCookie selectionCookie;

        try {
            selectionCookie = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
        } catch (Exception e) {
            log.error(e);
            return createFailureResponse();
        }

        if (addAll) {
            List<Long> allApplicationIdsBasedOnFilter = getAllApplicationIdsByFilters(competitionId, selectionCookie.getFundingDecisionFilterForm());

            addAllApplicationsIdsBasedOnFilter(competitionId, selectionCookie, allApplicationIdsBasedOnFilter);
        } else {
            removeAllApplicationsIds(selectionCookie);
        }

        saveFormToCookie(response, competitionId, selectionCookie);

        return createSuccessfulResponseWithSelectionStatus(selectionCookie.getFundingDecisionSelectionForm().getApplicationIds().size(), selectionCookie.getFundingDecisionSelectionForm().isAllSelected(), false);
    }

    private void addAllApplicationsIdsBasedOnFilter(long competitionId, FundingDecisionSelectionCookie selectionCookie, List<Long> allIds) {

        List<Long> limitedList = limitList(allIds);

        selectionCookie.getFundingDecisionSelectionForm().setApplicationIds(limitedList);
        selectionCookie.getFundingDecisionSelectionForm().setAllSelected(true);
    }

    private void removeAllApplicationsIds(FundingDecisionSelectionCookie selectionCookie) {
        selectionCookie.getFundingDecisionSelectionForm().setApplicationIds(Arrays.asList());
        selectionCookie.getFundingDecisionSelectionForm().setAllSelected(false);
    }

    @PostMapping(params = {"selectionId", "isSelected"})
    public @ResponseBody JsonNode addSelectedApplicationsToFundingDecisionList(@PathVariable("competitionId") long competitionId,
                                                               @RequestParam("selectionId") long applicationId,
                                                               @RequestParam("isSelected") boolean isSelected,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {

        boolean limitExceeded = false;

        try {
            FundingDecisionSelectionCookie cookieForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
            FundingDecisionSelectionForm selectionForm = cookieForm.getFundingDecisionSelectionForm();
            if (isSelected) {
                List<Long> applicationIds = selectionForm.getApplicationIds();

                int predictedSize = selectionForm.getApplicationIds().size() + 1;

                if(!applicationIds.contains(applicationId)) {

                    if(limitIsExceeded(predictedSize)){
                        limitExceeded = true;
                    }
                    else {
                        selectionForm.getApplicationIds().add(applicationId);
                        List<Long> filteredApplicationList = getAllApplicationIdsByFilters(competitionId, cookieForm.getFundingDecisionFilterForm());

                        if (selectionForm.containsAll(filteredApplicationList)) {
                            selectionForm.setAllSelected(true);
                        }
                    }
                }
            } else {
                selectionForm.getApplicationIds().remove(applicationId);
                selectionForm.setAllSelected(false);
            }

            cookieForm.setFundingDecisionSelectionForm(selectionForm);

            saveFormToCookie(response, competitionId, cookieForm);

            return createSuccessfulResponseWithSelectionStatus(selectionForm.getApplicationIds().size(), selectionForm.isAllSelected(), limitExceeded);
        } catch (Exception e) {
            log.error(e);
            return createFailureResponse();
        }
    }

    private Optional<FundingDecisionSelectionCookie> getApplicationSelectionFormFromCookie(HttpServletRequest request, long competitionId) {
        String assessorFormJson = cookieUtil.getCompressedCookieValue(request, format("%s_comp%s", SELECTION_FORM, competitionId));
        if (isNotBlank(assessorFormJson)) {
            return Optional.ofNullable(getObjectFromJson(assessorFormJson, FundingDecisionSelectionCookie.class));
        } else {
            return Optional.empty();
        }
    }

    private List<Long> getAllApplicationIdsByFilters(Long competitionId, FundingDecisionFilterForm filterForm) {
        RestResult<List<ApplicationSummaryResource>> restResult = applicationSummaryRestService.getAllSubmittedApplications(competitionId, filterForm.getStringFilter(), filterForm.getFundingFilter());

        return restResult.getSuccessObjectOrThrowException().stream().map(p -> p.getId()).collect(Collectors.toList());
    }

    private FundingDecisionSelectionForm trimSelectionByFilteredResult(FundingDecisionSelectionForm selectionForm, FundingDecisionFilterForm filterForm, Long competitionId) {
        List<Long> filteredApplicationIds = getAllApplicationIdsByFilters(competitionId, filterForm);

        FundingDecisionSelectionForm updatedSelectionForm = new FundingDecisionSelectionForm();

        if(selectionForm.isAllSelected()) {
            updatedSelectionForm.setApplicationIds(filteredApplicationIds);
        }
        else {
            selectionForm.getApplicationIds().retainAll(filteredApplicationIds);
            updatedSelectionForm.setApplicationIds(selectionForm.getApplicationIds());
        }

        return updatedSelectionForm;
    }

    private ObjectNode createFailureResponse() {
        return createJsonObjectNode(-1, false, false);
    }

    private ObjectNode createSuccessfulResponseWithSelectionStatus(int selectionCount, boolean allSelected, boolean limitExceeded) {
        return createJsonObjectNode(selectionCount, allSelected, limitExceeded);
    }

    private ObjectNode createJsonObjectNode(int selectionCount, boolean allSelected, boolean limitExceeded) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("selectionCount", selectionCount);
        node.put("limitExceeded", limitExceeded);
        node.put("allSelected", allSelected);

        return node;
    }

    MultiValueMap<String, String> mapFormFilterParametersToMultiValueMap(FundingDecisionFilterForm fundingDecisionFilterForm) {
        MultiValueMap<String, String> filterMap = new LinkedMultiValueMap<>();
        if(fundingDecisionFilterForm.getFundingFilter().isPresent()) {
            filterMap.put("fundingFilter", Arrays.asList(fundingDecisionFilterForm.getFundingFilter().get().getName()));
        }
        if(fundingDecisionFilterForm.getStringFilter().isPresent()) {
            filterMap.put("stringFilter", Arrays.asList(fundingDecisionFilterForm.getStringFilter().get()));
        }

        return filterMap;
    }

    private String fundersPanelCompetition(Model model,
                                           Long competitionId,
                                           FundingDecisionSelectionForm fundingDecisionSelectionForm,
                                           FundingDecisionPaginationForm fundingDecisionPaginationForm,
                                           FundingDecisionChoiceForm fundingDecisionChoiceForm,
                                           FundingDecisionFilterForm fundingDecisionFilterForm,
                                           BindingResult bindingResult) {
        if (fundingDecisionChoiceForm.getFundingDecision() != null) {
            validator.validate(fundingDecisionSelectionForm, bindingResult);
            if (!bindingResult.hasErrors()) {
                Optional<FundingDecision> fundingDecision = applicationFundingDecisionService.getFundingDecisionForString(fundingDecisionChoiceForm.getFundingDecision());
                if (fundingDecision.isPresent()) {
                    applicationFundingDecisionService.saveApplicationFundingDecisionData(competitionId, fundingDecision.get(), fundingDecisionSelectionForm.getApplicationIds());
                }
            }
        }

        return populateSubmittedModel(model, competitionId, fundingDecisionPaginationForm, fundingDecisionFilterForm, fundingDecisionSelectionForm);
    }

    private ApplicationSummaryPageResource getApplicationsByFilters(Long competitionId, FundingDecisionPaginationForm paginationForm, FundingDecisionFilterForm fundingDecisionFilterForm) {
        return applicationSummaryRestService.getSubmittedApplications(
                competitionId,
                "id",
                paginationForm.getPage(),
                PAGE_SIZE,
                fundingDecisionFilterForm.getStringFilter(),
                fundingDecisionFilterForm.getFundingFilter())
                .getSuccessObjectOrThrowException();
    }

    private String populateSubmittedModel(Model model, Long competitionId, FundingDecisionPaginationForm paginationForm, FundingDecisionFilterForm fundingDecisionFilterForm, FundingDecisionSelectionForm fundingDecisionSelectionForm) {
        ApplicationSummaryPageResource results = getApplicationsByFilters(competitionId, paginationForm, fundingDecisionFilterForm);
        String originQuery = buildOriginQueryString(CompetitionManagementApplicationServiceImpl.ApplicationOverviewOrigin.FUNDING_APPLICATIONS, mapFormFilterParametersToMultiValueMap(fundingDecisionFilterForm));

        CompetitionSummaryResource competitionSummary = applicationSummaryRestService
                .getCompetitionSummary(competitionId)
                .getSuccessObjectOrThrowException();

        model.addAttribute("pagination", new PaginationViewModel(results, originQuery));
        model.addAttribute("results", results);
        model.addAttribute("selectionForm", fundingDecisionSelectionForm);
        model.addAttribute("competitionSummary", competitionSummary);
        model.addAttribute("originQuery", originQuery);
        model.addAttribute("selectAllDisabled", limitIsExceeded(results.getTotalElements()));

        switch (competitionSummary.getCompetitionStatus()) {
            case FUNDERS_PANEL:
            case ASSESSOR_FEEDBACK:
                return "comp-mgt-funders-panel";
            default:
                return "redirect:/login";
        }
    }

    private void saveFormToCookie(HttpServletResponse response, long competitionId, FundingDecisionSelectionCookie selectionForm) {
        cookieUtil.saveToCompressedCookie(response, format("%s_comp%s", SELECTION_FORM, competitionId), getSerializedObject(selectionForm));
    }

    private List<Long> limitList(List<Long> allIds) {
        return allIds.subList(0, SELECTION_LIMIT);
    }

    private boolean limitIsExceeded(long amountOfIds) {
        return amountOfIds > SELECTION_LIMIT;
    }
}
