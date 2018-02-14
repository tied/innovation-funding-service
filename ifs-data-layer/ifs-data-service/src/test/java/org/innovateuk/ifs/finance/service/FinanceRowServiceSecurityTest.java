package org.innovateuk.ifs.finance.service;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.security.ApplicationLookupStrategy;
import org.innovateuk.ifs.application.security.ApplicationPermissionRules;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileAndContents;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.security.*;
import org.innovateuk.ifs.finance.transactional.FinanceRowService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.security.ProjectLookupStrategy;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.service.FinanceRowCostsServiceSecurityTest.TestFinanceRowCostsService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FinanceRowServiceSecurityTest extends BaseServiceSecurityTest<FinanceRowService> {

    private FinanceRowMetaFieldPermissionsRules financeRowMetaFieldPermissionsRules;
    private ApplicationFinanceRowPermissionRules costPermissionsRules;
    private ApplicationFinancePermissionRules applicationFinanceRules;
    private ApplicationPermissionRules applicationRules;
    private ApplicationLookupStrategy applicationLookupStrategy;
    private FinanceRowLookupStrategy financeRowLookupStrategy;
    private FinanceRowMetaFieldLookupStrategy financeRowMetaFieldLookupStrategy;
    private ApplicationFinanceLookupStrategy applicationFinanceLookupStrategy;

    private ProjectLookupStrategy projectLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        financeRowMetaFieldPermissionsRules = getMockPermissionRulesBean(FinanceRowMetaFieldPermissionsRules.class);
        costPermissionsRules = getMockPermissionRulesBean(ApplicationFinanceRowPermissionRules.class);
        applicationFinanceRules = getMockPermissionRulesBean(ApplicationFinancePermissionRules.class);
        applicationRules = getMockPermissionRulesBean(ApplicationPermissionRules.class);
        applicationLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ApplicationLookupStrategy.class);
        financeRowLookupStrategy = getMockPermissionEntityLookupStrategiesBean(FinanceRowLookupStrategy.class);
        financeRowMetaFieldLookupStrategy = getMockPermissionEntityLookupStrategiesBean(FinanceRowMetaFieldLookupStrategy.class);
        applicationFinanceLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ApplicationFinanceLookupStrategy.class);

        projectLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ProjectLookupStrategy.class);
    }

    @Test
    public void testFindApplicationFinanceByApplicationIdAndOrganisation() {
        final Long applicationId = 1L;
        final Long organisationId = 2L;
        assertAccessDenied(
                () -> classUnderTest.findApplicationFinanceByApplicationIdAndOrganisation(applicationId, organisationId),
                () -> verifyApplicationFinanceResourceReadRulesCalled()
        );
    }

    @Test
    public void testFindApplicationFinanceByApplication() {
        final Long applicationId = 1L;
        ServiceResult<List<ApplicationFinanceResource>> applicationFinanceByApplication = classUnderTest.findApplicationFinanceByApplication(applicationId);
        assertTrue(applicationFinanceByApplication.getSuccess().isEmpty());
        verifyApplicationFinanceResourceReadRulesCalled(ARRAY_SIZE_FOR_POST_FILTER_TESTS);
    }


    @Test
    public void testGetApplicationFinanceById() {
        final Long applicationId = 1L;
        assertAccessDenied(
                () -> classUnderTest.getApplicationFinanceById(applicationId),
                () -> verifyApplicationFinanceResourceReadRulesCalled()
        );
    }

    @Test
    public void testFinanceDetails() {
        final Long applicationId = 1L;
        final Long organisationId = 1L;
        assertAccessDenied(
                () -> classUnderTest.financeDetails(applicationId, organisationId),
                () -> verifyApplicationFinanceResourceReadRulesCalled()
        );
    }

    @Test
    public void testFinanceTotals() {
        final Long applicationId = 1L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());
        assertAccessDenied(
                () -> classUnderTest.financeTotals(applicationId),
                () -> {
                    verify(applicationRules).compAdminCanSeeApplicationFinancesTotals(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).consortiumCanSeeTheApplicationFinanceTotals(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).assessorCanSeeTheApplicationFinancesTotals(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).projectFinanceUserCanSeeApplicationFinancesTotals(isA(ApplicationResource.class), isA(UserResource.class));
                });
    }

    @Test
    public void testGetResearchParticipationPercentage() {
        final Long applicationId = 1L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().withId(applicationId).build());
        assertAccessDenied(
                () -> classUnderTest.getResearchParticipationPercentage(applicationId),
                () -> {
                    verify(applicationRules).assessorCanSeeTheResearchParticipantPercentageInApplicationsTheyAssess(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).internalUsersCanSeeTheResearchParticipantPercentageInApplications(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).consortiumCanSeeTheResearchParticipantPercentage(isA(ApplicationResource.class), isA(UserResource.class));
                });
    }

        @Test
    public void testDeleteFinanceFileEntry() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> classUnderTest.deleteFinanceFileEntry(applicationFinanceId),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanDeleteAFileForTheApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testCreateFinanceFileEntry() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> classUnderTest.createFinanceFileEntry(applicationFinanceId, null, null),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanCreateAFileForTheApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testUpdateFinanceFileEntry() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> classUnderTest.updateFinanceFileEntry(applicationFinanceId, null, null),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanUpdateAFileForTheApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }

    @Test
    public void testGetFileContents() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> classUnderTest.getFileContents(applicationFinanceId),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanGetFileEntryResourceByFinanceIdOfACollaborator(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                    verify(applicationFinanceRules).internalUserCanGetFileEntryResourceForFinanceIdOfACollaborator(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }

        @Test
    public void testOrganisationSeeksFunding() {
        final Long projectId = 1L;
        final Long applicationId = 1L;
        final Long organisationId = 1L;

        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(newProjectResource().withId(projectId).build());

        assertAccessDenied(
                () -> classUnderTest.organisationSeeksFunding(projectId, applicationId, organisationId),
                () -> {
                    verify(costPermissionsRules).projectPartnersCanCheckFundingStatusOfTeam(isA(ProjectResource.class), isA(UserResource.class));
                    verify(costPermissionsRules).projectPartnersCanCheckFundingStatusOfTeam(isA(ProjectResource.class), isA(UserResource.class));
                });
    }


    private void verifyApplicationFinanceResourceReadRulesCalled() {
        verifyApplicationFinanceResourceReadRulesCalled(1);
    }

    private void verifyApplicationFinanceResourceReadRulesCalled(int nTimes) {
        verify(applicationFinanceRules, times(nTimes)).consortiumCanSeeTheApplicationFinancesForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
        verify(applicationFinanceRules, times(nTimes)).assessorCanSeeTheApplicationFinanceForOrganisationsInApplicationsTheyAssess(isA(ApplicationFinanceResource.class), isA(UserResource.class));
        verify(applicationFinanceRules, times(nTimes)).internalUserCanSeeApplicationFinancesForOrganisations(isA(ApplicationFinanceResource.class), isA(UserResource.class));
    }

    @Override
    protected Class<FinanceRowServiceSecurityTest.TestFinanceRowService> getClassUnderTest() {
        return FinanceRowServiceSecurityTest.TestFinanceRowService.class;
    }

    public static class TestFinanceRowService implements FinanceRowService {

        @Override
        public ServiceResult<ApplicationFinanceResource> findApplicationFinanceByApplicationIdAndOrganisation(Long applicationId, Long organisationId) {
            return serviceSuccess(newApplicationFinanceResource().build());
        }

        @Override
        public ServiceResult<List<ApplicationFinanceResource>> findApplicationFinanceByApplication(Long applicationId) {
            return serviceSuccess(newApplicationFinanceResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<Double> getResearchParticipationPercentage(Long applicationId) {
            return null;
        }

        @Override
        public ServiceResult<Double> getResearchParticipationPercentageFromProject(@P("projectId") Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationFinanceResource> getApplicationFinanceById(Long applicationFinanceId) {
            return serviceSuccess(newApplicationFinanceResource().build());
        }


        @Override
        public ServiceResult<ApplicationFinanceResource> financeDetails(Long applicationId, Long organisationId) {
            return serviceSuccess(newApplicationFinanceResource().build());
        }

        @Override
        public ServiceResult<List<ApplicationFinanceResource>> financeDetails(Long applicationId) {
            return serviceSuccess(newApplicationFinanceResource().build(2));
        }

        @Override
        public ServiceResult<List<ApplicationFinanceResource>> financeTotals(Long applicationId) {
            return serviceSuccess(newApplicationFinanceResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<FileEntryResource> createFinanceFileEntry(long applicationFinanceId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> updateFinanceFileEntry(long applicationFinanceId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<Void> deleteFinanceFileEntry(long applicationFinanceId) {
            return null;
        }

        @Override
        public ServiceResult<FileAndContents> getFileContents(@P("applicationFinanceId") long applicationFinance) {
            return null;
        }

        @Override
        public ServiceResult<Boolean> organisationSeeksFunding(Long projectId, Long applicationId, Long organisationId) {
            return null;
        }
    }
}
