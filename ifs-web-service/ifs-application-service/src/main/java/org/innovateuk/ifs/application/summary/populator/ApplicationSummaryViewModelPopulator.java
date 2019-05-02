package org.innovateuk.ifs.application.summary.populator;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.summary.viewmodel.ApplicationSummaryViewModel;
import org.innovateuk.ifs.application.summary.viewmodel.ApplicationRowsSummaryViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.application.summary.ApplicationSummarySettings.defaultSettings;

@Component
public class ApplicationSummaryViewModelPopulator {

    @Autowired
    private ApplicationRowsSummaryViewModelPopulator applicationSummaryViewModelPopulator;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    public ApplicationSummaryViewModel populate(ApplicationResource application, CompetitionResource competition, UserResource user, boolean support) {

        ApplicationRowsSummaryViewModel applicationSummaryViewModel = applicationSummaryViewModelPopulator.populate(application, competition, user, defaultSettings());
        return new ApplicationSummaryViewModel(applicationSummaryViewModel, application, competition,
                isProjectWithdrawn(application.getId()), support);
    }

    private boolean isProjectWithdrawn(Long applicationId) {
        ProjectResource project = projectService.getByApplicationId(applicationId);
        return project != null && project.isWithdrawn();
    }

}
