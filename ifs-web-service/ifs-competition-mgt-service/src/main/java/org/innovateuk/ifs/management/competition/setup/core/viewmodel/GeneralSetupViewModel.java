package org.innovateuk.ifs.management.competition.setup.core.viewmodel;

import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.FundingRules;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class GeneralSetupViewModel {

    private static final List<CompetitionSetupSection> PUBLISH_SECTIONS = asList(
            CompetitionSetupSection.INITIAL_DETAILS,
            CompetitionSetupSection.COMPLETION_STAGE,
            CompetitionSetupSection.CONTENT);

    private static final List<CompetitionSetupSection> COMPETITION_SETUP_SECTIONS = asList(
            CompetitionSetupSection.TERMS_AND_CONDITIONS,
            CompetitionSetupSection.ADDITIONAL_INFO,
            CompetitionSetupSection.PROJECT_ELIGIBILITY,
            CompetitionSetupSection.FUNDING_ELIGIBILITY,
            CompetitionSetupSection.ORGANISATIONAL_ELIGIBILITY,
            CompetitionSetupSection.APPLICATION_FORM);

    private static final List<CompetitionSetupSection> ASSESSMENT_SECTIONS = singletonList(
            CompetitionSetupSection.ASSESSORS);

    private boolean editable;
    private boolean firstTimeInForm;
    private CompetitionResource competition;
    private CompetitionSetupSection currentSection;
    private String currentSectionFragment;
    private CompetitionSetupSection[] allSections;
    private boolean isInitialComplete;
    private CompetitionStateSetupViewModel state;
    private boolean ifsAdmin;

    public GeneralSetupViewModel(boolean editable, boolean firstTimeInForm, CompetitionResource competition,
                                 CompetitionSetupSection section,
                                 CompetitionSetupSection[] allSections, boolean isInitialComplete, boolean ifsAdmin) {
        this.editable = editable;
        this.firstTimeInForm = firstTimeInForm;
        this.competition = competition;
        this.currentSection = section;
        this.allSections = allSections;
        this.isInitialComplete = isInitialComplete;
        this.ifsAdmin = ifsAdmin;
    }

    public void setCurrentSectionFragment(String currentSectionFragment) {
        this.currentSectionFragment = currentSectionFragment;
    }

    public void setState(CompetitionStateSetupViewModel state) {
        this.state = state;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isFirstTimeInForm() {
        return firstTimeInForm;
    }

    public CompetitionResource getCompetition() {
        return competition;
    }

    public CompetitionSetupSection getCurrentSection() {
        return currentSection;
    }

    public String getCurrentSectionFragment() {
        return currentSectionFragment;
    }

    public CompetitionSetupSection[] getAllSections() {
        return allSections;
    }

    public boolean isInitialComplete() {
        return isInitialComplete;
    }

    public CompetitionStateSetupViewModel getState() {
        return state;
    }

    public boolean currentSectionIsHome() {
        return currentSection.equals(CompetitionSetupSection.HOME);
    }

    public boolean isIfsAdmin() {
        return ifsAdmin;
    }

    public boolean isSubsidyControlCompetition() {
        return FundingRules.SUBSIDY_CONTROL == competition.getFundingRules();
    }

    public static List<CompetitionSetupSection> getPublishSections() {
        return PUBLISH_SECTIONS;
    }

    public static List<CompetitionSetupSection> getCompetitionSetupSections() {
        return COMPETITION_SETUP_SECTIONS;
    }

    public static List<CompetitionSetupSection> getAssessmentSections() {
        return ASSESSMENT_SECTIONS;
    }
}
