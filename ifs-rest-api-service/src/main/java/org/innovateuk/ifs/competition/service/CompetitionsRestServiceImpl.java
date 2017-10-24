package org.innovateuk.ifs.competition.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.resource.ApplicationPageResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.resource.CompetitionOpenQueryResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.ZonedDateTime;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.*;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.competitionTypeResourceListType;

/**
 * CompetitionsRestServiceImpl is a utility for CRUD operations on {@link CompetitionResource}.
 * This class connects to the { org.innovateuk.ifs.competition.controller.CompetitionController}
 * through a REST call.
 */
@Service
public class CompetitionsRestServiceImpl extends BaseRestService implements CompetitionsRestService {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(CompetitionsRestServiceImpl.class);
    private String competitionsRestURL = "/competition";
    private String competitionsTypesRestURL = "/competition-type";

    @Override
    public RestResult<List<CompetitionResource>> getAll() {
        return getWithRestResult(competitionsRestURL + "/findAll", competitionResourceListType());
    }

    @Override
    public RestResult<List<CompetitionResource>> getCompetitionsByUserId(Long userId) {
        return getWithRestResult(competitionsRestURL + "/getCompetitionsByUserId/" + userId, competitionResourceListType());
    }

    @Override
    public RestResult<List<CompetitionSearchResultItem>> findLiveCompetitions() {
        return getWithRestResult(competitionsRestURL + "/live", competitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<CompetitionSearchResultItem>> findProjectSetupCompetitions() {
        return getWithRestResult(competitionsRestURL + "/project-setup", competitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<CompetitionSearchResultItem>> findUpcomingCompetitions() {
        return getWithRestResult(competitionsRestURL + "/upcoming", competitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<CompetitionSearchResultItem>> findNonIfsCompetitions() {
        return getWithRestResult(competitionsRestURL + "/non-ifs", competitionSearchResultItemListType());
    }

    @Override
    public RestResult<ApplicationPageResource> findUnsuccessfulApplications(Long competitionId, int pageNumber, int pageSize, String sortField) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String uriWithParams = buildPaginationUri(competitionsRestURL +  "/" + competitionId + "/unsuccessful-applications", pageNumber, pageSize, sortField, params);
        return getWithRestResult(uriWithParams, ApplicationPageResource.class);
    }

    @Override
    public RestResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size) {
        return getWithRestResult(competitionsRestURL + "/search/" + page + "/" + size + "?searchQuery=" + searchQuery, CompetitionSearchResult.class);
    }

    @Override
    public RestResult<CompetitionCountResource> countCompetitions() {
        return getWithRestResult(competitionsRestURL + "/count", CompetitionCountResource.class);
    }

    @Override
    public RestResult<CompetitionResource> getCompetitionById(long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId, CompetitionResource.class);
    }

    @Override
    public RestResult<List<UserResource>> findInnovationLeads(long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId + "/innovation-leads", userListType());
    }

    @Override
    public RestResult<Void> addInnovationLead(long competitionId, long innovationLeadUserId) {
        return postWithRestResult(competitionsRestURL + "/" + competitionId + "/add-innovation-lead/" + innovationLeadUserId, Void.class);
    }

    @Override
    public RestResult<Void> removeInnovationLead(long competitionId, long innovationLeadUserId) {
        return postWithRestResult(competitionsRestURL + "/" + competitionId + "/remove-innovation-lead/" + innovationLeadUserId, Void.class);
    }

    @Override
    public RestResult<List<OrganisationTypeResource>> getCompetitionOrganisationType(long competitionId) {
        return getWithRestResultAnonymous(competitionsRestURL + "/" + competitionId + "/getOrganisationTypes", organisationTypeResourceListType());
    }

    @Override
    public RestResult<CompetitionResource> getPublishedCompetitionById(long competitionId) {
        return getWithRestResultAnonymous(competitionsRestURL + "/" + competitionId, CompetitionResource.class);
    }

    @Override
    public RestResult<List<CompetitionTypeResource>> getCompetitionTypes() {
        return getWithRestResult(competitionsTypesRestURL + "/findAll", competitionTypeResourceListType());
    }

    @Override
    public RestResult<CompetitionResource> create() {
        return postWithRestResult(competitionsRestURL + "", CompetitionResource.class);
    }


    @Override
    public RestResult<Void> update(CompetitionResource competition) {
        return putWithRestResult(competitionsRestURL + "/" + competition.getId(), competition, Void.class);
    }

    @Override
    public RestResult<Void> updateCompetitionInitialDetails(CompetitionResource competition) {
        return putWithRestResult(competitionsRestURL + "/" + competition.getId() + "/update-competition-initial-details", competition, Void.class);
    }

    @Override
    public RestResult<Void> markSectionComplete(long competitionId, CompetitionSetupSection section) {
        return getWithRestResult(String.format("%s/sectionStatus/complete/%s/%s", competitionsRestURL, competitionId, section), Void.class);
    }

    @Override
    public RestResult<Void> markSectionInComplete(long competitionId, CompetitionSetupSection section) {
        return getWithRestResult(String.format("%s/sectionStatus/incomplete/%s/%s", competitionsRestURL, competitionId, section), Void.class);
    }

    @Override
    public RestResult<String> generateCompetitionCode(long competitionId, ZonedDateTime openingDate) {
        return postWithRestResult(String.format("%s/generateCompetitionCode/%s", competitionsRestURL, competitionId), openingDate, String.class);
    }

    @Override
    public RestResult<Void> initApplicationForm(long competitionId, long competitionTypeId) {
        return postWithRestResult(String.format("%s/%s/initialise-form/%s", competitionsRestURL, competitionId, competitionTypeId), Void.class);
    }

    @Override
    public RestResult<Void> markAsSetup(long competitionId) {
        return postWithRestResult(String.format("%s/%s/mark-as-setup", competitionsRestURL, competitionId), Void.class);
    }

    @Override
    public RestResult<Void> returnToSetup(long competitionId) {
        return postWithRestResult(String.format("%s/%s/return-to-setup", competitionsRestURL, competitionId), Void.class);
    }

    @Override
    public RestResult<Void> closeAssessment(long competitionId) {
        return putWithRestResult(String.format("%s/%s/close-assessment", competitionsRestURL, competitionId), Void.class);
    }

    @Override
    public RestResult<Void> notifyAssessors(long competitionId) {
        return putWithRestResult(String.format("%s/%s/notify-assessors", competitionsRestURL, competitionId), Void.class);
    }

    @Override
    public RestResult<Void> releaseFeedback(long competitionId) {
        return putWithRestResult(String.format("%s/%s/release-feedback", competitionsRestURL, competitionId), Void.class);
    }

    @Override
    public RestResult<CompetitionResource> createNonIfs() {
        return postWithRestResult(competitionsRestURL + "/non-ifs", CompetitionResource.class);
    }

    @Override
    public RestResult<List<CompetitionSearchResultItem>> findFeedbackReleasedCompetitions() {
        return getWithRestResult(competitionsRestURL +  "/feedback-released", competitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<CompetitionOpenQueryResource>> getCompetitionOpenQueries(long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId + "/queries/open", competitionOpenQueryResourceListType());
    }

    @Override
    public RestResult<Long> getCompetitionOpenQueriesCount(long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId + "/queries/open/count", Long.class);
    }
}
