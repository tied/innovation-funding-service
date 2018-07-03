package org.innovateuk.ifs.application.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.transactional.ApplicationResearchCategoryService;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationResearchCategoryControllerTest extends BaseControllerMockMVCTest<ApplicationResearchCategoryController> {

    @Mock
    private ApplicationResearchCategoryService applicationResearchCategoryServiceMock;

    @Override
    protected ApplicationResearchCategoryController supplyControllerUnderTest() {
        return new ApplicationResearchCategoryController();
    }

    @Test
    public void setResearchCategory() throws Exception {
        Long researchCategoryId = 1L;
        Long applicationId = 1L;

        when(applicationResearchCategoryServiceMock.setResearchCategory(applicationId, researchCategoryId)).thenReturn(serviceSuccess(newApplicationResource().build()));

        mockMvc.perform(post("/applicationResearchCategory/researchCategory/"+applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(researchCategoryId)))
                .andExpect(status().isOk());
    }
}