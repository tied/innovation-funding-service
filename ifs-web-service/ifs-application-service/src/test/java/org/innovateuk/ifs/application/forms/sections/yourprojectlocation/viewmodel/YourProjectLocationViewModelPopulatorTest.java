package org.innovateuk.ifs.application.forms.sections.yourprojectlocation.viewmodel;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class YourProjectLocationViewModelPopulatorTest {

    private static long applicationId = 9876L;
    private static long organisationId = 5432L;
    private static long sectionId = 1234L;

    private static Consumer<YourProjectLocationViewModel> expectViewModelIsComplete = model -> assertThat(model.isComplete()).isTrue();
    private static Consumer<YourProjectLocationViewModel> expectViewModelIsOpen = model -> assertThat(model.isOpen()).isTrue();
    private static Consumer<YourProjectLocationViewModel> expectViewModelIsReadonly = model -> assertThat(model.isReadOnly()).isTrue();

    private static Consumer<YourProjectLocationViewModel> expectViewModelIsIncomplete = model -> assertThat(model.isComplete()).isFalse();
    private static Consumer<YourProjectLocationViewModel> expectViewModelIsClosed = model -> assertThat(model.isOpen()).isFalse();
    private static Consumer<YourProjectLocationViewModel> expectViewModelIsEditable = model -> assertThat(model.isReadOnly()).isFalse();

    private static Consumer<YourProjectLocationViewModel> expectedExternalUserFinanceUrl = model ->
            assertThat(model.getFinancesUrl()).isEqualTo("/application/" + applicationId + "/form/FINANCE");

    private static Consumer<YourProjectLocationViewModel> expectedInternalUserFinanceUrl = model ->
            assertThat(model.getFinancesUrl()).isEqualTo("/application/" + applicationId + "/form/FINANCE/" + organisationId);

    @InjectMocks
    private YourProjectLocationViewModelPopulator populator;

    @Mock
    private ApplicationRestService applicationRestService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private SectionService sectionService;

    @Test
    public void populate() {

        boolean internalUser = false;
        List<Long> sectionsMarkedAsComplete = asList(111L, 333L);
        ApplicationState applicationState = ApplicationState.OPEN;
        CompetitionStatus competitionState = CompetitionStatus.OPEN;

        assertViewModelPopulatedOk(
                internalUser,
                sectionsMarkedAsComplete,
                applicationState,
                competitionState,
                expectViewModelIsIncomplete,
                expectViewModelIsOpen,
                expectViewModelIsEditable,
                expectedExternalUserFinanceUrl);
    }

    @Test
    public void populateComplete() {

        boolean internalUser = false;
        List<Long> sectionsMarkedAsComplete = asList(111L, sectionId, 333L);
        ApplicationState applicationState = ApplicationState.OPEN;
        CompetitionStatus competitionState = CompetitionStatus.OPEN;

        assertViewModelPopulatedOk(
                internalUser,
                sectionsMarkedAsComplete,
                applicationState,
                competitionState,
                expectViewModelIsComplete,
                expectViewModelIsOpen,
                expectViewModelIsReadonly,
                expectedExternalUserFinanceUrl);
    }

    @Test
    public void populateCompetitionClosed() {

        boolean internalUser = false;
        List<Long> sectionsMarkedAsComplete = asList(111L, 333L);
        ApplicationState applicationState = ApplicationState.OPEN;
        CompetitionStatus competitionState = CompetitionStatus.CLOSED;

        assertViewModelPopulatedOk(
                internalUser,
                sectionsMarkedAsComplete,
                applicationState,
                competitionState,
                expectViewModelIsIncomplete,
                expectViewModelIsClosed,
                expectViewModelIsReadonly,
                expectedExternalUserFinanceUrl);
    }

    @Test
    public void populateApplicationClosed() {

        boolean internalUser = false;
        List<Long> sectionsMarkedAsComplete = asList(111L, 333L);
        ApplicationState applicationState = ApplicationState.SUBMITTED;
        CompetitionStatus competitionState = CompetitionStatus.OPEN;

        assertViewModelPopulatedOk(
                internalUser,
                sectionsMarkedAsComplete,
                applicationState,
                competitionState,
                expectViewModelIsIncomplete,
                expectViewModelIsClosed,
                expectViewModelIsReadonly,
                expectedExternalUserFinanceUrl);
    }

    @Test
    public void populateForInternalUser() {

        boolean internalUser = true;
        List<Long> sectionsMarkedAsComplete = asList(111L, 333L);
        ApplicationState applicationState = ApplicationState.OPEN;
        CompetitionStatus competitionState = CompetitionStatus.OPEN;

        assertViewModelPopulatedOk(
                internalUser,
                sectionsMarkedAsComplete,
                applicationState,
                competitionState,
                expectViewModelIsIncomplete,
                expectViewModelIsClosed,
                expectViewModelIsReadonly,
                expectedInternalUserFinanceUrl);
    }

    private void assertViewModelPopulatedOk(
            boolean internalUser,
            List<Long> sectionsMarkedAsComplete,
            ApplicationState applicationState,
            CompetitionStatus competitionStatus,
            Consumer<YourProjectLocationViewModel>... conditionalAssertions) {

        CompetitionResource competition = newCompetitionResource().
                withCompetitionStatus(competitionStatus).
                build();

        ApplicationResource application = newApplicationResource().
                withId(applicationId).
                withCompetition(competition.getId()).
                withName("Lovely application").
                withApplicationState(applicationState).
                build();

        when(applicationRestService.getApplicationById(application.getId())).thenReturn(restSuccess(application));
        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competition));
        when(sectionService.getCompleted(application.getId(), organisationId)).thenReturn(sectionsMarkedAsComplete);

        YourProjectLocationViewModel viewModel = populator.populate(organisationId, application.getId(), sectionId, internalUser);

        assertThat(viewModel.getApplicationId()).isEqualTo(application.getId());
        assertThat(viewModel.getApplicationName()).isEqualTo(application.getName());
        assertThat(viewModel.getSectionId()).isEqualTo(sectionId);

        asList(conditionalAssertions).forEach(p -> p.accept(viewModel));
    }
}
