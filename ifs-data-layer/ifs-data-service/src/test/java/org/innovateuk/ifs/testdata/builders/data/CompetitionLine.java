package org.innovateuk.ifs.testdata.builders.data;

import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.competition.resource.*;

import java.util.List;
import java.util.Set;

public class CompetitionLine {

    public Long id;
    public String name;
    public Long type;
    public Set<Long> innovationAreas;
    public String innovationSector;
    public Set<Long> researchCategory;
    public CollaborationLevel collaborationLevel;
    public List<Long> leadApplicantTypes;
    public Integer researchRatio;
    public Boolean resubmission;
    public Boolean multiStream;
    public CompetitionStatus competitionStatus;
    public Long leadTechnologist;
    public Long compExecutive;
    public boolean setupComplete;
    public String budgetCode;
    public String code;
    public String pafCode;
    public String activityCode;
    public Integer assessorCount;
    public Boolean hasAssessmentPanel;
    public Boolean hasInterviewStage;
    public AssessorFinanceView assessorFinanceView;
    public boolean published;
    public FundingType fundingType;
    public boolean inviteOnly;
    public boolean nonIfs;
    public String nonIfsUrl;
    public CompetitionCompletionStage competitionCompletionStage;
    public Boolean includeJesForm;
    public ApplicationFinanceType applicationFinanceType;
    public Boolean includeProjectGrowth;
    public Boolean includeYourOrganisation;
    public FundingRules fundingRules;
    public Boolean alwaysOpen;

    public CompetitionLine() {
    }

    public CompetitionLine(Long id,
                           String name,
                           Long type,
                           Set<Long> innovationAreas,
                           String innovationSector,
                           Set<Long> researchCategory,
                           CollaborationLevel collaborationLevel,
                           List<Long> leadApplicantTypes,
                           Integer researchRatio,
                           Boolean resubmission,
                           Boolean multiStream,
                           CompetitionStatus competitionStatus,
                           Long leadTechnologist,
                           Long compExecutive,
                           boolean setupComplete,
                           String budgetCode,
                           String code,
                           String pafCode,
                           String activityCode,
                           Integer assessorCount,
                           Boolean hasAssessmentPanel,
                           Boolean hasInterviewStage,
                           AssessorFinanceView assessorFinanceView,
                           boolean published,
                           FundingType fundingType,
                           boolean inviteOnly,
                           boolean nonIfs,
                           String nonIfsUrl,
                           CompetitionCompletionStage competitionCompletionStage,
                           Boolean includeJesForm,
                           ApplicationFinanceType applicationFinanceType,
                           Boolean includeProjectGrowth,
                           Boolean includeYourOrganisation,
                           FundingRules fundingRules,
                           Boolean alwaysOpen) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.innovationAreas = innovationAreas;
        this.innovationSector = innovationSector;
        this.researchCategory = researchCategory;
        this.collaborationLevel = collaborationLevel;
        this.leadApplicantTypes = leadApplicantTypes;
        this.researchRatio = researchRatio;
        this.resubmission = resubmission;
        this.multiStream = multiStream;
        this.competitionStatus = competitionStatus;
        this.leadTechnologist = leadTechnologist;
        this.compExecutive = compExecutive;
        this.setupComplete = setupComplete;
        this.budgetCode = budgetCode;
        this.code = code;
        this.pafCode = pafCode;
        this.activityCode = activityCode;
        this.assessorCount = assessorCount;
        this.hasAssessmentPanel = hasAssessmentPanel;
        this.hasInterviewStage = hasInterviewStage;
        this.assessorFinanceView = assessorFinanceView;
        this.published = published;
        this.fundingType = fundingType;
        this.inviteOnly = inviteOnly;
        this.nonIfs = nonIfs;
        this.nonIfsUrl = nonIfsUrl;
        this.competitionCompletionStage = competitionCompletionStage;
        this.includeJesForm = includeJesForm;
        this.applicationFinanceType = applicationFinanceType;
        this.includeProjectGrowth = includeProjectGrowth;
        this.includeYourOrganisation = includeYourOrganisation;
        this.fundingRules = fundingRules;
        this.alwaysOpen = alwaysOpen;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Set<Long> getInnovationAreas() {
        return innovationAreas;
    }

    public void setInnovationAreas(Set<Long> innovationAreas) {
        this.innovationAreas = innovationAreas;
    }

    public String getInnovationSector() {
        return innovationSector;
    }

    public void setInnovationSector(String innovationSector) {
        this.innovationSector = innovationSector;
    }

    public Set<Long> getResearchCategory() {
        return researchCategory;
    }

    public void setResearchCategory(Set<Long> researchCategory) {
        this.researchCategory = researchCategory;
    }

    public CollaborationLevel getCollaborationLevel() {
        return collaborationLevel;
    }

