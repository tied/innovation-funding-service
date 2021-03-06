package org.innovateuk.ifs.application.forms.questions.generic.populator;

import org.innovateuk.ifs.applicant.resource.ApplicantFormInputResource;
import org.innovateuk.ifs.applicant.resource.ApplicantFormInputResponseResource;
import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.application.forms.questions.generic.viewmodel.GenericQuestionAppendix;
import org.innovateuk.ifs.application.forms.questions.generic.viewmodel.GenericQuestionApplicationViewModel;
import org.innovateuk.ifs.application.forms.questions.generic.viewmodel.GenericQuestionApplicationViewModel.GenericQuestionApplicationViewModelBuilder;
import org.innovateuk.ifs.application.populator.AssignButtonsPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.form.resource.MultipleChoiceOptionResource;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.innovateuk.ifs.application.forms.questions.generic.viewmodel.GenericQuestionApplicationViewModel.GenericQuestionApplicationViewModelBuilder.aGenericQuestionApplicationViewModel;
import static org.innovateuk.ifs.util.TimeZoneUtil.toUkTimeZone;

@Component
public class GenericQuestionApplicationModelPopulator {

    @Autowired
    private AssignButtonsPopulator assignButtonsPopulator;

    public GenericQuestionApplicationViewModel populate(ApplicantQuestionResource applicantQuestion, Optional<Long> organisationId) {
        Map<FormInputType, ApplicantFormInputResource> formInputs = applicantQuestion.getApplicantFormInputs()
                .stream()
                .collect(toMap(input -> input.getFormInput().getType(), Function.identity()));
        QuestionResource question = applicantQuestion.getQuestion();
        ApplicationResource application = applicantQuestion.getApplication();
        CompetitionResource competition = applicantQuestion.getCompetition();
        OrganisationResource leadOrganisation = applicantQuestion.getLeadOrganisation();

        GenericQuestionApplicationViewModelBuilder viewModelBuilder = aGenericQuestionApplicationViewModel();

        ofNullable(formInputs.get(FormInputType.TEXTAREA)).ifPresent(input -> buildTextAreaViewModel(viewModelBuilder, input));
        ofNullable(formInputs.get(FormInputType.MULTIPLE_CHOICE)).ifPresent(input -> buildMultipleChoiceOptionsViewModel(viewModelBuilder, organisationId, input));
        ofNullable(formInputs.get(FormInputType.FILEUPLOAD)).ifPresent(input -> buildAppendixViewModel(viewModelBuilder, input));
        ofNullable(formInputs.get(FormInputType.TEMPLATE_DOCUMENT)).ifPresent(input -> buildTemplateDocumentViewModel(viewModelBuilder, input));

        applicantQuestion.getApplicantFormInputs()
                .stream()
                .flatMap(input -> input.getApplicantResponses().stream())
                .map(ApplicantFormInputResponseResource::getResponse)
                .max(Comparator.comparing(FormInputResponseResource::getUpdateDate))
                .ifPresent(response -> viewModelBuilder.withLastUpdated(toUkTimeZone(response.getUpdateDate()))
                                                       .withLastUpdatedBy(response.getUpdatedByUser())
                                                       .withLastUpdatedByName(response.getUpdatedByUserName()));

        boolean hideAssignButtons = !Boolean.TRUE.equals(question.isAssignEnabled());

        return viewModelBuilder.withApplicationId(application.getId())
                .withCompetitionName(competition.getName())
                .withApplicationName(application.getName())
                .withCurrentUser(applicantQuestion.getCurrentUser().getId())
                .withQuestionId(question.getId())
                .withQuestionName(question.getShortName())
                .withQuestionSubtitle(question.getName())
                .withQuestionDescription(question.getDescription())
                .withQuestionDescription2(question.getDescription2())
                .withQuestionNumber(question.getQuestionNumber())
                .withQuestionType(question.getQuestionSetupType())
                .withQuestionHasMultipleStatus(Boolean.TRUE.equals(question.hasMultipleStatuses()))
                .withComplete(applicantQuestion.isCompleteByApplicant(applicantQuestion.getCurrentApplicant()))
                .withOpen(application.isOpen() && competition.isOpen())
                .withLeadApplicant(applicantQuestion.getCurrentApplicant().isLead())
                .withAssignButtonsViewModel(assignButtonsPopulator.populate(applicantQuestion, applicantQuestion, hideAssignButtons))
                .withLeadOrganisationName(leadOrganisation.getName())
                .withLeadOrganisationCompaniesHouseNumber(leadOrganisation.getCompaniesHouseNumber())
                .build();
    }

