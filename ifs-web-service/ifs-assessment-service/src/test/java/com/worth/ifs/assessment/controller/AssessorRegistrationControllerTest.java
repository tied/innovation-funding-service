package com.worth.ifs.assessment.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.assessment.model.AssessorRegistrationBecomeAnAssessorModelPopulator;
import com.worth.ifs.assessment.model.AssessorRegistrationModelPopulator;
import com.worth.ifs.assessment.viewmodel.AssessorRegistrationBecomeAnAssessorViewModel;
import com.worth.ifs.assessment.viewmodel.AssessorRegistrationViewModel;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.invite.resource.CompetitionInviteResource;
import com.worth.ifs.invite.service.EthnicityRestService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static com.worth.ifs.assessment.builder.CompetitionInviteResourceBuilder.newCompetitionInviteResource;
import static com.worth.ifs.invite.builder.EthnicityResourceBuilder.newEthnicityResource;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class AssessorRegistrationControllerTest extends BaseControllerMockMVCTest<AssessorRegistrationController> {

    @Spy
    @InjectMocks
    private AssessorRegistrationModelPopulator registrationModelPopulator;

    @Spy
    @InjectMocks
    private AssessorRegistrationBecomeAnAssessorModelPopulator becomeAnAssessorModelPopulator;

    @Mock
    private EthnicityRestService ethnicityRestService;

    @Override
    protected AssessorRegistrationController supplyControllerUnderTest() {
        return new AssessorRegistrationController();
    }

    @Test
    public void becomeAnAssessor() throws Exception {
        CompetitionInviteResource competitionInviteResource = newCompetitionInviteResource().build();

        when(competitionInviteRestService.getInvite("hash")).thenReturn(RestResult.restSuccess(competitionInviteResource));

        AssessorRegistrationBecomeAnAssessorViewModel expectedViewModel = new AssessorRegistrationBecomeAnAssessorViewModel("hash");

        mockMvc.perform(get("/registration/{inviteHash}/start", "hash"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("registration/become-assessor"));
    }

    @Test
    public void registerAssessor() throws Exception {
        CompetitionInviteResource competitionInviteResource = newCompetitionInviteResource().withEmail("test@test.com").build();

        when(competitionInviteRestService.getInvite("hash")).thenReturn(RestResult.restSuccess(competitionInviteResource));
        when(ethnicityRestService.findAllActive()).thenReturn(RestResult.restSuccess(asList(newEthnicityResource())));
        AssessorRegistrationViewModel expectedViewModel = new AssessorRegistrationViewModel("hash", "test@test.com");

        mockMvc.perform(get("/registration/{inviteHash}/register", "hash"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("registration/register"));
    }

}