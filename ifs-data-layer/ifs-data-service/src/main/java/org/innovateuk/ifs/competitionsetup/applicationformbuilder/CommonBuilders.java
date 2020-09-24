package org.innovateuk.ifs.competitionsetup.applicationformbuilder;

import org.innovateuk.ifs.category.domain.ResearchCategory;
import org.innovateuk.ifs.category.repository.ResearchCategoryRepository;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.FormInputBuilder;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.GuidanceRowBuilder;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.QuestionBuilder;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.SectionBuilder;
import org.innovateuk.ifs.finance.domain.GrantClaimMaximum;
import org.innovateuk.ifs.finance.resource.OrganisationSize;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.form.resource.QuestionType;
import org.innovateuk.ifs.form.resource.SectionType;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.FormInputBuilder.aFormInput;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.GuidanceRowBuilder.aGuidanceRow;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.MultipleChoiceOptionBuilder.aMultipleChoiceOption;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.QuestionBuilder.*;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.SectionBuilder.aSection;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.SectionBuilder.aSubSection;

@Component
public class CommonBuilders {

    @Autowired
    private ResearchCategoryRepository categoryRepository;

    /*
    Tech debt.
    Unused columns: section.withDisplayInAssessmentApplicationSummary
                    section.withAssessorGuidanceDescription (move to template?)
                    section.questionGroup
                    form_input.includedInApplicationSummary

     */

    public static SectionBuilder projectDetails() {
        return aSection()
                .withName("Project details")
                .withType(SectionType.GENERAL)
                .withDescription("Please provide information about your project. This section is not scored but will provide background to the project.")
                .withAssessorGuidanceDescription("These sections give important background information on the project. They do not need scoring however you do need to mark the scope.");
    }

    public static SectionBuilder applicationQuestions() {
        return aSection()
                .withName("Application questions")
                .withType(SectionType.GENERAL)
                .withDescription("These are the questions which will be marked by the assessors.")
                .withAssessorGuidanceDescription("Each question should be given a score out of 10. Written feedback should also be given.");
    }

    public static SectionBuilder ktpAssessmentQuestions() {
        return aSection()
                .withName("Score Guidance")
                .withType(SectionType.KTP_ASSESSMENT);
    }

    public static SectionBuilder finances() {
        return aSection()
                .withName("Finances")
                .withType(SectionType.GENERAL)
                .withAssessorGuidanceDescription("Each partner is required to submit their own project finances and funding rates. The overall project costs for all partners can be seen in the Finances overview section")
                .withChildSections(newArrayList(
                        aSubSection()
                                .withName("Your project finances")
                                .withType(SectionType.FINANCE)
                                .withChildSections(newArrayList(
                                        aSubSection()
                                                .withName("Your project costs")
                                                .withType(SectionType.PROJECT_COST_FINANCES)
                                                .withQuestions(newArrayList(
                                                        aQuestionWithMultipleStatuses()
                                                )),
                                        aSubSection()
                                                .withName("Your project location")
                                                .withType(SectionType.PROJECT_LOCATION)
                                                .withQuestions(newArrayList(
                                                        aQuestionWithMultipleStatuses()
                                                )),
                                        aSubSection()
                                                .withName("Your organisation")
                                                .withType(SectionType.ORGANISATION_FINANCES)
                                                .withQuestions(newArrayList(
                                                        aQuestionWithMultipleStatuses()
                                                )),
                                        aSubSection()
                                                .withName("Your funding")
                                                .withType(SectionType.FUNDING_FINANCES)
                                                .withQuestions(newArrayList(
                                                        aQuestionWithMultipleStatuses()
                                                ))
                                )),
                        aSubSection()
                                .withName("Finances overview")
                                .withType(SectionType.OVERVIEW_FINANCES)
                                .withQuestions(newArrayList(
                                        aQuestion()
                                ))
                ));
    }

    public static SectionBuilder termsAndConditions() {
        return aSection()
                .withName("Terms and conditions")
                .withType(SectionType.TERMS_AND_CONDITIONS)
                .withDescription("You must agree to these before you submit your application.")
                .withQuestions(newArrayList(aQuestion()
                        .withShortName("Award terms and conditions")
                        .withName("Award terms and conditions")
                        .withDescription("Award terms and conditions")
                        .withMarkAsCompletedEnabled(true)
                        .withMultipleStatuses(true)
                        .withAssignEnabled(false)
                        .withQuestionSetupType(QuestionSetupType.TERMS_AND_CONDITIONS)));
    }

