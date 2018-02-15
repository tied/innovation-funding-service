package org.innovateuk.ifs.testdata.services;

import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.testdata.builders.*;
import org.innovateuk.ifs.testdata.builders.data.ApplicationData;
import org.innovateuk.ifs.testdata.builders.data.CompetitionData;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.innovateuk.ifs.assessment.resource.AssessmentState.READY_TO_SUBMIT;
import static org.innovateuk.ifs.assessment.resource.AssessmentState.SUBMITTED;
import static org.innovateuk.ifs.testdata.builders.AssessmentDataBuilder.newAssessmentData;
import static org.innovateuk.ifs.testdata.builders.AssessorDataBuilder.newAssessorData;
import static org.innovateuk.ifs.testdata.builders.AssessorInviteDataBuilder.newAssessorInviteData;
import static org.innovateuk.ifs.testdata.builders.AssessorResponseDataBuilder.newAssessorResponseData;
import static org.innovateuk.ifs.testdata.services.CsvUtils.*;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

/**
 * TODO DW - document this class
 */
@Component
public class AssessmentDataBuilderService extends BaseDataBuilderService {

    private List<CsvUtils.AssessorResponseLine> assessorResponseLines;
    private List<CsvUtils.AssessorUserLine> assessorUserLines;
    private List<CsvUtils.InviteLine> inviteLines;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenericApplicationContext applicationContext;

    private List<CsvUtils.AssessmentLine> assessmentLines;

    private AssessmentDataBuilder assessmentBuilder;
    private AssessorDataBuilder assessorUserBuilder;
    private AssessorResponseDataBuilder assessorResponseBuilder;
    private AssessorInviteDataBuilder assessorInviteUserBuilder;

    @PostConstruct
    public void readCsvs() {
        ServiceLocator serviceLocator = new ServiceLocator(applicationContext, COMP_ADMIN_EMAIL, PROJECT_FINANCE_EMAIL);

        assessmentBuilder = newAssessmentData(serviceLocator);
        assessorUserBuilder = newAssessorData(serviceLocator);
        assessorResponseBuilder = newAssessorResponseData(serviceLocator);
        assessorInviteUserBuilder = newAssessorInviteData(serviceLocator);

        assessmentLines = readAssessments();
        assessorUserLines = readAssessorUsers();
        assessorResponseLines = readAssessorResponses();
        inviteLines = readInvites();
    }

    public void createAssessments(List<ApplicationData> applications) {

        applications.forEach(application -> {

            List<AssessmentLine> assessmentLinesForApplication = simpleFilter(assessmentLines, l ->
                    Objects.equals(l.applicationName, application.getApplication().getName()));

            assessmentLinesForApplication.forEach(assessmentLine -> {

                createAssessment(assessmentLine);

                createAssessorResponses(assessmentLine, application.getCompetition());

                submitAssessment(assessmentLine);
            });

        });
    }

    private void createAssessorResponses(AssessmentLine assessmentLine, CompetitionResource competition) {

        List<AssessorResponseLine> assessorResponsesForAssessment = simpleFilter(assessorResponseLines, l ->
                Objects.equals(l.applicationName, assessmentLine.applicationName) &&
                l.assessorEmail.equals(assessmentLine.assessorEmail));

        if (!assessorResponsesForAssessment.isEmpty()) {

            createAssessorResponsesFromCsvs(assessorResponsesForAssessment);

        } else if (asList(READY_TO_SUBMIT, SUBMITTED).contains(assessmentLine.state)) {

            createAssessorResponsesFromDefaults(assessmentLine, competition);
        }
    }

    private void createAssessorResponsesFromDefaults(AssessmentLine assessmentLine, CompetitionResource competition) {

        String competitionName = competition.getName();
        String applicationName = assessmentLine.applicationName;
        String assessorEmail = assessmentLine.assessorEmail;

        List<QuestionResource> competitionQuestions = retrieveCachedQuestionsByCompetitionId(competition.getId());

        List<QuestionResource> questionsToAnswer = simpleFilter(competitionQuestions, question -> {

            List<FormInputResource> formInputs = retrieveCachedFormInputsByQuestionId(question);

            return simpleAnyMatch(formInputs, fi -> FormInputScope.ASSESSMENT.equals(fi.getScope()));
        });

        questionsToAnswer.forEach(question -> {

            List<FormInputResource> formInputs = retrieveCachedFormInputsByQuestionId(question);

            createAssessorResponseForFormInputIfPresent(competitionName, applicationName, assessorEmail, question,
                    formInputs, "research category", "Feasibility studies");

            createAssessorResponseForFormInputIfPresent(competitionName, applicationName, assessorEmail, question,
                    formInputs, "scope", "TRUE");

            createAssessorResponseForFormInputIfPresent(competitionName, applicationName, assessorEmail, question,
                    formInputs, "Feedback",
                    "This is the " + question.getShortName().toLowerCase() + " feedback");

            createAssessorResponseForFormInputIfPresent(competitionName, applicationName, assessorEmail, question,
                    formInputs, "score", "7");
        });
    }

    private void createAssessorResponseForFormInputIfPresent(String competitionName,
                                                             String applicationName,
                                                             String assessorEmail,
                                                             QuestionResource question,
                                                             List<FormInputResource> formInputs,
                                                             String description,
                                                             String value) {

        Optional<FormInputResource> assessorFormInput = simpleFindFirst(formInputs, fi ->
                fi.getDescription().contains(description));

        assessorFormInput.ifPresent(r ->
                createAssessorResponse(
                        competitionName,
                        applicationName,
                        assessorEmail,
                        question.getShortName(),
                        description,
                        description.equals("research category"),
                        value));
    }

