package org.innovateuk.ifs.application.areas.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.areas.populator.ApplicationResearchCategoryModelPopulator;
import org.innovateuk.ifs.application.areas.viewmodel.ResearchCategoryViewModel;
import org.innovateuk.ifs.application.forms.validator.ApplicationDetailsEditableValidator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationInnovationAreaRestService;
import org.innovateuk.ifs.application.service.ApplicationResearchCategoryRestService;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.APPLICATION_BASE_URL;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ResearchCategoryControllerTest extends BaseControllerMockMVCTest<ResearchCategoryController> {
    @Override
    protected ResearchCategoryController supplyControllerUnderTest() {
        return new ResearchCategoryController();
    }

    @Mock
    private ApplicationResearchCategoryModelPopulator applicationInnovationAreaPopulator;

    @Mock
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Mock
    private ApplicationDetailsEditableValidator applicationDetailsEditableValidator;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationResearchCategoryRestService applicationResearchCategoryRestService;

    @Mock
    private ApplicationInnovationAreaRestService applicationInnovationAreaRestService;

    @Test
    public void getInnovationAreas() throws Exception {
        Long applicationId = 1L;
        Long questionId = 2L;
        Long loggedInUserId = 3L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ResearchCategoryViewModel researchCategoryViewModel = new ResearchCategoryViewModel(
                anyString(),
                applicationId,
                questionId,
                anyList(),
                false
        );

        when(applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)).thenReturn(true);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());
        when(applicationInnovationAreaPopulator.populate(applicationResource, questionId, loggedInUserId)).thenReturn(researchCategoryViewModel);

        mockMvc.perform(get(APPLICATION_BASE_URL + "1/form/question/2/research-category"))
                .andExpect(view().name("application/research-categories"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        verify(applicationInnovationAreaPopulator).populate(any(), any(), any());
    }

    @Test
    public void submitInnovationAreaChoice_applicableChoiceShouldCallServiceAndRedirectToApplicationDetails() throws Exception {
        Long applicationId = 1L;
        Long questionId = 2L;
        Long innovationAreaId = 3L;
        Long loggedInUserId = 4L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();

        ResearchCategoryViewModel researchCategoryViewModel = new ResearchCategoryViewModel(
                anyString(),
                applicationId,
                questionId,
                anyList(),
                false
        );

        when(applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)).thenReturn(true);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());
        when(applicationInnovationAreaPopulator.populate(applicationResource, questionId, loggedInUserId)).thenReturn(researchCategoryViewModel);
        when(applicationResearchCategoryRestService.saveApplicationResearchCategoryChoice(applicationId, innovationAreaId)).thenReturn(restSuccess(newApplicationResource().build()));


        mockMvc.perform(post(APPLICATION_BASE_URL + "1/form/question/2/research-category")
                .param("researchCategoryChoice", innovationAreaId.toString()))
                .andExpect(view().name("redirect:/application/1/form/question/2"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        verify(applicationInnovationAreaPopulator).populate(any(), any(), any());
        verify(cookieFlashMessageFilter).setFlashMessage(any(), any());
        verify(applicationResearchCategoryRestService).saveApplicationResearchCategoryChoice(applicationId, innovationAreaId);
    }

    @Test
    public void submitInnovationAreaChoice_restServiceErrorShouldResultInErrorOnInnovationAreasPage() throws Exception {
        Long applicationId = 1L;
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        Long questionId = 2L;
        Long loggedInUserId = 4L;

        Long nonExistentInnovationAreaId = 3L;

        RestResult<ApplicationResource> result = restFailure(new Error("", HttpStatus.NOT_FOUND));

        ResearchCategoryViewModel researchCategoryViewModel = new ResearchCategoryViewModel(
                anyString(),
                applicationId,
                questionId,
                anyList(),
                false
        );

        when(applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)).thenReturn(true);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());
        when(applicationInnovationAreaPopulator.populate(applicationResource, questionId, loggedInUserId)).thenReturn(researchCategoryViewModel);
        when(applicationResearchCategoryRestService.saveApplicationResearchCategoryChoice(applicationId, nonExistentInnovationAreaId)).thenReturn(result);

        mockMvc.perform(post(APPLICATION_BASE_URL + "1/form/question/2/research-category")
                .param("researchCategoryChoice", nonExistentInnovationAreaId.toString()))
                .andExpect(view().name("application/research-categories"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        verify(applicationInnovationAreaPopulator).populate(any(), any(), any());
        verifyZeroInteractions(cookieFlashMessageFilter);
    }

    @Test
    public void submitInnovationAreaChoice_missingChoiceShouldThrowError() throws Exception {
        Long applicationId = 1L;
        Long questionId = 2L;
        Long loggedInUserId = 3L;
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();


        ResearchCategoryViewModel researchCategoryViewModel = new ResearchCategoryViewModel(
                anyString(),
                applicationId,
                questionId,
                anyList(),
                false
        );

        when(applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)).thenReturn(true);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());
        when(applicationInnovationAreaPopulator.populate(applicationResource, questionId, loggedInUserId)).thenReturn(researchCategoryViewModel);

        mockMvc.perform(post(APPLICATION_BASE_URL + "1/form/question/2/research-category"))
                .andExpect(view().name("application/research-categories"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "researchCategoryChoice"));

        verify(applicationInnovationAreaPopulator).populate(any(), any(), any());
        verifyZeroInteractions(applicationInnovationAreaRestService);
        verifyZeroInteractions(cookieFlashMessageFilter);
    }

    @Test
    public void submitInnovationAreaChoice_validatorReturnFalseShouldResultInForbiddenView() throws Exception {
        Long applicationId = 1L;
        Long questionId = 2L;
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();

        when(applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)).thenReturn(false);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());

        mockMvc.perform(post(APPLICATION_BASE_URL + "1/form/question/2/research-category"))
                .andExpect(view().name("forbidden"))
                .andExpect(status().is4xxClientError())
                .andReturn();

        verifyZeroInteractions(applicationInnovationAreaPopulator);
        verifyZeroInteractions(applicationInnovationAreaRestService);
        verifyZeroInteractions(cookieFlashMessageFilter);
    }
}