    public void setCollaborationLevel(CollaborationLevel collaborationLevel) {
        this.collaborationLevel = collaborationLevel;
    }

    public List<Long> getLeadApplicantTypes() {
        return leadApplicantTypes;
    }

    public void setLeadApplicantTypes(List<Long> leadApplicantTypes) {
        this.leadApplicantTypes = leadApplicantTypes;
    }

    public Integer getResearchRatio() {
        return researchRatio;
    }

    public void setResearchRatio(Integer researchRatio) {
        this.researchRatio = researchRatio;
    }

    public Boolean getResubmission() {
        return resubmission;
    }

    public void setResubmission(Boolean resubmission) {
        this.resubmission = resubmission;
    }

    public Boolean getMultiStream() {
        return multiStream;
    }

    public void setMultiStream(Boolean multiStream) {
        this.multiStream = multiStream;
    }

    public CompetitionStatus getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(CompetitionStatus competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public Long getLeadTechnologist() {
        return leadTechnologist;
    }

    public void setLeadTechnologist(Long leadTechnologist) {
        this.leadTechnologist = leadTechnologist;
    }

    public Long getCompExecutive() {
        return compExecutive;
    }

    public void setCompExecutive(Long compExecutive) {
        this.compExecutive = compExecutive;
    }

    public boolean isSetupComplete() {
        return setupComplete;
    }

    public void setSetupComplete(boolean setupComplete) {
        this.setupComplete = setupComplete;
    }

    public String getBudgetCode() {
        return budgetCode;
    }

    public void setBudgetCode(String budgetCode) {
        this.budgetCode = budgetCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPafCode() {
        return pafCode;
    }

    public void setPafCode(String pafCode) {
        this.pafCode = pafCode;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public Integer getAssessorCount() {
        return assessorCount;
    }

    public void setAssessorCount(Integer assessorCount) {
        this.assessorCount = assessorCount;
    }

    public Boolean getHasAssessmentPanel() {
        return hasAssessmentPanel;
    }

    public void setHasAssessmentPanel(Boolean hasAssessmentPanel) {
        this.hasAssessmentPanel = hasAssessmentPanel;
    }

    public Boolean getHasInterviewStage() {
        return hasInterviewStage;
    }

    public void setHasInterviewStage(Boolean hasInterviewStage) {
        this.hasInterviewStage = hasInterviewStage;
    }

    public AssessorFinanceView getAssessorFinanceView() {
        return assessorFinanceView;
    }

    public void setAssessorFinanceView(AssessorFinanceView assessorFinanceView) {
        this.assessorFinanceView = assessorFinanceView;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public FundingType getFundingType() {
        return fundingType;
    }

    public void setFundingType(FundingType fundingType) {
        this.fundingType = fundingType;
    }

    public boolean isInviteOnly() {
        return inviteOnly;
    }

    public void setInviteOnly(boolean inviteOnly) {
        this.inviteOnly = inviteOnly;
    }

    public boolean isNonIfs() {
        return nonIfs;
    }

    public void setNonIfs(boolean nonIfs) {
        this.nonIfs = nonIfs;
    }

    public String getNonIfsUrl() {
        return nonIfsUrl;
    }

    public void setNonIfsUrl(String nonIfsUrl) {
        this.nonIfsUrl = nonIfsUrl;
    }

    public CompetitionCompletionStage getCompetitionCompletionStage() {
        return competitionCompletionStage;
    }

    public void setCompetitionCompletionStage(CompetitionCompletionStage competitionCompletionStage) {
        this.competitionCompletionStage = competitionCompletionStage;
    }

    public Boolean getIncludeJesForm() {
        return includeJesForm;
    }

    public void setIncludeJesForm(Boolean includeJesForm) {
        this.includeJesForm = includeJesForm;
    }

    public ApplicationFinanceType getApplicationFinanceType() {
        return applicationFinanceType;
    }

    public void setApplicationFinanceType(ApplicationFinanceType applicationFinanceType) {
        this.applicationFinanceType = applicationFinanceType;
    }

    public Boolean getIncludeProjectGrowth() {
        return includeProjectGrowth;
    }

    public void setIncludeProjectGrowth(Boolean includeProjectGrowth) {
        this.includeProjectGrowth = includeProjectGrowth;
    }

    public Boolean getIncludeYourOrganisation() {
        return includeYourOrganisation;
    }

    public void setIncludeYourOrganisation(Boolean includeYourOrganisation) {
        this.includeYourOrganisation = includeYourOrganisation;
    }

    public FundingRules getFundingRules() {
        return fundingRules;
    }

    public void setFundingRules(FundingRules fundingRules) {
        this.fundingRules = fundingRules;
    }

    public Boolean getAlwaysOpen() {
        return alwaysOpen;
    }

    public void setAlwaysOpen(Boolean alwaysOpen) {
        this.alwaysOpen = alwaysOpen;
    }
}
