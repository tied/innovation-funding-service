package org.innovateuk.ifs.project.status.viewmodel;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerResource;
import org.innovateuk.ifs.project.projectdetails.viewmodel.BasicProjectDetailsViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.sections.SectionAccess;
import org.innovateuk.ifs.sections.SectionStatus;

import java.util.Optional;

/**
 * A view model that backs the Project Status page
 */

public class SetupStatusViewModel implements BasicProjectDetailsViewModel {

    private Long projectId;
    private String projectName;
    private Long applicationId;
    private String competitionName;
    private Long competitionId;
    private boolean monitoringOfficerAssigned;
    private boolean leadPartner;
    private boolean hasCompaniesHouse;
    private boolean projectComplete;
    private String monitoringOfficerName;
    private Long organisationId;
    private SectionAccessList sectionAccesses;
    private SectionStatusList sectionStatuses;
    private boolean collaborationAgreementRequired;
    private boolean competitionDocuments;
    private boolean projectManager;
    private boolean pendingQuery;
    private String originQuery;
    private ProjectState projectState;
    private boolean monitoringOfficer;

    public SetupStatusViewModel() {}

    public SetupStatusViewModel(ProjectResource project,
                                CompetitionResource competition,
                                Optional<MonitoringOfficerResource> monitoringOfficerResource,
                                OrganisationResource organisation,
                                boolean leadPartner,
                                SectionAccessList sectionAccesses,
                                SectionStatusList sectionStatuses,
                                boolean collaborationAgreementRequired,
                                boolean competitionDocuments,
                                boolean projectManager,
                                boolean pendingQuery,
                                String originQuery,
                                boolean monitoringOfficer) {

        this.projectId = project.getId();
        this.projectName = project.getName();
        this.applicationId = project.getApplication();
        this.competitionName = competition.getName();
        this.competitionId = competition.getId();
        this.leadPartner = leadPartner;
        this.hasCompaniesHouse = organisation.getCompaniesHouseNumber() != null && !organisation.getCompaniesHouseNumber().isEmpty();
        this.monitoringOfficerAssigned = monitoringOfficerResource.isPresent();
        this.monitoringOfficerName = monitoringOfficerResource.map(MonitoringOfficerResource::getFullName).orElse("");
        this.organisationId = organisation.getId();
        this.sectionAccesses = sectionAccesses;
        this.sectionStatuses = sectionStatuses;
        this.projectComplete = sectionStatuses.isProjectComplete();
        this.collaborationAgreementRequired = collaborationAgreementRequired;
        this.competitionDocuments = competitionDocuments;
        this.projectManager = projectManager;
        this.pendingQuery = pendingQuery;
        this.originQuery = originQuery;
        this.projectState = project.getProjectState();
        this.monitoringOfficer = monitoringOfficer;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public boolean isMonitoringOfficerAssigned() {
        return monitoringOfficerAssigned;
    }

    public String getMonitoringOfficerName() {
        return monitoringOfficerName;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public boolean isLeadPartner() {
        return leadPartner;
    }

    public boolean isNonLeadPartner() {
        return !leadPartner;
    }

    public SectionAccess getCompaniesHouseSection() {
        return sectionAccesses.getCompaniesHouseSection();
    }

    public SectionAccess getProjectDetailsSection() {
        return sectionAccesses.getProjectDetailsSection();
    }

    public SectionAccess getProjectTeamSection() {
        return sectionAccesses.getProjectTeamSection();
    }

    public SectionAccess getMonitoringOfficerSection() {
        return sectionAccesses.getMonitoringOfficerSection();
    }

    public SectionAccess getBankDetailsSection() {
        return sectionAccesses.getBankDetailsSection();
    }

    public SectionAccess getFinanceChecksSection() {
        return sectionAccesses.getFinanceChecksSection();
    }

    public SectionAccess getSpendProfileSection() {
        return sectionAccesses.getSpendProfileSection();
    }

    public SectionAccess getDocumentsSection() {
        return sectionAccesses.getDocumentsSection();
    }

    public SectionAccess getGrantOfferLetterSection() {
        return sectionAccesses.getGrantOfferLetterSection();
    }

    public SectionStatus getProjectDetailsStatus() {
        return sectionStatuses.getProjectDetailsStatus();
    }

    public SectionStatus getProjectTeamStatus() {
        return sectionStatuses.getProjectTeamStatus();
    }

    public SectionStatus getMonitoringOfficerStatus() {
        return sectionStatuses.getMonitoringOfficerStatus();
    }

    public SectionStatus getBankDetailsStatus() {
        return sectionStatuses.getBankDetailsStatus();
    }

    public SectionStatus getFinanceChecksStatus() {
        return sectionStatuses.getFinanceChecksStatus();
    }

    public SectionStatus getSpendProfileStatus() {
        return sectionStatuses.getSpendProfileStatus();
    }

    public SectionStatus getDocumentsStatus() {
        return sectionStatuses.getDocumentsStatus();
    }

    public SectionStatus getGrantOfferLetterStatus() {
        return sectionStatuses.getGrantOfferLetterStatus();
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public boolean isHasCompaniesHouse() {
        return hasCompaniesHouse;
    }

    public boolean isProjectComplete() {
        return projectComplete;
    }

    public boolean isCollaborationAgreementRequired() { return collaborationAgreementRequired; }

    public boolean isCompetitionDocuments() {
        return competitionDocuments;
    }

    public boolean isProjectManager() { return projectManager; }

    public boolean isShowFinanceChecksPendingQueryWarning() {
        return pendingQuery;
    }

    public String getOriginQuery() {
        return originQuery;
    }

    public ProjectState getProjectState() {
        return projectState;
    }

    public boolean isMonitoringOfficer() {
        return monitoringOfficer;
    }

    public void setMonitoringOfficer(boolean monitoringOfficer) {
        this.monitoringOfficer = monitoringOfficer;
    }
}
