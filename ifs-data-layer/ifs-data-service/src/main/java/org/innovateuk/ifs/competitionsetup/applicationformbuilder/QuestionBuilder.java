package org.innovateuk.ifs.competitionsetup.applicationformbuilder;

import org.innovateuk.ifs.form.domain.Question;
import org.innovateuk.ifs.form.resource.QuestionType;
import org.innovateuk.ifs.question.resource.QuestionSetupType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class QuestionBuilder {
    private String questionNumber;
    private String name;
    private String shortName;
    private String description;
    private Boolean markAsCompletedEnabled = false;
    private Boolean assignEnabled = true;
    private Boolean multipleStatuses = false;
    private List<FormInputBuilder> formInputs = new ArrayList<>();
    private QuestionType type = QuestionType.GENERAL;
    private QuestionSetupType questionSetupType;
    private Integer assessorMaximumScore;

    private QuestionBuilder() {
    }

    public static QuestionBuilder aQuestion() {
        return new QuestionBuilder();
    }

    public static QuestionBuilder aDefaultAssessedQuestion() {
        return aQuestion()
                .withAssignEnabled(true)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.ASSESSED_QUESTION)
                .withAssessorMaximumScore(10);
    }

    public static QuestionBuilder aQuestionWithMultipleStatuses() {
        return new QuestionBuilder()
                .withMultipleStatuses(true)
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true);
    }

    public QuestionBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public QuestionBuilder withShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public QuestionBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public QuestionBuilder withMarkAsCompletedEnabled(Boolean markAsCompletedEnabled) {
        this.markAsCompletedEnabled = markAsCompletedEnabled;
        return this;
    }

    public QuestionBuilder withAssignEnabled(Boolean assignEnabled) {
        this.assignEnabled = assignEnabled;
        return this;
    }

    public QuestionBuilder withMultipleStatuses(Boolean multipleStatuses) {
        this.multipleStatuses = multipleStatuses;
        return this;
    }

    public QuestionBuilder withFormInputs(List<FormInputBuilder> formInputs) {
        this.formInputs = formInputs;
        return this;
    }

    public List<FormInputBuilder> getFormInputs() {
        return formInputs;
    }

    public QuestionBuilder withQuestionNumber(String questionNumber) {
        this.questionNumber = questionNumber;
        return this;
    }

    public QuestionBuilder withType(QuestionType type) {
        this.type = type;
        return this;
    }

    public QuestionBuilder withQuestionSetupType(QuestionSetupType questionSetupType) {
        this.questionSetupType = questionSetupType;
        return this;
    }

    public QuestionBuilder withAssessorMaximumScore(Integer assessorMaximumScore) {
        this.assessorMaximumScore = assessorMaximumScore;
        return this;
    }

    public Question build() {
        Question question = new Question();
        question.setName(name);
        question.setShortName(shortName);
        question.setDescription(description);
        question.setMarkAsCompletedEnabled(markAsCompletedEnabled);
        question.setAssignEnabled(assignEnabled);
        question.setMultipleStatuses(multipleStatuses);
        question.setFormInputs(formInputs.stream().map(FormInputBuilder::build).collect(Collectors.toList()));
        question.setQuestionNumber(questionNumber);
        question.setType(type);
        question.setQuestionSetupType(questionSetupType);
        question.setAssessorMaximumScore(assessorMaximumScore);
        return question;
    }
}
