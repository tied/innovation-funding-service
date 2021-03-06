package org.innovateuk.ifs.application.validator;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.CompanyAge;
import org.innovateuk.ifs.application.resource.CompanyPrimaryFocus;
import org.innovateuk.ifs.application.resource.CompetitionReferralSource;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.stream.IntStream;


import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.resource.CompanyAge.PRE_START_UP;
import static org.innovateuk.ifs.application.resource.CompanyPrimaryFocus.CHEMICALS;
import static org.innovateuk.ifs.application.resource.CompetitionReferralSource.BUSINESS_CONTACT;
import static org.innovateuk.ifs.category.builder.InnovationAreaBuilder.newInnovationArea;
import static org.innovateuk.ifs.category.builder.ResearchCategoryBuilder.newResearchCategory;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceBuilder.newApplicationFinance;
import static org.innovateuk.ifs.procurement.milestone.builder.ApplicationProcurementMilestoneBuilder.newApplicationProcurementMilestone;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Mark as complete validator test class for application details section
 */
public class ApplicationDetailsMarkAsCompleteValidatorTest {

    private Validator validator;
    private LocalDate currentDate;
    private BindingResult bindingResult;
    private Competition competition;
    private Competition procurementCompetition;
    private Application validApplication;

    @Before
    public void setup() {
        validator = new ApplicationDetailsMarkAsCompleteValidator();
        currentDate = LocalDate.now();

        competition = newCompetition()
                .withMinProjectDuration(10)
                .withMaxProjectDuration(20)
                .withResubmission(false)
                .build();

        procurementCompetition = newCompetition()
                .withResubmission(false)
                .withFundingType(FundingType.PROCUREMENT)
                .withMinProjectDuration(10)
                .withMaxProjectDuration(20)
                .build();

        validApplication = newApplication()
                .withName("Valid Application")
                .withStartDate(currentDate.plusDays(1))
                .withDurationInMonths(18L)
                .withNoInnovationAreaApplicable(true)
                .withResubmission(true)
                .withCompetition(competition)
                .withPreviousApplicationNumber("Previous Application Number")
                .withPreviousApplicationTitle("Failed Application")
                .withResearchCategory(newResearchCategory().build())
                .build();
    }

