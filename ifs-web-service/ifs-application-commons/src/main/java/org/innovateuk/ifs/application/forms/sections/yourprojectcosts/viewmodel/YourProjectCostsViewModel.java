package org.innovateuk.ifs.application.forms.sections.yourprojectcosts.viewmodel;

import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;

public class YourProjectCostsViewModel {
    private final Long applicationId;

    private final Long sectionId;

    private final Long organisationId;

    private final Long competitionId;

    private final boolean complete;

    private final boolean open;

    private final String applicationName;

    private final String organisationName;

    private final String financesUrl;

    private final boolean internal;

    private final boolean includeVat;

    private final boolean procurementCompetition;

    public YourProjectCostsViewModel(long applicationId,
                                     long sectionId,
                                     long competitionId,
                                     long organisationId,
                                     boolean complete,
                                     boolean open,
                                     boolean includeVat,
                                     String applicationName,
                                     String organisationName,
                                     String financesUrl,
                                     boolean procurementCompetition) {
        this.applicationId = applicationId;
        this.sectionId = sectionId;
        this.competitionId = competitionId;
        this.organisationId = organisationId;
        this.complete = complete;
        this.open = open;
        this.includeVat = includeVat;
        this.applicationName = applicationName;
        this.organisationName = organisationName;
        this.financesUrl = financesUrl;
        this.procurementCompetition = procurementCompetition;

        this.internal = false;
    }

    public YourProjectCostsViewModel(boolean open, boolean internal, boolean procurementCompetition) {
        this.open = open;
        this.internal = internal;
        this.procurementCompetition = procurementCompetition;

        this.applicationId = null;
        this.sectionId = null;
        this.competitionId = null;
        this.organisationId = null;
        this.complete = false;
        this.applicationName = null;
        this.organisationName = null;
        this.financesUrl = null;
        this.includeVat = false;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isOpen() {
        return open;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getFinancesUrl() {
        return financesUrl;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isIncludeVat() {
        return includeVat;
    }

    /* view logic */
    public boolean isReadOnly() {
        return complete || !open;
    }

    public boolean isReadOnly(FinanceRowType type) {
        return isReadOnly();
    }

    public boolean isProcurementCompetition() {
        return procurementCompetition;
    }
}