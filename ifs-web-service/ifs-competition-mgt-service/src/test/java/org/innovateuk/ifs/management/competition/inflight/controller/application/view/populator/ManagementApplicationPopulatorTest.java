package org.innovateuk.ifs.management.competition.inflight.controller.application.view.populator;

import org.innovateuk.ifs.application.readonly.populator.ApplicationReadOnlyViewModelPopulator;
import org.innovateuk.ifs.application.readonly.viewmodel.ApplicationReadOnlyViewModel;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.summary.populator.InterviewFeedbackViewModelPopulator;
import org.innovateuk.ifs.application.summary.viewmodel.InterviewFeedbackViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputRestService;
import org.innovateuk.ifs.interview.service.InterviewAssignmentRestService;
import org.innovateuk.ifs.management.application.view.populator.ApplicationOverviewIneligibilityModelPopulator;
import org.innovateuk.ifs.management.application.view.populator.ManagementApplicationPopulator;
import org.innovateuk.ifs.management.application.view.viewmodel.ApplicationOverviewIneligibilityViewModel;
import org.innovateuk.ifs.management.application.view.viewmodel.ManagementApplicationViewModel;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static org.innovateuk.ifs.application.readonly.ApplicationReadOnlySettings.defaultSettings;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.ASSESSOR_FEEDBACK;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagementApplicationPopulatorTest {

    @InjectMocks
    private ManagementApplicationPopulator target;

    @Mock
    private ApplicationOverviewIneligibilityModelPopulator applicationOverviewIneligibilityModelPopulator;

    @Mock
    private ApplicationReadOnlyViewModelPopulator applicationReadOnlyViewModelPopulator;

    @Mock
    private InterviewFeedbackViewModelPopulator interviewFeedbackViewModelPopulator;

    @Mock
    private ApplicationRestService applicationRestService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private FormInputResponseRestService formInputResponseRestService;

    @Mock
    private FormInputRestService formInputRestService;

    @Mock
    private FileEntryRestService fileEntryRestService;

    @Mock
    private InterviewAssignmentRestService interviewAssignmentRestService;

    @Mock
    private ProjectService projectService;

    @Test
    public void populate_withoutInterviewFeedback() {
        CompetitionResource competition = newCompetitionResource()
                .withInnovationAreas(singleton(1L))
                .build();
        ApplicationResource application = newApplicationResource()
                .withCompetition(competition.getId())
                .withApplicationState(ApplicationState.SUBMITTED)
                .withInnovationArea(newInnovationAreaResource().build())
                .build();
        UserResource user = newUserResource()
                .withRoleGlobal(Role.COMP_ADMIN)
                .build();

        when(applicationRestService.getApplicationById(application.getId())).thenReturn(restSuccess(application));
        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competition));
        when(applicationReadOnlyViewModelPopulator.populate(application, competition, user, defaultSettings())).thenReturn(mock(ApplicationReadOnlyViewModel.class));
        when(applicationOverviewIneligibilityModelPopulator.populateModel(application)).thenReturn(mock(ApplicationOverviewIneligibilityViewModel.class));
        when(projectService.getByApplicationId(application.getId())).thenReturn(null);
        when(interviewAssignmentRestService.isAssignedToInterview(application.getId())).thenReturn(restSuccess(false));

        FormInputResource appendix = newFormInputResource().build();
        FormInputResponseResource response = newFormInputResponseResource()
                .withFormInputs(singletonList(appendix.getId()))
                .withFileEntries(newFileEntryResource()
                .withName("Appendix1.pdf", "Appendix2.pdf")
                .withFilesizeBytes(1024L)
                .build(2))
                .build();
        when(formInputResponseRestService.getResponsesByApplicationId(application.getId())).thenReturn(restSuccess(singletonList(response)));
        when(formInputRestService.getById(appendix.getId())).thenReturn(restSuccess(appendix));

        ManagementApplicationViewModel actual = target.populate(application.getId(), user);

        assertEquals(application, actual.getApplication());
        assertEquals(competition, actual.getCompetition());
        assertEquals(2, actual.getAppendices().size());
        assertEquals("Appendix1.pdf", actual.getAppendices().get(0).getName());
        assertEquals("Appendix2.pdf", actual.getAppendices().get(1).getName());

        assertTrue(actual.isCanMarkAsIneligible());
        assertTrue(actual.isCanReinstate());
    }

    @Test
    public void populate_withInterviewFeedback() {
        CompetitionResource competition = newCompetitionResource()
                .withInnovationAreas(singleton(1L))
                .build();
        ApplicationResource application = newApplicationResource()
                .withCompetition(competition.getId())
                .withApplicationState(ApplicationState.OPENED)
                .withInnovationArea(newInnovationAreaResource().build())
                .withCompetitionStatus(ASSESSOR_FEEDBACK)
                .build();
        UserResource user = newUserResource()
                .withRoleGlobal(Role.COMP_ADMIN)
                .build();

        when(applicationRestService.getApplicationById(application.getId())).thenReturn(restSuccess(application));
        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competition));
        when(applicationReadOnlyViewModelPopulator.populate(application, competition, user, defaultSettings().setIncludeAllAssessorFeedback(true))).thenReturn(mock(ApplicationReadOnlyViewModel.class));
        when(applicationOverviewIneligibilityModelPopulator.populateModel(application)).thenReturn(mock(ApplicationOverviewIneligibilityViewModel.class));
        when(projectService.getByApplicationId(application.getId())).thenReturn(null);
        when(interviewAssignmentRestService.isAssignedToInterview(application.getId())).thenReturn(restSuccess(true));
        when(interviewFeedbackViewModelPopulator.populate(application.getId(), application.getCompetitionName(), user, application.getCompetitionStatus().isFeedbackReleased())).thenReturn(mock(InterviewFeedbackViewModel.class));

        FormInputResource appendix = newFormInputResource().build();
        FormInputResponseResource response = newFormInputResponseResource()
                .withFormInputs(singletonList(appendix.getId()))
                .withFileEntries(newFileEntryResource()
                        .withName("Appendix1.pdf", "Appendix2.pdf")
                        .withFilesizeBytes(1024L)
                        .build(2))
                .build();
        when(formInputResponseRestService.getResponsesByApplicationId(application.getId())).thenReturn(restSuccess(singletonList(response)));
        when(formInputRestService.getById(appendix.getId())).thenReturn(restSuccess(appendix));

        ManagementApplicationViewModel actual = target.populate(application.getId(), user);

        assertEquals(application, actual.getApplication());
        assertEquals(competition, actual.getCompetition());
        assertEquals(2, actual.getAppendices().size());
        assertEquals("Appendix1.pdf", actual.getAppendices().get(0).getName());
        assertEquals("Appendix2.pdf", actual.getAppendices().get(1).getName());

        assertFalse(actual.isCanMarkAsIneligible());
        assertTrue(actual.isCanReinstate());
    }
}
