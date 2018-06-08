package org.innovateuk.ifs.application.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.transactional.ApplicationInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.innovateuk.ifs.commons.error.ValidationMessages.rejectValue;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;

/**
 * Validates the inputs in the application team page, if valid on the markAsComplete action
 *
 */
 @Component
 public class ApplicationTeamMarkAsCompleteValidator implements Validator {

    @Autowired
    ApplicationInviteService applicationInviteService;

    private static final Log LOG = LogFactory.getLog(ApplicationTeamMarkAsCompleteValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        //Check subclasses for in case we receive hibernate proxy class.
        return Application.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        LOG.debug("do ApplicationTeamMarkAsComplete Validation");

        Application application = (Application) target;

        ServiceResult<List<InviteOrganisationResource>> invitesResult = applicationInviteService.getInvitesByApplication(application.getId());
        List<InviteOrganisationResource> invites = invitesResult.getSuccess();
        for (InviteOrganisationResource organisation : invites) {
            if (organisation.getInviteResources()
                    .stream()
                    .anyMatch(invite -> invite.getStatus() != OPENED)) {
                LOG.debug("MarkAsComplete application team validation message for invite organisation: " + organisation.getOrganisationName());
                //TODO: IFS-3088 - currently this always rejects the first invite. need to make this more clever
                rejectValue(errors, "invites[0]", "validation.applicationteam.pending.invites");
            }
        }
    }

}
