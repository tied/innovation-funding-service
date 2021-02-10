package org.innovateuk.ifs.questionnaire.link.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class QuestionnaireResponseLinkRestServiceImpl extends BaseRestService implements QuestionnaireResponseLinkRestService {

    private String questionnaireResponseLink = "/questionnaire-response-link";

    @Override
    public RestResult<Long> getResponseIdByApplicationIdAndOrganisationIdAndQuestionnaireId(long questionnaireId, long applicationId, long organisationId) {
        return getWithRestResult(format("%s/%d/application/%d/organisation/%d", questionnaireResponseLink, questionnaireId, applicationId, organisationId), Long.class);
    }
}
