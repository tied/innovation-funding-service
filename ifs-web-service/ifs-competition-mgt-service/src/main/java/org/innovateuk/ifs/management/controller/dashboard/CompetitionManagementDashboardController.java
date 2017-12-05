package org.innovateuk.ifs.management.controller.dashboard;

import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResultItem;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.service.CompetitionSetupRestService;
import org.innovateuk.ifs.management.service.CompetitionDashboardSearchService;
import org.innovateuk.ifs.management.viewmodel.dashboard.*;
import org.innovateuk.ifs.project.bankdetails.service.BankDetailsRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

@Controller
public class CompetitionManagementDashboardController {
    private static final String TEMPLATE_PATH = "dashboard/";
    private static final String MODEL_ATTR = "model";

    @Autowired
    private CompetitionDashboardSearchService competitionDashboardSearchService;

    @Autowired
    private CompetitionSetupRestService competitionSetupRestService;

    @Autowired
    private BankDetailsRestService bankDetailsRestService;

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead')")
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/dashboard/live";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead')")
    @GetMapping("/dashboard/live")
    public String live(Model model, UserResource user){
        Map<CompetitionStatus, List<CompetitionSearchResultItem>> liveCompetitions = competitionDashboardSearchService.getLiveCompetitions();
        model.addAttribute(MODEL_ATTR, new LiveDashboardViewModel(liveCompetitions, competitionDashboardSearchService.getCompetitionCounts(), new DashboardTabsViewModel(user)));
        return TEMPLATE_PATH + "live";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead')")
    @GetMapping("/dashboard/project-setup")
    public String projectSetup(Model model, UserResource user) {
        final Map<CompetitionStatus, List<CompetitionSearchResultItem>> projectSetupCompetitions = competitionDashboardSearchService.getProjectSetupCompetitions();
        Long countBankDetails = bankDetailsRestService.countBankDetailsApprovals().getSuccessObjectOrThrowException();

        model.addAttribute(MODEL_ATTR,
                new ProjectSetupDashboardViewModel(projectSetupCompetitions, competitionDashboardSearchService.getCompetitionCounts(), countBankDetails, new DashboardTabsViewModel(user)));

        return TEMPLATE_PATH + "projectSetup";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support')")
    @GetMapping("/dashboard/upcoming")
    public String upcoming(Model model, UserResource user) {
        final Map<CompetitionStatus, List<CompetitionSearchResultItem>> upcomingCompetitions = competitionDashboardSearchService.getUpcomingCompetitions();

        model.addAttribute(MODEL_ATTR, new UpcomingDashboardViewModel(upcomingCompetitions,
                competitionDashboardSearchService.getCompetitionCounts(),
                formatInnovationAreaNames(upcomingCompetitions), new DashboardTabsViewModel(user)));

        return TEMPLATE_PATH + "upcoming";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support')")
    @GetMapping("/dashboard/previous")
    public String previous(Model model, UserResource user) {
        model.addAttribute(MODEL_ATTR, new PreviousDashboardViewModel(competitionDashboardSearchService.getPreviousCompetitions(),
                competitionDashboardSearchService.getCompetitionCounts(), new DashboardTabsViewModel(user)));

        return TEMPLATE_PATH + "previous";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support')")
    @GetMapping("/dashboard/non-ifs")
    public String nonIfs(Model model, UserResource user) {
        model.addAttribute(MODEL_ATTR, new NonIFSDashboardViewModel(competitionDashboardSearchService.getNonIfsCompetitions(), competitionDashboardSearchService.getCompetitionCounts(), new DashboardTabsViewModel(user)));
        return TEMPLATE_PATH + "non-ifs";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead')")
    @GetMapping("/dashboard/search")
    public String search(@RequestParam(name = "searchQuery", defaultValue = "") String searchQuery,
                         @RequestParam(name = "page", defaultValue = "1") int page, Model model,
                         UserResource user) {
        String trimmedSearchQuery = StringUtils.normalizeSpace(searchQuery);
        model.addAttribute("results", competitionDashboardSearchService.searchCompetitions(trimmedSearchQuery, page - 1));
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("tabs", new DashboardTabsViewModel(user));
        return TEMPLATE_PATH + "search";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @GetMapping("/competition/create")
    public String create(){
        CompetitionResource competition = competitionSetupRestService.create().getSuccessObjectOrThrowException();
        return String.format("redirect:/competition/setup/%s", competition.getId());
    }

    private List<String> formatInnovationAreaNames(Map<CompetitionStatus, List<CompetitionSearchResultItem>> competitionTypes) {

        List<String> formattedList = new ArrayList<>();

        for (Map.Entry<CompetitionStatus, List<CompetitionSearchResultItem>> entry : competitionTypes.entrySet()) {
            for (CompetitionSearchResultItem competition : entry.getValue()) {
                formattedList.add(competition.getInnovationAreaNames().stream().collect(joining(", ")));
            }
        }
        return formattedList;
    }
}
