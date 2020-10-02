package org.innovateuk.ifs.cofunder.controller;

import org.innovateuk.ifs.cofunder.resource.ApplicationsForCofundingPageResource;
import org.innovateuk.ifs.cofunder.resource.CofunderAssignmentResource;
import org.innovateuk.ifs.cofunder.resource.CofunderDecisionResource;
import org.innovateuk.ifs.cofunder.resource.CofundersAvailableForApplicationPageResource;
import org.innovateuk.ifs.cofunder.transactional.CofunderAssignmentService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes CRUD operations through a REST API to manage {@link org.innovateuk.ifs.assessment.domain.Assessment} related data.
 */
@RestController
@RequestMapping("/cofunder")
public class CofunderAssignmentController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private CofunderAssignmentService cofunderAssignmentService;

    @GetMapping("/user/{userId}/application/{applicationId}")
    public RestResult<CofunderAssignmentResource> getAssignment(@PathVariable long userId, @PathVariable long applicationId) {
        return cofunderAssignmentService.getAssignment(userId, applicationId).toGetResponse();
    }

    @PostMapping("/user/{userId}/application/{applicationId}")
    public RestResult<CofunderAssignmentResource> assign(@PathVariable long userId, @PathVariable long applicationId) {
        return cofunderAssignmentService.assign(userId, applicationId).toGetResponse();
    }

    @DeleteMapping("/user/{userId}/application/{applicationId}")
    public RestResult<Void> removeAssignment(@PathVariable long userId, @PathVariable long applicationId) {
        return cofunderAssignmentService.removeAssignment(userId, applicationId).toGetResponse();
    }

    @PostMapping("assignment/{assignmentId}/decision")
    public RestResult<Void> decision(@PathVariable long assignmentId, @RequestBody CofunderDecisionResource decision) {
        return cofunderAssignmentService.decision(assignmentId, decision).toGetResponse();
    }

    @PostMapping("assignment/{assignmentId}/edit")
    public RestResult<Void> edit(@PathVariable long assignmentId) {
        return cofunderAssignmentService.edit(assignmentId).toGetResponse();
    }

    @GetMapping("/competition/{competitionId}")
    public RestResult<ApplicationsForCofundingPageResource> findApplicationsNeedingCofunders(@PathVariable long competitionId,
                                                                                             @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable,
                                                                                             @RequestParam String filter) {
        return cofunderAssignmentService.findApplicationsNeedingCofunders(competitionId, filter, pageable).toGetResponse();
    }

    @GetMapping("/application/{applicationId}")
    public RestResult<CofundersAvailableForApplicationPageResource> findAvailableCofudersForApplication(@PathVariable long applicationId,
                                                                                                        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable,
                                                                                                        @RequestParam String filter) {
        return cofunderAssignmentService.findAvailableCofudersForApplication(applicationId, filter, pageable).toGetResponse();
    }
}