    private void createAssessorResponsesFromCsvs(List<AssessorResponseLine> assessorResponsesForAssessment) {
        assessorResponsesForAssessment.forEach(this::createAssessorResponse);
    }

    public void createAssessors(List<CompetitionData> competitions) {

        competitions.forEach(competition -> {

            List<AssessorUserLine> assessorLinesForCompetition = simpleFilter(assessorUserLines, l ->
                    Objects.equals(l.competitionName, competition.getCompetition().getName()));

            assessorLinesForCompetition.forEach(this::createAssessor);
        });
    }

    public void createNonRegisteredAssessorInvites(List<CompetitionData> competitions) {

        competitions.forEach(competition -> {

            List<CsvUtils.InviteLine> assessorInvites = simpleFilter(inviteLines, invite ->
                    "COMPETITION".equals(invite.type) && invite.targetName.equals(competition.getCompetition().getName()));

            List<CsvUtils.InviteLine> nonRegisteredAssessorInvites = simpleFilter(assessorInvites, invite -> !userRepository.findByEmail(invite.email).isPresent());

            nonRegisteredAssessorInvites.forEach(line -> createAssessorInvite(assessorInviteUserBuilder, line));
        });
    }

    private void createAssessment(AssessmentLine line) {
        assessmentBuilder.withAssessmentData(
                line.assessorEmail,
                line.applicationName,
                line.rejectReason,
                line.rejectComment,
                line.state,
                line.feedback,
                line.recommendComment)
                .build();
    }

    private void createAssessorResponse(AssessorResponseLine line) {
        createAssessorResponse(line.competitionName,
                line.applicationName,
                line.assessorEmail,
                line.shortName,
                line.description,
                line.isResearchCategory,
                line.value);
    }

    private void createAssessorResponse(String competitionName,
                                        String applicationName,
                                        String assessorEmail,
                                        String shortName,
                                        String description,
                                        boolean isResearchCategory,
                                        String value) {

        assessorResponseBuilder.withAssessorResponseData(competitionName,
                applicationName,
                assessorEmail,
                shortName,
                description,
                isResearchCategory,
                value).build();
    }

    private void submitAssessment(AssessmentLine line) {
        assessmentBuilder.withSubmission(
                line.applicationName,
                line.assessorEmail,
                line.state)
                .build();
    }

    private void createAssessor(AssessorUserLine line) {

        List<InviteLine> assessorInvitesForThisAssessor = simpleFilter(inviteLines, invite ->
                invite.email.equals(line.emailAddress) && invite.type.equals("COMPETITION"));

        AssessorDataBuilder builder = assessorUserBuilder;

        Optional<User> existingUser = userRepository.findByEmail(line.emailAddress);
        Optional<User> sentBy = userRepository.findByEmail("john.doe@innovateuk.test");
        Optional<ZonedDateTime> sentOn = Optional.of(ZonedDateTime.now());

        for (InviteLine invite : assessorInvitesForThisAssessor) {
            builder = builder.withInviteToAssessCompetition(
                    invite.targetName,
                    invite.email,
                    invite.name,
                    invite.hash,
                    invite.status,
                    existingUser,
                    invite.innovationAreaName,
                    sentBy,
                    sentOn
            );
        }

        String inviteHash = !isBlank(line.hash) ? line.hash : UUID.randomUUID().toString();
        String innovationArea = !line.innovationAreas.isEmpty() ? line.innovationAreas.get(0) : "";

        AssessorDataBuilder baseBuilder = builder.withInviteToAssessCompetition(
                line.competitionName,
                line.emailAddress,
                line.firstName + " " + line.lastName,
                inviteHash,
                line.inviteStatus,
                existingUser,
                innovationArea,
                sentBy,
                sentOn
        );

        if (!existingUser.isPresent()) {
            baseBuilder = baseBuilder.registerUser(
                    line.firstName,
                    line.lastName,
                    line.emailAddress,
                    line.phoneNumber,
                    line.ethnicity,
                    line.gender,
                    line.disability,
                    inviteHash
            );
        } else {
            baseBuilder = baseBuilder.addAssessorRole();
        }

        baseBuilder = baseBuilder.addSkills(line.skillAreas, line.businessType, line.innovationAreas);
        baseBuilder = baseBuilder.addAffiliations(
                line.principalEmployer,
                line.role,
                line.professionalAffiliations,
                line.appointments,
                line.financialInterests,
                line.familyAffiliations,
                line.familyFinancialInterests
        );

        if (line.agreementSigned) {
            baseBuilder = baseBuilder.addAgreementSigned();
        }

        if (!line.rejectionReason.isEmpty()) {
            baseBuilder = baseBuilder.rejectInvite(inviteHash, line.rejectionReason, line.rejectionComment);
        } else if (InviteStatus.OPENED.equals(line.inviteStatus)) {
            baseBuilder = baseBuilder.acceptInvite(inviteHash);
        }

        baseBuilder.build();
    }

    private void createAssessorInvite(AssessorInviteDataBuilder assessorInviteUserBuilder, InviteLine line) {
        assessorInviteUserBuilder.withInviteToAssessCompetition(
                line.targetName,
                line.email,
                line.name,
                line.hash,
                line.status,
                userRepository.findByEmail(line.email),
                line.innovationAreaName,
                userRepository.findByEmail(line.sentByEmail),
                Optional.of(line.sentOn)).
                build();
    }



}

