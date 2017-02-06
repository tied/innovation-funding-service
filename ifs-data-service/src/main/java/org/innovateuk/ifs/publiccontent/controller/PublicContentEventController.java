package org.innovateuk.ifs.publiccontent.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentEventResource;
import org.innovateuk.ifs.publiccontent.transactional.PublicContentEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for all public content actions.
 */
@RestController
@RequestMapping("/public-content/events/")
public class PublicContentEventController {

    @Autowired
    private PublicContentEventService publicContentEventService;

    @RequestMapping(value = "save-event", method = RequestMethod.POST)
    public RestResult<Void> saveEvent(@RequestBody final PublicContentEventResource event) {
        return publicContentEventService.saveEvent(event).toPostResponse();
    }

    @RequestMapping(value = "reset-and-save-events", method = RequestMethod.POST)
    public RestResult<Void> resetAndSaveEvent(@RequestParam("id") final Long id,
                                              @RequestBody final List<PublicContentEventResource> events) {
        return publicContentEventService.resetAndSaveEvents(id, events).toPostResponse();
    }
}