    public static QuestionBuilder applicationTeam() {
        return aQuestion()
                .withShortName("Application team")
                .withName("Application team")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.LEAD_ONLY)
                .withQuestionSetupType(QuestionSetupType.APPLICATION_TEAM);
    }

    public static QuestionBuilder applicationDetails() {
        return aQuestion()
                .withShortName("Application details")
                .withName("Application details")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.LEAD_ONLY)
                .withQuestionSetupType(QuestionSetupType.APPLICATION_DETAILS);
    }

    public static QuestionBuilder researchCategory() {
        return aQuestion()
                .withShortName("Research category")
                .withName("Research category")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.LEAD_ONLY)
                .withQuestionSetupType(QuestionSetupType.RESEARCH_CATEGORY);
    }

    public static QuestionBuilder equalityDiversityAndInclusion() {
        return aQuestion()
                .withShortName("Equality, diversity and inclusion")
                .withName("Have you completed the EDI survey?")
                .withDescription("<a href=\"https://www.surveymonkey.co.uk/r/ifsaccount\" target=\"_blank\" rel=\"external\">Complete the survey (opens in new window).</a><p>We will not use this data when we assess your application. We collect this data anonymously and only use it to help us understand our funding recipients better.</p>")
                .withAssignEnabled(true)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.EQUALITY_DIVERSITY_INCLUSION)
                .withFormInputs(newArrayList(
                        aFormInput()
                                .withType(FormInputType.MULTIPLE_CHOICE)
                                .withActive(true)
                                .withScope(FormInputScope.APPLICATION)
                                .withMultipleChoiceOptions(newArrayList(
                                        aMultipleChoiceOption()
                                                .withText("Yes"),
                                        aMultipleChoiceOption()
                                                .withText("No")
                                ))
                ));
    }

    public static QuestionBuilder projectSummary() {
        return aQuestion()
                .withShortName("Project summary")
                .withName("Project summary")
                .withDescription("Please provide a short summary of your project. We will not score this summary.")
                .withAssignEnabled(true)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.PROJECT_SUMMARY)
                .withFormInputs(newArrayList(
                        aFormInput()
                                .withType(FormInputType.TEXTAREA)
                                .withGuidanceTitle("What should I include in the project summary?")
                                .withGuidanceAnswer("<p>We will not score this summary, but it will give the assessors a useful introduction to your project. It should provide a clear overview of the whole project, including:</p> <ul class=\"list-bullet\">         <li>your vision for the project</li><li>key objectives</li><li>main areas of focus</li><li>details of how it is innovative</li></ul>")
                                .withWordCount(400)
                                .withActive(true)
                                .withScope(FormInputScope.APPLICATION),
                        aFormInput()
                                .withType(FormInputType.MULTIPLE_CHOICE)
                                .withActive(false)
                                .withScope(FormInputScope.APPLICATION)
                ));
    }

    public static QuestionBuilder publicDescription() {
        return aQuestion()
                .withShortName("Public description")
                .withName("Public description")
                .withDescription("Please provide a brief description of your project. If your application is successful, we will publish this description. This question is mandatory but is not scored.")
                .withAssignEnabled(true)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.PUBLIC_DESCRIPTION)
                .withFormInputs(newArrayList(
                        aFormInput()
                                .withType(FormInputType.TEXTAREA)
                                .withGuidanceTitle("What should I include in the project public description?")
                                .withGuidanceAnswer("<p>Innovate UK publishes information about projects we have funded. This is in line with government practice on openness and transparency of public-funded activities.</p><p>Describe your project in a way that will be easy for a non-specialist to understand. Don't include any information that is confidential, for example, intellectual property or patent details.</p> ")
                                .withWordCount(400)
                                .withActive(true)
                                .withScope(FormInputScope.APPLICATION),
                        aFormInput()
                                .withType(FormInputType.MULTIPLE_CHOICE)
                                .withActive(false)
                                .withScope(FormInputScope.APPLICATION)
                ));
    }

    public static QuestionBuilder scope() {
        return aQuestion()
                .withShortName("Scope")
                .withName("How does your project align with the scope of this competition?")
                .withDescription("If your application doesn't align with the scope, we will not assess it.")
                .withAssignEnabled(true)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.SCOPE)
                .withFormInputs(newArrayList(
                        aFormInput()
                                .withType(FormInputType.TEXTAREA)
                                .withScope(FormInputScope.APPLICATION)
                                .withActive(true)
                                .withGuidanceTitle("What should I include in the project scope?")
                                .withGuidanceAnswer("<p>It is important that you read the following guidance.</p><p>To show how your project aligns with the scope of this competition, you need to:</p><ul class=\"list-bullet\">         <li>read the competition brief in full</li><li>understand the background, challenge and scope of the competition</li><li>address the research objectives in your application</li><li>match your project's objectives and activities to these</li></ul> <p>Once you have submitted your application, you should not change this section unless:</p><ul class=\"list-bullet\">         <li>we ask you to provide more information</li><li>we ask you to make it clearer</li></ul>")
                                .withWordCount(400),
                        aFormInput()
                                .withType(FormInputType.MULTIPLE_CHOICE)
                                .withScope(FormInputScope.APPLICATION)
                                .withActive(false),
                        aFormInput()
                                .withType(FormInputType.TEXTAREA)
                                .withScope(FormInputScope.ASSESSMENT)
                                .withActive(true)
                                .withGuidanceTitle("Guidance for assessing scope")
                                .withGuidanceAnswer("You should still assess this application even if you think that it is not in scope. Your answer should be based upon the following:")
                                .withWordCount(100)
                                .withGuidanceRows(newArrayList(
                                        aGuidanceRow()
                                                .withSubject("Yes")
                                                .withJustification("The application contains the following: Is the consortia business led? Are there two or more partners to the collaboration? Does it meet the scope of the competition as defined in the competition brief?"),
                                        aGuidanceRow()
                                                .withSubject("No")
                                                .withJustification("One or more of the above requirements have not been satisfied.")
                                )),
                        aFormInput()
                                .withType(FormInputType.ASSESSOR_APPLICATION_IN_SCOPE)
                                .withScope(FormInputScope.ASSESSMENT)
                                .withActive(true)
                                .withDescription("Is the application in scope?"),
                        aFormInput()
                                .withType(FormInputType.ASSESSOR_RESEARCH_CATEGORY)
                                .withScope(FormInputScope.ASSESSMENT)
                                .withActive(true)
                                .withDescription("Please select the research category for this project")
                ));
    }

    public static QuestionBuilder genericQuestion() {
        return aDefaultAssessedQuestion()
                .withFormInputs(
                        defaultAssessedQuestionFormInputs(Function.identity(),
                                assessorInputBuilder ->
                                        assessorInputBuilder.withGuidanceRows(newArrayList(
                                                aGuidanceRow()
                                                        .withSubject("9,10"),
                                                aGuidanceRow()
                                                        .withSubject("7,8"),
                                                aGuidanceRow()
                                                        .withSubject("5,6"),
                                                aGuidanceRow()
                                                        .withSubject("3,4"),
                                                aGuidanceRow()
                                                        .withSubject("1,2")
                                        ))
                        )
                );
    }

    //        maybe change default assessed questions and fix form inputs
    public static QuestionBuilder impact() {
        return aQuestion()
                .withShortName("Impact")
                .withName("Impact")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.KTP_ASSESSMENT)
                .withFormInputs(defaultKtpAssessedQuestionFormInputs("impact", impactGuidanceRows()));
    }

    public static QuestionBuilder innovation() {
        return aQuestion()
                .withShortName("Innovation")
                .withName("Innovation")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.KTP_ASSESSMENT)
                .withFormInputs(defaultKtpAssessedQuestionFormInputs("innovation", innovationGuidanceRows()));
    }

    public static QuestionBuilder cohesiveness() {
        return aQuestion()
                .withShortName("Cohesiveness")
                .withName("Cohesiveness")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.KTP_ASSESSMENT)
                .withFormInputs(defaultKtpAssessedQuestionFormInputs("cohesiveness", cohesivenessGuidanceRows()));
    }

    public static QuestionBuilder challenge() {
        return aQuestion()
                .withShortName("Challenge")
                .withName("Challenge")
                .withAssignEnabled(false)
                .withMarkAsCompletedEnabled(true)
                .withMultipleStatuses(false)
                .withType(QuestionType.GENERAL)
                .withQuestionSetupType(QuestionSetupType.KTP_ASSESSMENT)
                .withFormInputs(defaultKtpAssessedQuestionFormInputs("challenge", challengeGuidanceRows()));
    }

    public static List<FormInputBuilder> defaultKtpAssessedQuestionFormInputs(String questionName, List<GuidanceRowBuilder> guidanceRows) {
        return newArrayList(aFormInput()
                        .withType(FormInputType.ASSESSOR_SCORE)
                        .withScope(FormInputScope.ASSESSMENT)
                .withGuidanceAnswer("Your score should be base of the following")
                .withGuidanceTitle("Guidance for assessing " + questionName)
                        .withActive(true),
                aFormInput()
                        .withType(FormInputType.TEXTAREA)
                        .withScope(FormInputScope.ASSESSMENT)
                        .withGuidanceAnswer("Your score should be base of the following")
                        .withGuidanceTitle("Guidance for assessing " + questionName)
                        .withActive(true)
                .withGuidanceRows(guidanceRows)
        );
    }

    private static List<GuidanceRowBuilder> impactGuidanceRows() {
        return newArrayList(
                aGuidanceRow()
                        .withSubject("9,10")
                        .withJustification("The application demonstrates a well-defined, realistic and positive impact on the business partner’s financial position (i.e. profit, turn-over, productivity and cost savings) and embeds new capabilities within the organisation, which will develop a culture of and capacity for ongoing innovation. There is potential for impact (economic or societal) beyond the partnership."),
                aGuidanceRow()
                        .withSubject("7,8")
                        .withJustification("The application demonstrates a well-defined, realistic and positive impact on the business partner’s financial position (i.e. profit, turn-over, productivity and cost savings) and embeds new capabilities within the organisation, which will develop a culture of and capacity for ongoing innovation. There is potential for impact (economic or societal) beyond the partnership."),
                aGuidanceRow()
                        .withSubject("5,6")
                        .withJustification("The application has realistic and defined impact on the business partner’s financial position and embeds realistic and defined new capabilities within the organisation."),
                aGuidanceRow()
                        .withSubject("3,4")
                        .withJustification("The application has unrealistic or ill-defined impact on the business partner’s financial position or fails to embed new capabilities within the organisation."),
                aGuidanceRow()
                        .withSubject("1,2")
                        .withJustification("The application fails to demonstrate realistic or feasible impact on the business partner’s financial position and does not embed new capabilities within the organisation.")
        );
    }

    private static List<GuidanceRowBuilder> cohesivenessGuidanceRows() {
        return newArrayList(
                aGuidanceRow()
                        .withSubject("9,10")
                        .withJustification("The application is clearly cohesive and easily demonstrates an outstanding balance between the various expectations of a KTP project.  The application gives a very high level of confidence that the project team will work well together, that the project will exceed its goals and the partners will gain in ways above and beyond the defined outcomes of the project."),
                aGuidanceRow()
                        .withSubject("7,8")
                        .withJustification("The application is ill-defined or only demonstrates a moderate to low level of cohesiveness between the individual elements, so the project is not likely to deliver it goals."),
                aGuidanceRow()
                        .withSubject("5,6")
                        .withJustification("The application is ill-defined or only demonstrates a moderate to low level of cohesiveness between the individual elements, so the project is not likely to deliver it goals."),
                aGuidanceRow()
                        .withSubject("3,4")
                        .withJustification("The application is ill-defined or only demonstrates a moderate to low level of cohesiveness between the individual elements, so the project is not likely to deliver it goals."),
                aGuidanceRow()
                        .withSubject("1,2")
                        .withJustification("The application fails to demonstrate any cohesiveness between the individual elements of the application.")
        );
    }

    private static List<GuidanceRowBuilder> challengeGuidanceRows() {
        return newArrayList(
                aGuidanceRow()
                        .withSubject("9,10")
                        .withJustification("The application clearly demonstrates that the project challenges the practices of the business partner and the markets it operates in. The application will clearly demonstrate that the knowledge base partner will be stretched in their translational thinking to deliver the project. The application will demonstrate that the associate will be at the centre of managing and delivering such a project and will get excellent technical and commercial exposure."),
                aGuidanceRow()
                        .withSubject("7,8")
                        .withJustification("The application demonstrates a realistic and well-defined challenge for the business partner in that its practices or processes will change. The application demonstrates that the knowledge base partner needs to deliver creative solutions and that the associate will be stretched in technical and commercial terms."),
                aGuidanceRow()
                        .withSubject("5,6")
                        .withJustification("The application demonstrates a realistic and well-defined challenge for the business partner, but it will not lead to a significant change of its practices. The application demonstrates that the knowledge base partner needs to deliver solutions based on current knowledge, without being cutting-edge solutions, and that the associate will be expected to show above-average technical and commercial skills."),
                aGuidanceRow()
                        .withSubject("3,4")
                        .withJustification("The application has an unrealistic, ill-defined or low level of challenge for either the knowledge base partner, the associate or the business partner."),
                aGuidanceRow()
                        .withSubject("1,2")
                        .withJustification("The application is clearly cohesive and easily demonstrates an outstanding balance between the various expectations of a KTP project.  The application gives a very high level of confidence that the project team will work well together, that the project will exceed its goals and the partners will gain in ways above and beyond the defined outcomes of the project.")
        );
    }

    private static List<GuidanceRowBuilder> innovationGuidanceRows() {
        return newArrayList(
                aGuidanceRow()
                        .withSubject("9,10")
                        .withJustification("The application demonstrates high likelihood that the business partner will become a leader in the field or best in class.  It will bring unique, innovative new products or services to market. The project will provide an opportunity for the business partner to do something new for itself or create a new commercial opportunity. The application demonstrates the business partner’s commitment to ongoing innovation."),
                aGuidanceRow()
                        .withSubject("7,8")
                        .withJustification("The application demonstrates well defined, substantial and realistic goals regarding the business partner’s product or market position and demonstrates that the project will lead to a new commercial opportunity and improve the innovation culture within the organisation."),
                aGuidanceRow()
                        .withSubject("5,6")
                        .withJustification("The application has realistic, defined goals regarding the business partner’s product or market position and indicates that the project will lead to a new commercial opportunity."),
                aGuidanceRow()
                        .withSubject("3,4")
                        .withJustification("The application has unrealistic or ill-defined goals regarding the business partner’s product or market position or fails to demonstrate that the project will lead to either a new commercial opportunity or an innovative culture."),
                aGuidanceRow()
                        .withSubject("1,2")
                        .withJustification("The application fails to demonstrate realistic or feasible innovation in the business partner’s product or market position and does not demonstrate a commitment to ongoing innovation.")
        );
    }

    public static List<FormInputBuilder> defaultAssessedQuestionFormInputs(Function<FormInputBuilder, FormInputBuilder> applicationTextAreaModifier, Function<FormInputBuilder, FormInputBuilder> assessorTextAreaModifier, Function<FormInputBuilder, FormInputBuilder> appendixFormInputModifier) {
        return newArrayList(
                applicationTextAreaModifier.apply(aFormInput()
                        .withType(FormInputType.TEXTAREA)
                        .withScope(FormInputScope.APPLICATION)
                        .withActive(true)
                        .withWordCount(400)),
                aFormInput()
                        .withType(FormInputType.MULTIPLE_CHOICE)
                        .withScope(FormInputScope.APPLICATION)
                        .withActive(false),
                appendixFormInputModifier.apply(aFormInput()
                        .withType(FormInputType.FILEUPLOAD)
                        .withScope(FormInputScope.APPLICATION)
                        .withActive(false)),
                aFormInput()
                        .withType(FormInputType.TEMPLATE_DOCUMENT)
                        .withScope(FormInputScope.APPLICATION)
                        .withActive(false),
                assessorTextAreaModifier.apply(aFormInput()
                        .withType(FormInputType.TEXTAREA)
                        .withScope(FormInputScope.ASSESSMENT)
                        .withActive(true)
                        .withWordCount(100)),
                aFormInput()
                        .withType(FormInputType.ASSESSOR_SCORE)
                        .withScope(FormInputScope.ASSESSMENT)
                        .withActive(true)
        );
    }

    public static List<FormInputBuilder> defaultAssessedQuestionFormInputs(Function<FormInputBuilder, FormInputBuilder> applicationTextAreaModifier, Function<FormInputBuilder, FormInputBuilder> assessorTextAreaModifier) {
        return defaultAssessedQuestionFormInputs(applicationTextAreaModifier,
                assessorTextAreaModifier,
                Function.identity());
    }

    public List<GrantClaimMaximum> getDefaultGrantClaimMaximums() {
        ResearchCategory feasibilityStudies = categoryRepository.findById(33L).get();
        ResearchCategory industrialResearch = categoryRepository.findById(34L).get();
        ResearchCategory experimentalDevelopment = categoryRepository.findById(35L).get();
        return newArrayList(
                new GrantClaimMaximum(feasibilityStudies, OrganisationSize.SMALL, 70),
                new GrantClaimMaximum(feasibilityStudies, OrganisationSize.MEDIUM, 60),
                new GrantClaimMaximum(feasibilityStudies, OrganisationSize.LARGE, 50),
                new GrantClaimMaximum(industrialResearch, OrganisationSize.SMALL, 70),
                new GrantClaimMaximum(industrialResearch, OrganisationSize.MEDIUM, 60),
                new GrantClaimMaximum(industrialResearch, OrganisationSize.LARGE, 50),
                new GrantClaimMaximum(experimentalDevelopment, OrganisationSize.SMALL, 45),
                new GrantClaimMaximum(experimentalDevelopment, OrganisationSize.MEDIUM, 35),
                new GrantClaimMaximum(experimentalDevelopment, OrganisationSize.LARGE, 25)
        );
    }
}
