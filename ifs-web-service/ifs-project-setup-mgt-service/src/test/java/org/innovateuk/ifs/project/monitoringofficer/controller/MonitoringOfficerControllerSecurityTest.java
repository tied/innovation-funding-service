package org.innovateuk.ifs.project.monitoringofficer.controller;

import org.innovateuk.ifs.project.BaseProjectSetupControllerSecurityTest;
import org.innovateuk.ifs.project.resource.ProjectCompositeId;
import org.innovateuk.ifs.project.security.ProjectLookupStrategy;
import org.innovateuk.ifs.project.status.security.SetupSectionsPermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

public class MonitoringOfficerControllerSecurityTest extends BaseProjectSetupControllerSecurityTest<MonitoringOfficerController> {


    private ProjectLookupStrategy projectLookupStrategy;
    private ProjectCompositeId projectCompositeId;

    @Override
    @Before
    public void lookupPermissionRules() {
        super.lookupPermissionRules();
        projectLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ProjectLookupStrategy.class);
        projectCompositeId = ProjectCompositeId.id(123l);
        when(projectLookupStrategy.getProjectCompositeId(projectCompositeId.id())).thenReturn(projectCompositeId);
    }

    @Override
    protected Class<? extends MonitoringOfficerController> getClassUnderTest() {
        return MonitoringOfficerController.class;
    }

    @Test
    public void testViewMonitoringOfficer() {
        assertSecured(() -> classUnderTest.viewMonitoringOfficer(null, projectCompositeId.id(), null));
    }

    @Override
    protected Consumer<SetupSectionsPermissionRules> getVerification() {
        return permissionRules -> permissionRules.internalCanAccessMonitoringOfficerSection(eq(projectCompositeId), isA(UserResource.class));
    }
}
