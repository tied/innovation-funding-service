package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.assessment.transactional.AssessmentPanelService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing applications on an assessment panel.
 */
@RestController
@RequestMapping("/assessmentpanel")
public class AssessmentPanelController {

    @Autowired
    private AssessmentPanelService assessmentPanelService;

    @PostMapping({
            "/assignApplication/{applicationId}",
    })
    public RestResult<Void> assignApplication(@PathVariable long applicationId) {
        return assessmentPanelService.assignApplicationToPanel(applicationId).toPostResponse();
    }

    @PostMapping({
            "/unassignApplication/{applicationId}",
    })
    public RestResult<Void> unAssignApplication(@PathVariable long applicationId) {
        return assessmentPanelService.unassignApplicationFromPanel(applicationId).toPostResponse();
    }

    @PostMapping("/notify-assessors/{competitionId}")
    public RestResult<Void> notifyAssessors(@PathVariable("competitionId") long competitionId) {
        return assessmentPanelService.createAndNotifyReviews(competitionId).toPostResponse();
    }

    @GetMapping("/notify-assessors/{competitionId}")
    public RestResult<Boolean> isPendingReviewNotifications(@PathVariable("competitionId") long competitionId) {
        return assessmentPanelService.isPendingReviewNotifications(competitionId).toGetResponse();
    }
}