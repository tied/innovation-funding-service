package com.worth.ifs.monitoringofficer.viewmodel;

import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.application.resource.CompetitionSummaryResource;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * View model to back the Monitoring Officer page
 */
public class ProjectMonitoringOfficerViewModel {

    private Long projectId;
    private String projectTitle;
    private String area;
    private LocalDate targetProjectStartDate;
    private String projectManagerName;
    private List<String> partnerOrganisationNames;
    private CompetitionSummaryResource competitionSummary;
    private boolean existingMonitoringOfficer;
    private boolean editMode;
    private List<String> primaryAddressLines;

    public ProjectMonitoringOfficerViewModel(Long projectId, String projectTitle, String area, AddressResource primaryAddress, LocalDate targetProjectStartDate, String projectManagerName, List<String> partnerOrganisationNames, CompetitionSummaryResource competitionSummary, boolean existingMonitoringOfficer, boolean editMode) {
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.area = area;
        this.primaryAddressLines = primaryAddress != null ? primaryAddress.getNonEmptyLines() : emptyList();
        this.targetProjectStartDate = targetProjectStartDate;
        this.projectManagerName = projectManagerName;
        this.partnerOrganisationNames = partnerOrganisationNames;
        this.competitionSummary = competitionSummary;
        this.existingMonitoringOfficer = existingMonitoringOfficer;
        this.editMode = editMode;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getArea() {
        return area;
    }

    public LocalDate getTargetProjectStartDate() {
        return targetProjectStartDate;
    }

    public String getProjectManagerName() {
        return projectManagerName;
    }

    public List<String> getPartnerOrganisationNames() {
        return partnerOrganisationNames;
    }

    public CompetitionSummaryResource getCompetitionSummary() {
        return competitionSummary;
    }

    public List<String> getPrimaryAddressLines() {
        return primaryAddressLines;
    }

    public boolean isReadOnly() {
        return !editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public boolean isExistingMonitoringOfficer() {
        return existingMonitoringOfficer;
    }

    public boolean isDisplayMonitoringOfficerAssignedMessage() {
        return existingMonitoringOfficer && isReadOnly();
    }

    public boolean isDisplayChangeMonitoringOfficerLink() {
        return isReadOnly();
    }

    public boolean isDisplayAssignMonitoringOfficerButton() {
        return isEditMode();
    }
}