    @Test
    public void validApplication() {
        DataBinder dataBinder = new DataBinder(validApplication);
        bindingResult = dataBinder.getBindingResult();
        validator.validate(validApplication, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public void invalidApplication() {
        Application invalidApplication = newApplication()
                .withName((String) null)
                .withStartDate(currentDate.minusDays(1))
                .withDurationInMonths(-5L)
                .withNoInnovationAreaApplicable(false)
                .withResubmission(true)
                .withCompetition(competition)
                .withPreviousApplicationNumber((String) null)
                .withPreviousApplicationTitle((String) null)
                .build();

        DataBinder dataBinder = new DataBinder(invalidApplication);
        bindingResult = dataBinder.getBindingResult();
        validator.validate(invalidApplication, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(6, bindingResult.getErrorCount());
        assertEquals("validation.project.name.must.not.be.empty", bindingResult.getFieldError("name").getDefaultMessage());
        assertEquals("validation.project.start.date.not.in.future", bindingResult.getFieldError("startDate").getDefaultMessage());
        assertEquals("validation.project.duration.input.invalid", bindingResult.getFieldError("durationInMonths").getDefaultMessage());
        assertEquals("validation.application.innovationarea.category.required", bindingResult.getFieldError("innovationArea").getDefaultMessage());
        assertEquals("validation.application.previous.application.number.required", bindingResult.getFieldError("previousApplicationNumber").getDefaultMessage());
        assertEquals("validation.application.previous.application.title.required", bindingResult.getFieldError("previousApplicationTitle").getDefaultMessage());
    }

    @Test
    public void validProcurementApplication() {
        Application validProcurementApplication = newApplication()
                .withName("Valid Procurement Application")
                .withStartDate(currentDate.plusDays(1))
                .withDurationInMonths(18L)
                .withNoInnovationAreaApplicable(true)
                .withResubmission(true)
                .withCompetition(procurementCompetition)
                .withPreviousApplicationNumber("Previous Application Number")
                .withPreviousApplicationTitle("Failed Application")
                .withCompetitionReferralSource(BUSINESS_CONTACT)
                .withCompetitionPrimaryFocus(CHEMICALS)
                .withCompanyAge(PRE_START_UP)
                .withResearchCategory(newResearchCategory().build())
                .build();

        DataBinder dataBinder = new DataBinder(validProcurementApplication);
        bindingResult = dataBinder.getBindingResult();
        validator.validate(validProcurementApplication, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public void invalidProcurementApplication() {
        Application invalidProcurementApplication = newApplication()
                .withName((String) null)
                .withStartDate(currentDate.minusDays(1))
                .withDurationInMonths(-5L)
                .withNoInnovationAreaApplicable(false)
                .withResubmission(true)
                .withCompetition(procurementCompetition)
                .withCompetitionReferralSource((CompetitionReferralSource) null)
                .withCompetitionPrimaryFocus((CompanyPrimaryFocus) null)
                .withCompanyAge((CompanyAge) null)
                .withPreviousApplicationNumber((String) null)
                .withPreviousApplicationTitle((String) null)
                .build();

        DataBinder dataBinder = new DataBinder(invalidProcurementApplication);
        bindingResult = dataBinder.getBindingResult();
        validator.validate(invalidProcurementApplication, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals(9, bindingResult.getErrorCount());
        assertEquals("validation.project.name.must.not.be.empty", bindingResult.getFieldError("name").getDefaultMessage());
        assertEquals("validation.project.start.date.not.in.future", bindingResult.getFieldError("startDate").getDefaultMessage());
        assertEquals("validation.project.duration.input.invalid", bindingResult.getFieldError("durationInMonths").getDefaultMessage());
        assertEquals("validation.application.procurement.competitionreferralsource.required", bindingResult.getFieldError("competitionReferralSource").getDefaultMessage());
        assertEquals("validation.application.procurement.companyage.required", bindingResult.getFieldError("companyAge").getDefaultMessage());
        assertEquals("validation.application.procurement.companyprimaryfocus.required", bindingResult.getFieldError("companyPrimaryFocus").getDefaultMessage());
        assertEquals("validation.application.innovationarea.category.required", bindingResult.getFieldError("innovationArea").getDefaultMessage());
        assertEquals("validation.application.previous.application.number.required", bindingResult.getFieldError("previousApplicationNumber").getDefaultMessage());
        assertEquals("validation.application.previous.application.title.required", bindingResult.getFieldError("previousApplicationTitle").getDefaultMessage());
    }

    @Test
    public void valid_applicationInnovationAreaIsApplicableAndSet() {
        Application validApplicationInnovationAreaApplicableAndSet = newApplication()
                .withName("Valid Application")
                .withStartDate(currentDate.plusDays(1))
                .withDurationInMonths(18L)
                .withNoInnovationAreaApplicable(false)
                .withInnovationArea(newInnovationArea().build())
                .withResubmission(true)
                .withCompetition(competition)
                .withPreviousApplicationNumber("Previous Application Number")
                .withPreviousApplicationTitle("Failed Application")
                .withResearchCategory(newResearchCategory().build())
                .build();

        DataBinder dataBinder = new DataBinder(validApplicationInnovationAreaApplicableAndSet);
        bindingResult = dataBinder.getBindingResult();
        validator.validate(validApplicationInnovationAreaApplicableAndSet, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public void validate_applicationInnovationAreaIsNotSetButApplicableShouldResultInError() {
        Application validApplicationInnovationAreaApplicableNotSet = newApplication()
                .withName("Application with no Innovation Area Applicable")
                .withStartDate(currentDate.plusDays(1))
                .withDurationInMonths(18L)
                .withNoInnovationAreaApplicable(false)
                .withResubmission(true)
                .withCompetition(competition)
                .withPreviousApplicationNumber("Previous Application Number")
                .withPreviousApplicationTitle("Failed Application")
                .withResearchCategory(newResearchCategory().build())
                .build();

        DataBinder dataBinder = new DataBinder(validApplicationInnovationAreaApplicableNotSet);
        bindingResult = dataBinder.getBindingResult();
        validator.validate(validApplicationInnovationAreaApplicableNotSet, bindingResult);

        assertTrue(bindingResult.hasErrors());
        assertEquals("validation.application.innovationarea.category.required",
                bindingResult.getFieldError("innovationArea").getDefaultMessage());
    }

    @Test
    public void supportsApplicationAndSubclasses() {
        assertTrue(validator.supports(Application.class));
        assertTrue(validator.supports(new Application() {
            //empty extension of application;
        }.getClass()));
    }

    @Test
    public void testApplicationDurationIsValidatedAgainstMaxMilestone(){
        Application applicationOverMaxCompetitionDuration = newApplication()
                // Application states 12 months duration
                .withDurationInMonths(12l)
                .withCompetition(newCompetition().withMinProjectDuration(4).withMaxProjectDuration(24).withResubmission(false).build())
                .withApplicationFinancesList(asList(
                        newApplicationFinance()
                                .withMilestones(asList(
                                        // The max milestone is higher than the application duration
                                        newApplicationProcurementMilestone().withMonth(13).build())
                        ).build())
        ).build();
        validateApplicationHasError(
                applicationOverMaxCompetitionDuration,
                "durationInMonths",
                "validation.project.duration.must.be.greater.than.milestones");
    }

    @Test
    public void testApplicationWhereCompetitionDictatesTheMinimumDuration () {
        Application applicationWhereCompetitionDictatesTheMinimumDuration = newApplication()
                // Application states 12 months duration
                .withDurationInMonths(12l)
                // The competition min is 24 months
                .withCompetition(newCompetition().withMinProjectDuration(24).withMaxProjectDuration(36).withResubmission(false).build())
                .withApplicationFinancesList(asList(
                        newApplicationFinance()
                                .withMilestones(asList(
                                        // The max milestone is less than the competition min of 24 months
                                        // Therefore it is the competition that dictates the minimum duration of the application.
                                        newApplicationProcurementMilestone().withMonth(13).build())
                                ).build())
                ).build();
        validateApplicationHasError(
                applicationWhereCompetitionDictatesTheMinimumDuration,
                "durationInMonths",
                "validation.project.duration.input.invalid", new Object[]{24, 36});
    }

    @Test
    public void testApplicationWhereMaxMilestoneDictatesTheMinimumDuration () {
        Application applicationWhereCompetitionDictatesTheMinimumDuration = newApplication()
                // Application states 12 months duration
                .withDurationInMonths(12l)
                // The competition min is 13 months
                .withCompetition(newCompetition().withMinProjectDuration(13).withMaxProjectDuration(36).withResubmission(false).build())
                .withApplicationFinancesList(asList(
                        newApplicationFinance()
                                .withMilestones(asList(
                                        // The max milestone is greater than the competition min of 13 months
                                        // Therefore it is the max milestone that dictates the minimum duration of the application.
                                        newApplicationProcurementMilestone().withMonth(24).build())
                                ).build())
                ).build();
        validateApplicationHasError(
                applicationWhereCompetitionDictatesTheMinimumDuration,
                "durationInMonths",
                "validation.project.duration.must.be.greater.than.milestones");
    }

    @Test
    public void testApplicationDurationIsEqualToMaxAndMinDurationShouldNotResultInError() {
        Application application = newApplication()
                .withDurationInMonths(10l)
                .withCompetition(newCompetition().withMinProjectDuration(10).withMaxProjectDuration(10).withResubmission(false).build())
                .build();
        validateApplicationDoesNotHaveError(application, "durationInMonths");
    }

    @Test
    public void testApplicationDurationExceedsMaxDurationShouldResultInError() {
        Application application = newApplication()
                .withDurationInMonths(21l)
                .withCompetition(newCompetition().withMinProjectDuration(10).withMaxProjectDuration(20).withResubmission(false).build())
                .build();
        validateApplicationHasError(
                application ,
                "durationInMonths",
                "validation.project.duration.input.invalid", new Object[]{10, 20});
    }

    private void validateApplicationDoesNotHaveError(Application application, String field){
        DataBinder dataBinder = new DataBinder(application);
        BindingResult result = dataBinder.getBindingResult();
        new ApplicationDetailsMarkAsCompleteValidator().validate(application, result);
        assertTrue(simpleFilter(
                result.getFieldErrors(),
                error -> error.getField().equals(field))
                .isEmpty());
    }

    private void validateApplicationHasError(Application application, String field, String errorMessage){
        validateApplicationHasError(application, field, errorMessage, new Object[]{});
    }

    private void validateApplicationHasError(Application application, String field, String errorMessage, Object[] args){
        DataBinder dataBinder = new DataBinder(application);
        BindingResult result = dataBinder.getBindingResult();
        new ApplicationDetailsMarkAsCompleteValidator().validate(application, result);
        assertFalse(simpleFilter(
                result.getFieldErrors(),
                error -> args.length == error.getArguments().length
                        && IntStream.range(0, args.length).allMatch(i -> args[i].equals(error.getArguments()[i]))
                        && error.getField().equals(field)
                        && error.getDefaultMessage().equals(errorMessage))
                .isEmpty());
    }
}