    private void buildTemplateDocumentViewModel(GenericQuestionApplicationViewModelBuilder viewModelBuilder, ApplicantFormInputResource input) {
        viewModelBuilder.withTemplateDocumentFormInputId(input.getFormInput().getId())
                .withTemplateDocumentTitle(input.getFormInput().getDescription())
                .withTemplateDocumentFilename(input.getFormInput().getFile().getName())
                .withTemplateDocumentResponseFilename(firstFile(input).map(FileEntryResource::getName).orElse(null))
                .withTemplateDocumentResponseFileEntryId(firstFile(input).map(FileEntryResource::getId).orElse(null));
    }

    private void buildAppendixViewModel(GenericQuestionApplicationViewModelBuilder viewModelBuilder, ApplicantFormInputResource input) {
        viewModelBuilder.withAppendixFormInputId(input.getFormInput().getId())
                .withAppendixGuidance(input.getFormInput().getGuidanceAnswer())
                .withAppendices(appendices(input))
                .withAppendixAllowedFileTypes(input.getFormInput().getAllowedFileTypes())
                .withMaximumAppendices(input.getFormInput().getWordCount());
    }

    private void buildTextAreaViewModel(GenericQuestionApplicationViewModelBuilder viewModelBuilder, ApplicantFormInputResource input) {
        viewModelBuilder.withTextAreaFormInputId(input.getFormInput().getId())
                .withWordCount(input.getFormInput().getWordCount())
                .withQuestionGuidanceTitle(input.getFormInput().getGuidanceTitle())
                .withQuestionGuidance(input.getFormInput().getGuidanceAnswer())
                .withWordsLeft(firstResponse(input).map(FormInputResponseResource::getWordCountLeft).orElse(input.getFormInput().getWordCount()));
    }

    private void buildMultipleChoiceOptionsViewModel(GenericQuestionApplicationViewModelBuilder viewModelBuilder, Optional<Long> organisationId, ApplicantFormInputResource input) {
        viewModelBuilder.withMultipleChoiceFormInputId(input.getFormInput().getId())
                .withSelectedMultipleChoiceOption(multipleChoiceOptionResponseOrNull(input, organisationId))
                .withQuestionGuidanceTitle(input.getFormInput().getGuidanceTitle())
                .withQuestionGuidance(input.getFormInput().getGuidanceAnswer())
                .withMultipleChoiceOptions(input.getFormInput().getMultipleChoiceOptions());
    }

    private List<GenericQuestionAppendix> appendices(ApplicantFormInputResource input) {
        return firstResponse(input)
                .map(resp -> resp.getFileEntries()
                        .stream()
                        .map(file -> new GenericQuestionAppendix(file.getId(), file.getName()))
                        .collect(toList()))
                .orElse(emptyList());

    }

    private Optional<FileEntryResource> firstFile(ApplicantFormInputResource input) {
        return firstResponse(input)
                .flatMap(resp -> resp.getFileEntries().stream().findFirst());
    }

    private MultipleChoiceOptionResource multipleChoiceOptionResponseOrNull(ApplicantFormInputResource input, Optional<Long> organisationId) {

        return firstResponse(input, organisationId)
                .map(formInputResponse -> new MultipleChoiceOptionResource(formInputResponse.getMultipleChoiceOptionId(),
                        formInputResponse.getMultipleChoiceOptionText()))
                .orElse(null);
    }

    private Optional<FormInputResponseResource> firstResponse(ApplicantFormInputResource input) {
        return firstResponse(input, Optional.empty());
    }

    private Optional<FormInputResponseResource> firstResponse(ApplicantFormInputResource input, Optional<Long> organisationId) {

        return input.getApplicantResponses()
                .stream()
                .filter(resp -> {
                    if (organisationId.isPresent()) {
                        return organisationId.get().equals(resp.getApplicant().getOrganisation().getId());
                    } else {
                        return true;
                    }
                }).findAny()
                .map(ApplicantFormInputResponseResource::getResponse);
    }
}
