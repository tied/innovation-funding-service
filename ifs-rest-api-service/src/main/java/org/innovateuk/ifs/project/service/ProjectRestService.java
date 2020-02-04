package org.innovateuk.ifs.project.service;

import java.util.List;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;

public interface ProjectRestService {
    RestResult<ProjectResource> getProjectById(long projectId);

    RestResult<List<ProjectResource>> findByUserId(long userId);

    RestResult<List<ProjectUserResource>> getProjectUsersForProject(long projectId);

    RestResult<ProjectResource> getByApplicationId(long applicationId);

    RestResult<OrganisationResource> getOrganisationByProjectAndUser(long projectId, long userId);

    RestResult<ProjectUserResource> getProjectManager(long projectId);

    RestResult<ProjectResource> createProjectFromApplicationId(long applicationId);

    RestResult<OrganisationResource> getLeadOrganisationByProject(long projectId);

    RestResult<Boolean> existsOnApplication(long projectId, long organisationId);
}