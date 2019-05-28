package org.innovateuk.ifs.project.projectmonitoringofficer.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.project.monitoring.service.MonitoringOfficerRestServiceImpl;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectResourceListType;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.userListType;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

public class MonitoringOfficerRestServiceImplTest extends BaseRestServiceUnitTest<MonitoringOfficerRestServiceImpl> {

    @Test
    public void findAll() {
        List<UserResource> expected = singletonList(new UserResource());
        setupGetWithRestResultExpectations("/monitoring-officer/find-all", userListType(), expected, OK);

        RestResult<List<UserResource>> result = service.findAll();

        assertTrue(result.isSuccess());
        assertEquals(result.getSuccess(), expected);
    }

    @Test
    public void getProjectsForMonitoringOfficer() {
        long userId = 1L;
        List<ProjectResource> expected = newProjectResource().build(1);
        setupGetWithRestResultExpectations("/monitoring-officer/1/projects", projectResourceListType(), expected, OK);

        RestResult<List<ProjectResource>> result = service.getProjectsForMonitoringOfficer(userId);

        assertTrue(result.isSuccess());
        assertEquals(result.getSuccess(), expected);
    }

    @Override
    protected MonitoringOfficerRestServiceImpl registerRestServiceUnderTest() {
        return new MonitoringOfficerRestServiceImpl();
    }
}
