package org.innovateuk.ifs.cofunder.controller;

import org.innovateuk.ifs.cofunder.resource.AssessorDashboardState;
import org.innovateuk.ifs.cofunder.resource.CofunderDashboardCompetitionResource;
import org.innovateuk.ifs.cofunder.transactional.CofunderDashboardService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.innovateuk.ifs.cofunder.resource.CofunderDashboardApplicationPageResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cofunder/dashboard")
public class CofunderDashboardController {
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private CofunderDashboardService cofunderDashboardService;

    @GetMapping("/user/{userId}/dashboard")
    public RestResult<Map<AssessorDashboardState, List<CofunderDashboardCompetitionResource>>> getCofunderDashboard(@PathVariable long userId) {
        return cofunderDashboardService.getCompetitionsForCofunding(userId).toGetResponse();
    }

    @GetMapping("/user/{userId}/competition/{competitionId}")
    public RestResult<CofunderDashboardApplicationPageResource> getCofunderDashboard(@PathVariable long userId, @PathVariable long competitionId, @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = {"activityState"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return cofunderDashboardService.getApplicationsForCofunding(userId, competitionId, pageable).toGetResponse();
    }
}
