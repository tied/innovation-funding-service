package org.innovateuk.ifs.interview.controller;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.interview.form.InterviewAssignmentSelectionForm;
import org.innovateuk.ifs.interview.model.InterviewAssignmentApplicationsFindModelPopulator;
import org.innovateuk.ifs.interview.model.InterviewAssignmentApplicationsInviteModelPopulator;
import org.innovateuk.ifs.interview.viewmodel.*;
import org.innovateuk.ifs.invite.resource.AvailableApplicationPageResource;
import org.innovateuk.ifs.invite.resource.AvailableApplicationResource;
import org.innovateuk.ifs.invite.resource.InterviewAssignmentStagedApplicationPageResource;
import org.innovateuk.ifs.invite.resource.InterviewAssignmentStagedApplicationResource;
import org.innovateuk.ifs.management.form.InviteNewAssessorsForm;
import org.innovateuk.ifs.management.form.InviteNewAssessorsRowForm;
import org.innovateuk.ifs.management.model.AssessorProfileModelPopulator;
import org.innovateuk.ifs.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.is;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.IN_ASSESSMENT;
import static org.innovateuk.ifs.invite.builder.AvailableApplicationPageResourceBuilder.newAvailableApplicationPageResource;
import static org.innovateuk.ifs.invite.builder.AvailableApplicationResourceBuilder.newAvailableApplicationResource;
import static org.innovateuk.ifs.invite.builder.InterviewAssignmentCreatedInviteResourceBuilder.newInterviewAssignmentStagedApplicationResource;
import static org.innovateuk.ifs.invite.builder.InterviewAssignmentStagedApplicationPageResourceBuilder.newInterviewAssignmentStagedApplicationPageResource;
import static org.innovateuk.ifs.util.CollectionFunctions.asLinkedSet;
import static org.innovateuk.ifs.util.CollectionFunctions.forEachWithIndex;
import static org.innovateuk.ifs.util.CompressionUtil.getCompressedString;
import static org.innovateuk.ifs.util.CompressionUtil.getDecompressedString;
import static org.innovateuk.ifs.util.JsonUtil.getObjectFromJson;
import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class InterviewAssignmentApplicationsControllerTest extends BaseControllerMockMVCTest<InterviewAssignmentApplicationsController> {

    @Spy
    @InjectMocks
    private InterviewAssignmentApplicationsFindModelPopulator interviewAssignmentApplicationsFindModelPopulator;

    @Spy
    @InjectMocks
    private AssessorProfileModelPopulator assessorProfileModelPopulator;

    @Spy
    @InjectMocks
    private InterviewAssignmentApplicationsInviteModelPopulator interviewAssignmentApplicationsInviteModelPopulator;

    private CompetitionResource competition;

    @Override
    protected InterviewAssignmentApplicationsController supplyControllerUnderTest() {
        return new InterviewAssignmentApplicationsController();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        this.setupCookieUtil();

        competition = newCompetitionResource()
                .withCompetitionStatus(IN_ASSESSMENT)
                .withName("Technology inspired")
                .withInnovationSectorName("Infrastructure systems")
                .withInnovationAreaNames(asLinkedSet("Transport Systems", "Urban living"))
                .build();

        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competition));
    }

    @Test
    public void find() throws Exception {
        int page = 2;

        AvailableApplicationPageResource availableAssessorPageResource = newAvailableApplicationPageResource()
                .withContent(newAvailableApplicationResource().build(2))
                .build();

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(singletonList(1L)));
        when(interviewAssignmentRestService.getAvailableApplications(competition.getId(), page)).thenReturn(restSuccess(availableAssessorPageResource));

        MvcResult result = mockMvc.perform(get("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-panel-find"))
                .andReturn();

        InterviewAssignmentSelectionForm selectionForm = (InterviewAssignmentSelectionForm) result.getModelAndView().getModel().get("interviewAssignmentApplicationSelectionForm");
        assertTrue(selectionForm.getSelectedIds().isEmpty());

        assertCompetitionDetails(competition, result);
        assertAvailableApplications(availableAssessorPageResource.getContent(), result);

        InOrder inOrder = inOrder(competitionRestService, interviewAssignmentRestService);
        inOrder.verify(interviewAssignmentRestService).getAvailableApplicationIds(competition.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewAssignmentRestService).getAvailableApplications(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void find_defaultParams() throws Exception {
        int page = 0;

        AvailableApplicationPageResource availableAssessorPageResource = newAvailableApplicationPageResource()
                .withContent(newAvailableApplicationResource().build(2))
                .build();

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(singletonList(1L)));
        when(interviewAssignmentRestService.getAvailableApplications(competition.getId(), page)).thenReturn(restSuccess(availableAssessorPageResource));

        MvcResult result = mockMvc.perform(get("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-panel-find"))
                .andReturn();

        InterviewAssignmentSelectionForm selectionForm = (InterviewAssignmentSelectionForm) result.getModelAndView().getModel().get("interviewAssignmentApplicationSelectionForm");
        assertTrue(selectionForm.getSelectedIds().isEmpty());

        assertCompetitionDetails(competition, result);
        assertAvailableApplications(availableAssessorPageResource.getContent(), result);

        InOrder inOrder = inOrder(competitionRestService, interviewAssignmentRestService);
        inOrder.verify(interviewAssignmentRestService).getAvailableApplicationIds(competition.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewAssignmentRestService).getAvailableApplications(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void find_existingCookie() throws Exception {
        int page = 0;
        long expectedApplicationId = 1L;

        InterviewAssignmentSelectionForm expectedSelectionForm = new InterviewAssignmentSelectionForm();
        expectedSelectionForm.getSelectedIds().add(expectedApplicationId);
        Cookie selectionFormCookie = createFormCookie(expectedSelectionForm);

        AvailableApplicationPageResource availableAssessorPageResource = newAvailableApplicationPageResource()
                .withContent(newAvailableApplicationResource().build(2))
                .build();

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));
        when(interviewAssignmentRestService.getAvailableApplications(competition.getId(), page)).thenReturn(restSuccess(availableAssessorPageResource));

        MvcResult result = mockMvc.perform(get("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .cookie(selectionFormCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-panel-find"))
                .andReturn();

        InterviewAssignmentSelectionForm selectionForm = (InterviewAssignmentSelectionForm) result.getModelAndView().getModel().get("interviewAssignmentApplicationSelectionForm");
        assertEquals(expectedSelectionForm, selectionForm);

        Optional<InterviewAssignmentSelectionForm> resultForm = getInterviewAssignmentSelectionFormFromCookie(result.getResponse(), format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()));
        assertTrue(resultForm.get().getSelectedIds().contains(expectedApplicationId));

        assertCompetitionDetails(competition, result);
        assertAvailableApplications(availableAssessorPageResource.getContent(), result);

        InOrder inOrder = inOrder(competitionRestService, interviewAssignmentRestService);
        inOrder.verify(interviewAssignmentRestService).getAvailableApplicationIds(competition.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewAssignmentRestService).getAvailableApplications(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void addApplicationSelectionFromFindView() throws Exception {
        long applicationId = 1L;
        Cookie formCookie = createFormCookie(new InterviewAssignmentSelectionForm());

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .param("selectionId", valueOf(applicationId))
                .param("isSelected", "true")
                .param("page", "1")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(1)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewAssignmentSelectionForm> resultForm = getInterviewAssignmentSelectionFormFromCookie(result.getResponse(), format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()));
        assertTrue(resultForm.get().getSelectedIds().contains(applicationId));
    }

    @Test
    public void addApplicationSelectionFromFindView_defaultParams() throws Exception {
        long applicationId = 1L;
        Cookie formCookie = createFormCookie(new InterviewAssignmentSelectionForm());

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .param("selectionId", valueOf(applicationId))
                .param("isSelected", "true")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(1)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewAssignmentSelectionForm> resultForm = getInterviewAssignmentSelectionFormFromCookie(result.getResponse(), format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()));
        assertTrue(resultForm.get().getSelectedIds().contains(applicationId));
    }

    @Test
    public void addAllApplicationsFromFindView_defaultParams() throws Exception {
        Cookie formCookie = createFormCookie(new InterviewAssignmentSelectionForm());

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .param("addAll", "true")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(2)))
                .andExpect(jsonPath("allSelected", is(true)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewAssignmentSelectionForm> resultForm = getInterviewAssignmentSelectionFormFromCookie(result.getResponse(), format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()));
        assertEquals(2, resultForm.get().getSelectedIds().size());
    }

    @Test
    public void removeApplicationSelectionFromFindView() throws Exception {
        long applicationId = 1L;
        InterviewAssignmentSelectionForm form = new InterviewAssignmentSelectionForm();
        form.getSelectedIds().add(applicationId);
        Cookie formCookie = createFormCookie(form);

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .param("selectionId", valueOf(applicationId))
                .param("isSelected", "false")
                .param("page", "1")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(0)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewAssignmentSelectionForm> resultForm = getInterviewAssignmentSelectionFormFromCookie(result.getResponse(), format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()));
        assertFalse(resultForm.get().getSelectedIds().contains(applicationId));
    }

    @Test
    public void removeApplicationSelectionFromFindView_defaultParams() throws Exception {
        long applicationId = 1L;
        Cookie formCookie;
        InterviewAssignmentSelectionForm form = new InterviewAssignmentSelectionForm();
        form.getSelectedIds().add(applicationId);
        formCookie = createFormCookie(form);

        when(interviewAssignmentRestService.getAvailableApplicationIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview-panel/competition/{competitionId}/applications/find", competition.getId())
                .param("selectionId", valueOf(applicationId))
                .param("isSelected", "false")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(0)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewAssignmentSelectionForm> resultForm = getInterviewAssignmentSelectionFormFromCookie(result.getResponse(), format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()));
        assertFalse(resultForm.get().getSelectedIds().contains(applicationId));
    }

    @Test
    public void invite() throws Exception {
        int page = 0;

        List<InterviewAssignmentStagedApplicationResource> interviewAssignmentStagedApplicationResources = setUpApplicationCreatedInviteResources();
        InterviewAssignmentStagedApplicationPageResource interviewAssignmentStagedApplicationPageResource = newInterviewAssignmentStagedApplicationPageResource()
                .withContent(interviewAssignmentStagedApplicationResources)
                .build();

        setupDefaultInviteViewExpectations(page, interviewAssignmentStagedApplicationPageResource);

        InviteNewAssessorsForm expectedForm = new InviteNewAssessorsForm();
        expectedForm.setInvites(singletonList(new InviteNewAssessorsRowForm()));

        MvcResult result = mockMvc.perform(get("/assessment/interview-panel/competition/{competitionId}/applications/invite", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-panel-invite"))
                .andReturn();

        assertCompetitionDetails(competition, result);
        assertInvitedApplications(interviewAssignmentStagedApplicationResources, result);

        InOrder inOrder = inOrder(competitionRestService, interviewAssignmentRestService);
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewAssignmentRestService).getStagedApplications(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    private List<InterviewAssignmentStagedApplicationResource> setUpApplicationCreatedInviteResources() {
        return newInterviewAssignmentStagedApplicationResource()
                .withId(1L, 2L)
                .withApplicationId(3L, 4L)
                .withApplicationName("App 1", "App 2")
                .withLeadOrganisationName("Org 1", "Org 2")
                .build(2);
    }

    private void assertCompetitionDetails(CompetitionResource expectedCompetition, MvcResult result) {
        InterviewAssignmentApplicationsViewModel model = (InterviewAssignmentApplicationsViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedCompetition.getId(), (Long) model.getCompetitionId());
        assertEquals(expectedCompetition.getName(), model.getCompetitionName());
        assertInnovationSectorAndArea(expectedCompetition, model);
    }

    private void assertInnovationSectorAndArea(CompetitionResource expectedCompetition, InterviewAssignmentApplicationsViewModel model) {
        assertEquals(expectedCompetition.getInnovationSectorName(), model.getInnovationSector());
        assertEquals(StringUtils.join(expectedCompetition.getInnovationAreaNames(), ", "), model.getInnovationArea());
    }

    private void assertAvailableApplications(List<AvailableApplicationResource> expectedAvailableApplications, MvcResult result) {
        assertTrue(result.getModelAndView().getModel().get("model") instanceof InterviewAssignmentApplicationsFindViewModel);
        InterviewAssignmentApplicationsFindViewModel model = (InterviewAssignmentApplicationsFindViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedAvailableApplications.size(), model.getApplications().size());

        forEachWithIndex(expectedAvailableApplications, (i, availableApplicationResource) -> {
            InterviewAssignmentApplicationRowViewModel availableAssignmentRowViewModel = model.getApplications().get(i);
            assertEquals(availableApplicationResource.getName(), availableAssignmentRowViewModel.getName());
        });
    }

    private String formatInnovationAreas(List<InnovationAreaResource> innovationAreas) {
        return innovationAreas == null ? EMPTY : innovationAreas.stream()
                .map(CategoryResource::getName)
                .collect(joining(", "));
    }

    private void assertInvitedApplications(List<InterviewAssignmentStagedApplicationResource> expectedCreatedInvites, MvcResult result) {
        assertTrue(result.getModelAndView().getModel().get("model") instanceof InterviewAssignmentApplicationsInviteViewModel);
        InterviewAssignmentApplicationsInviteViewModel model = (InterviewAssignmentApplicationsInviteViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedCreatedInvites.size(), model.getApplications().size());

        forEachWithIndex(expectedCreatedInvites, (i, createdInviteResource) -> {
            InterviewAssignmentApplicationInviteRowViewModel invitedApplicationRowViewModel = model.getApplications().get(i);
            assertEquals(createdInviteResource.getApplicationName(), invitedApplicationRowViewModel.getApplicationName());
            assertEquals(createdInviteResource.getLeadOrganisationName(), invitedApplicationRowViewModel.getLeadOrganisation());
        });
    }

    private void setupDefaultInviteViewExpectations(int page,
                                                    InterviewAssignmentStagedApplicationPageResource interviewAssignmentStagedApplicationResource) {

        when(interviewAssignmentRestService.getStagedApplications(competition.getId(), page)).thenReturn(restSuccess(interviewAssignmentStagedApplicationResource));
    }

    private Cookie createFormCookie(InterviewAssignmentSelectionForm form) throws Exception {
        String cookieContent = JsonUtil.getSerializedObject(form);
        return new Cookie(format("interviewAssignmentApplicationSelectionForm_comp_%s", competition.getId()), getCompressedString(cookieContent));
    }

    private Optional<InterviewAssignmentSelectionForm> getInterviewAssignmentSelectionFormFromCookie(MockHttpServletResponse response, String cookieName) throws Exception {
        String value = getDecompressedString(response.getCookie(cookieName).getValue());
        String decodedFormJson  = URLDecoder.decode(value, CharEncoding.UTF_8);

        if (isNotBlank(decodedFormJson)) {
            return Optional.ofNullable(getObjectFromJson(decodedFormJson, InterviewAssignmentSelectionForm.class));
        } else {
            return Optional.empty();
        }
    }
}