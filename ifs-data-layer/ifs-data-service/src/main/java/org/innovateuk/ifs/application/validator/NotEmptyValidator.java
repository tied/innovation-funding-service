package org.innovateuk.ifs.application.validator;

import org.innovateuk.ifs.application.domain.FormInputResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.ZeroDowntime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import static org.innovateuk.ifs.commons.rest.ValidationMessages.rejectValue;

/**
 * This class validates the FormInputResponse, it checks if there is a value present.
 */
@Component
public class NotEmptyValidator extends BaseValidator {
    @ZeroDowntime(reference = "IFS-3144", description = "Remove old package names and add flyway script to correct them in database.")
    public static final String OLD_PACKAGE_NAME = "org.innovateuk.ifs.validation.validator.NotEmptyValidator";
    private static final Log LOG = LogFactory.getLog(NotEmptyValidator.class);

    @Override
    public void validate(Object target, Errors errors) {
        LOG.debug("do NotEmpty validation ");
        FormInputResponse response = (FormInputResponse) target;

        if (StringUtils.isEmpty(response.getValue()) || "".equals(response.getValue().trim())) {
            LOG.debug("NotEmpty validation message for: " + response.getId());
            rejectValue(errors, "value", "validation.field.please.enter.some.text");
        }
    }
}