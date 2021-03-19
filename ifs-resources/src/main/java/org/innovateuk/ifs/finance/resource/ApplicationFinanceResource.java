package org.innovateuk.ifs.finance.resource;

/**
 * Application finance resource holds the organisation's finance resources for an application
 */
public class ApplicationFinanceResource extends BaseFinanceResource {

    private Long financeFileEntry;
    private String workPostcode;
    private String internationalLocation;
    private String justification;
    private Boolean fecModelEnabled;
    private Long fecFileEntry;

    public ApplicationFinanceResource(ApplicationFinanceResource applicationFinance) {

        super(applicationFinance);

        if (applicationFinance != null && applicationFinance.getFinanceFileEntry() != null) {
            this.financeFileEntry = applicationFinance.getFinanceFileEntry();
            this.workPostcode = applicationFinance.getWorkPostcode();
            this.internationalLocation = applicationFinance.getInternationalLocation();
            this.justification = applicationFinance.getJustification();
            this.fecFileEntry = applicationFinance.getFecFileEntry();
            this.fecModelEnabled = applicationFinance.getFecModelEnabled();
        }
    }

    public ApplicationFinanceResource() {
    }

    public ApplicationFinanceResource(Long financeFileEntry) {
        this.financeFileEntry = financeFileEntry;
    }

    public ApplicationFinanceResource(Boolean fecModelEnabled, Long fecFileEntry) {
        this.fecModelEnabled = fecModelEnabled;
        this.fecFileEntry = fecFileEntry;
    }

    public ApplicationFinanceResource(long id,
                                      long organisation,
                                      long application,
                                      OrganisationSize organisationSize,
                                      String workPostcode,
                                      String internationalLocation,
                                      String justification) {
        super(id, organisation, application, organisationSize);
        this.workPostcode = workPostcode;
        this.internationalLocation = internationalLocation;
        this.justification = justification;
    }

    public Long getFinanceFileEntry() {
        return financeFileEntry;
    }

    public void setFinanceFileEntry(Long financeFileEntry) {
        this.financeFileEntry = financeFileEntry;
    }

    public Long getApplication() {
        return super.getTarget();
    }

    public void setApplication(Long target) {
        super.setTarget(target);
    }

    public String getWorkPostcode() {
        return workPostcode;
    }

    public void setWorkPostcode(String workPostcode) {
        this.workPostcode = workPostcode;
    }

    public String getInternationalLocation() {
        return internationalLocation;
    }

    public void setInternationalLocation(String internationalLocation) {
        this.internationalLocation = internationalLocation;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Boolean getFecModelEnabled() {
        return fecModelEnabled;
    }

    public void setFecModelEnabled(Boolean fecModelEnabled) {
        this.fecModelEnabled = fecModelEnabled;
    }

    public Long getFecFileEntry() {
        return fecFileEntry;
    }

    public void setFecFileEntry(Long fecFileEntry) {
        this.fecFileEntry = fecFileEntry;
    }
}