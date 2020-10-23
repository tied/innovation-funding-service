package org.innovateuk.ifs;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.supporter.repository.SupporterAssignmentRepository;
import org.innovateuk.ifs.competition.mapper.ExternalFinanceRepository;
import org.innovateuk.ifs.competition.repository.StakeholderRepository;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.monitoring.repository.MonitoringOfficerRepository;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.core.domain.ProjectParticipantRole.*;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.resource.Role.COMP_ADMIN;
import static org.mockito.Mockito.when;

/**
 * A base class for testing @PermissionRules-annotated classes
 */
public abstract class BasePermissionRulesTest<T> extends RootPermissionRulesTest<T> {

    @InjectMocks
    protected T rules = supplyPermissionRulesUnderTest();

    @Mock
    protected ProjectUserRepository projectUserRepository;

    @Mock
    protected MonitoringOfficerRepository projectMonitoringOfficerRepository;

    @Mock
    protected ProjectRepository projectRepository;

    @Mock
    protected ProcessRoleRepository processRoleRepository;

    @Mock
    protected OrganisationRepository organisationRepository;

    @Mock
    protected StakeholderRepository stakeholderRepository;

    @Mock
    protected ExternalFinanceRepository externalFinanceRepository;

    @Mock
    protected SupporterAssignmentRepository supporterAssignmentRepository;

    protected void setUpUserAsProjectManager(ProjectResource projectResource, UserResource user) {

        List<ProjectUser> projectManagerUser = newProjectUser().build(1);

        when(projectUserRepository.findByProjectIdAndUserIdAndRole(projectResource.getId(), user.getId(), PROJECT_MANAGER))
                .thenReturn(projectManagerUser);
    }

    protected void setUpUserNotAsProjectManager(UserResource user) {
        List<Role> projectManagerUser = emptyList();
        user.setRoles(projectManagerUser);
    }

    protected void setupUserAsPartner(ProjectResource project, UserResource user) {
        setupPartnerExpectations(project, user, true);
    }

    protected void setupUserAsPartner(ProjectResource project, UserResource user, OrganisationResource organisation) {
        setupPartnerExpectations(project, user, organisation, true);
    }

    protected void setupUserNotAsPartner(ProjectResource project, UserResource user) {
        setupPartnerExpectations(project, user, false);
    }

    protected void setupUserNotAsPartner(ProjectResource project, UserResource user, OrganisationResource organisation) {
        setupPartnerExpectations(project, user, organisation, false);
    }

    protected void setupUserAsMonitoringOfficer(ProjectResource project, UserResource user) {
        setupMonitoringOfficerExpectations(project, user, true);
    }

    protected void setupUserNotAsMonitoringOfficer(ProjectResource project, UserResource user) {
        setupMonitoringOfficerExpectations(project, user, false);
    }

    protected void setUpUserAsCompAdmin(ProjectResource project, UserResource user) {
        List<Role> compAdminRoleResource = singletonList(COMP_ADMIN);
        user.setRoles(compAdminRoleResource);
    }

    protected void setUpUserNotAsCompAdmin(ProjectResource project, UserResource user) {
        List<Role> compAdminRoleResource = emptyList();
        user.setRoles(compAdminRoleResource);
    }

    protected void setUpUserAsProjectFinanceUser(ProjectResource project, UserResource user) {
        List<Role> projectFinanceUser = singletonList(Role.PROJECT_FINANCE);
        user.setRoles(projectFinanceUser);
    }

    protected void setUpUserNotAsProjectFinanceUser(ProjectResource project, UserResource user) {
        List<Role> projectFinanceUser = emptyList();
        user.setRoles(projectFinanceUser);
    }

    protected void setUpUserAsSupport(ProjectResource project, UserResource user) {
        List<Role> supportUser = singletonList(Role.SUPPORT);
        user.setRoles(supportUser);
    }

    protected void setUpUserNotAsSupport(ProjectResource project, UserResource user) {
        List<Role> supportUser = emptyList();
        user.setRoles(supportUser);
    }

    protected void setupPartnerExpectations(ProjectResource project, UserResource user, boolean userIsPartner) {
        List<ProjectUser> partnerProjectUser = newProjectUser().build(1);

        when(projectUserRepository.findByProjectIdAndUserIdAndRoleIsIn(project.getId(), user.getId(), PROJECT_USER_ROLES.stream().collect(Collectors.toList()))).thenReturn(userIsPartner ? partnerProjectUser : emptyList());
    }

    protected void setupPartnerExpectations(ProjectResource project, UserResource user, OrganisationResource organisation, boolean userIsPartner) {
        ProjectUser partnerProjectUser = newProjectUser().build();

        when(projectUserRepository.findFirstByProjectIdAndUserIdAndOrganisationIdAndRoleIn(project.getId(), user.getId(), organisation.getId(), PROJECT_USER_ROLES.stream().collect(Collectors.toList()))).thenReturn(userIsPartner ? partnerProjectUser : null);
    }

    protected void setupMonitoringOfficerExpectations(ProjectResource project, UserResource user, boolean userIsMonitoringOfficer) {
        when(projectMonitoringOfficerRepository.existsByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(userIsMonitoringOfficer);
    }

    protected void setupUserAsLeadPartner(ProjectResource project, UserResource user) {
        setupLeadPartnerExpectations(project, user, true);
    }

    protected void setupUserNotAsLeadPartner(ProjectResource project, UserResource user) {
        setupLeadPartnerExpectations(project, user, false);
    }

    protected void setupSupporterAssignmentExpectations(Long applicationId, Long userId, boolean userIsSupporter) {
        when(supporterAssignmentRepository.existsByParticipantIdAndTargetId(userId, applicationId))
                .thenReturn(userIsSupporter);
    }

    protected void setupSupporterAssignmentCompetitionExpectations(Long competitionId, Long userId, boolean userIsSupporter) {
        when(supporterAssignmentRepository.existsByParticipantIdAndCompetitionId(userId, competitionId))
                .thenReturn(userIsSupporter);
    }

    private void setupLeadPartnerExpectations(ProjectResource project, UserResource user, boolean userIsLeadPartner) {

        org.innovateuk.ifs.application.domain.Application originalApplication = newApplication().build();
        Project projectEntity = newProject().withApplication(originalApplication).build();
        Organisation leadOrganisation = newOrganisation().build();
        ProcessRole leadApplicantProcessRole = newProcessRole().withOrganisationId(leadOrganisation.getId()).build();

        // find the lead organisation
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(projectEntity));
        when(processRoleRepository.findOneByApplicationIdAndRole(projectEntity.getApplication().getId(), Role.LEADAPPLICANT)).thenReturn(leadApplicantProcessRole);

        // see if the user is a partner on the lead organisation
        when(organisationRepository.findById(leadOrganisation.getId())).thenReturn(Optional.of(leadOrganisation));
        when(projectUserRepository.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(
                project.getId(), user.getId(), leadOrganisation.getId(), PROJECT_PARTNER)).thenReturn(userIsLeadPartner ? newProjectUser().build() : null);
    }

    protected abstract T supplyPermissionRulesUnderTest();

}
