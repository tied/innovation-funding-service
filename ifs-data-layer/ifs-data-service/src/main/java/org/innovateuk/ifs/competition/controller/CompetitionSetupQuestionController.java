package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.rest.*;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.transactional.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * CompetitionSetupQuestionController exposes competition setup application questions data and operations through a REST API.
 */
@RestController
@RequestMapping("/competition-setup-question")
public class CompetitionSetupQuestionController {

    @Autowired
    private CompetitionSetupQuestionService competitionSetupQuestionService;

    @GetMapping("/getById/{id}")
    public RestResult<CompetitionSetupQuestionResource> getByQuestionId(@PathVariable("id") final Long id) {
        return competitionSetupQuestionService.getByQuestionId(id).toGetResponse();
    }

    @PutMapping("/save")
    public RestResult<Void> save(@RequestBody final CompetitionSetupQuestionResource competitionSetupQuestionResource) {
        return competitionSetupQuestionService.update(competitionSetupQuestionResource).toPutResponse();
    }

    @PostMapping("/addDefaultToCompetition/{id}")
    public RestResult<CompetitionSetupQuestionResource> addDefaultToCompetitionId(@PathVariable("id") final Long competitionId) {
        return competitionSetupQuestionService.createByCompetitionId(competitionId).toPostCreateResponse();
    }

    @DeleteMapping("/deleteByIdAndSection/{id}/{sectionName}")
    public RestResult<Void> deleteByIdAndSection(@PathVariable("id") final Long questionId,
                                       @PathVariable("sectionName") final String sectionName) {
        return competitionSetupQuestionService.delete(questionId, sectionName).toDeleteResponse();
    }

    /**
     * TODO: This endpoint needs to be removed
     * @param questionId
     * @return
     */
    @ZeroDowntime(reference = "IFS-2832", description = "Changed the endpoint. deleteByIdAndSection is now used")
    @DeleteMapping("/deleteById/{id}")
    public RestResult<Void> deleteById(@PathVariable("id") final Long questionId) {
        return competitionSetupQuestionService.delete(questionId, "Application questions").toDeleteResponse();
    }

}
