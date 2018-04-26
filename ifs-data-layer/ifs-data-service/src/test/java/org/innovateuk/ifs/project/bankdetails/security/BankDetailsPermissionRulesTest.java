package org.innovateuk.ifs.project.bankdetails.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.project.bankdetails.resource.BankDetailsResource;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.domain.ProjectProcess;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.project.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.builder.ProjectProcessBuilder.newProjectProcess;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.workflow.domain.ActivityType.PROJECT_SETUP;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BankDetailsPermissionRulesTest extends BasePermissionRulesTest<BankDetailsPermissionRules> {

    @Override
    protected BankDetailsPermissionRules supplyPermissionRulesUnderTest() {
        return new BankDetailsPermissionRules();
    }

    private UserResource user;
    private UserResource projectFinanceUser;
    private ProjectResource project;
    private Project projectProcessProject;
    private List<ProjectUser> partnerProjectUser;
    private OrganisationResource organisationResource;
    private BankDetailsResource bankDetailsResource;
    private ProjectProcess projectProcess;

    @Before
    public void setUp(){
        user = newUserResource().build();
        project = newProjectResource().build();
        projectProcessProject = newProject().build();
        projectFinanceUser = newUserResource().withRolesGlobal(singletonList(Role.PROJECT_FINANCE)).build();
        partnerProjectUser = newProjectUser().build(1);
        organisationResource = newOrganisationResource().build();
        bankDetailsResource = newBankDetailsResource().withOrganisation(organisationResource.getId()).withProject(project.getId()).build();

        projectProcess = newProjectProcess()
                .withProject(projectProcessProject)
                .withActivityState(new ActivityState(PROJECT_SETUP, ProjectState.SETUP.getBackingState()))
                .build();
    }

    @Test
    public void testPartnersCanSeeBankDetailsOfTheirOwnOrg() {
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(partnerProjectUser);
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(project.getId(), user.getId(), organisationResource.getId(), PROJECT_PARTNER)).thenReturn(partnerProjectUser.get(0));
        assertTrue(rules.partnersCanSeeTheirOwnOrganisationsBankDetails(bankDetailsResource, user));
    }

    @Test
    public void testNonPartnersCannotSeeBankDetails() {
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(Collections.emptyList());
        assertFalse(rules.partnersCanSeeTheirOwnOrganisationsBankDetails(bankDetailsResource, user));
    }

    @Test
    public void testPartnersCannotSeeBankDetailsOfAnotherOrganisation() {
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(partnerProjectUser);
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(project.getId(), user.getId(), organisationResource.getId(), PROJECT_PARTNER)).thenReturn(null);
        assertFalse(rules.partnersCanSeeTheirOwnOrganisationsBankDetails(bankDetailsResource, user));
    }

    @Test
    public void testPartnersCanUpdateTheirOwnOrganisationBankDetails(){
        when(projectProcessRepositoryMock.findOneByTargetId(project.getId())).thenReturn(projectProcess);
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(partnerProjectUser);
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(project.getId(), user.getId(), organisationResource.getId(), PROJECT_PARTNER)).thenReturn(partnerProjectUser.get(0));
        assertTrue(rules.partnersCanSubmitTheirOwnOrganisationsBankDetails(bankDetailsResource, user));
    }

    @Test
    public void testNonPartnersCannotUpdateBankDetails(){
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(Collections.emptyList());
        assertFalse(rules.partnersCanSubmitTheirOwnOrganisationsBankDetails(bankDetailsResource, user));
    }

    @Test
    public void testPartnersCannotUpdateBankDetailsOfAnotherOrg(){
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(partnerProjectUser);
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(project.getId(), user.getId(), organisationResource.getId(), PROJECT_PARTNER)).thenReturn(null);
        assertFalse(rules.partnersCanSubmitTheirOwnOrganisationsBankDetails(bankDetailsResource, user));
    }

    @Test
    public void testProjectFinanceUserCanSeeAllBankDetailsForAllOrganisations(){
        assertTrue(rules.projectFinanceUsersCanSeeAllBankDetailsOnAllProjects(bankDetailsResource, projectFinanceUser));
    }

    @Test
    public void testProjectFinanceUserCanUpdateBankDetailsForAllOrganisations(){
        when(projectProcessRepositoryMock.findOneByTargetId(project.getId())).thenReturn(projectProcess);
        assertTrue(rules.projectFinanceUsersCanUpdateAnyOrganisationsBankDetails(bankDetailsResource, projectFinanceUser));
    }
}
