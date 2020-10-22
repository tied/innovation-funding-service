package org.innovateuk.ifs.cofunder.service;

import org.innovateuk.ifs.cofunder.resource.AssessorDashboardState;
import org.innovateuk.ifs.cofunder.resource.CofunderDashboardCompetitionResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.cofunder.resource.CofunderDashboardApplicationPageResource;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;
import java.util.Map;

public interface CofunderDashboardRestService {
    RestResult<Map<AssessorDashboardState, List<CofunderDashboardCompetitionResource>>> getCofunderCompetitionDashboard(long userId);
    RestResult<CofunderDashboardApplicationPageResource> getCofunderCompetitionDashboardApplications(@PathVariable long userId, @PathVariable long competitionId, int page);
}