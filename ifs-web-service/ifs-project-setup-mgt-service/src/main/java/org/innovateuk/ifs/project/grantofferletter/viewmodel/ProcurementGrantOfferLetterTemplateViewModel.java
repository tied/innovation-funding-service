package org.innovateuk.ifs.project.grantofferletter.viewmodel;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ProcurementGrantOfferLetterTemplateViewModel {

    private final long applicationId;
    private final String organisationName;
    private final String projectName;
    private final String competitionName;
    private final String projectManagerName;
    private final List<ProcurementGrantOfferLetterTemplateMilestoneEntryViewModel> milestones;
    private final List<ProcurementGrantOfferLetterTemplateMilestoneMonthEntryViewModel> milestoneMonths;

    public ProcurementGrantOfferLetterTemplateViewModel(long applicationId, String organisationName, String projectName, String competitionName, String projectManagerName,
                                                        List<ProcurementGrantOfferLetterTemplateMilestoneEntryViewModel> milestones,
                                                        List<ProcurementGrantOfferLetterTemplateMilestoneMonthEntryViewModel> milestoneMonths) {
        this.applicationId = applicationId;
        this.organisationName = organisationName;
        this.projectName = projectName;
        this.competitionName = competitionName;
        this.projectManagerName = projectManagerName;
        this.milestones = milestones;
        this.milestoneMonths = milestoneMonths;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public String getProjectManagerName() {
        return projectManagerName;
    }

    public List<ProcurementGrantOfferLetterTemplateMilestoneEntryViewModel> getMilestones() {
        return milestones;
    }

    public List<ProcurementGrantOfferLetterTemplateMilestoneMonthEntryViewModel> getMilestoneMonths() {
        return milestoneMonths;
    }

    public static class ProcurementGrantOfferLetterTemplateMilestoneEntryViewModel {
        private String description;
        private String deliverable;
        private String successCriteria;
        private Integer month;
        private LocalDate completionDate;

        public ProcurementGrantOfferLetterTemplateMilestoneEntryViewModel(String description, String deliverable, String successCriteria, Integer month, LocalDate completionDate) {
            this.description = description;
            this.deliverable = deliverable;
            this.successCriteria = successCriteria;
            this.month = month;
            this.completionDate = completionDate;
        }

        public String getDescription() {
            return description;
        }

        public String getDeliverable() {
            return deliverable;
        }

        public String getSuccessCriteria() {
            return successCriteria;
        }

        public Integer getMonth() {
            return month;
        }

        public LocalDate getCompletionDate() {
            return completionDate;
        }
    }

    public static class ProcurementGrantOfferLetterTemplateMilestoneMonthEntryViewModel {
        private long month;
        private BigInteger invoiceNet;
        private BigInteger vat;
        private List<Integer> milestoneNumbers;

        public ProcurementGrantOfferLetterTemplateMilestoneMonthEntryViewModel(long month, BigInteger invoiceNet, BigInteger vat, List<Integer> milestoneNumbers){
            this.month = month;
            this.invoiceNet = invoiceNet;
            this.vat = vat;
            this.milestoneNumbers = milestoneNumbers;
        }

        public long getMonth() {
            return month;
        }

        public BigInteger getInvoiceNet() {
            return invoiceNet;
        }

        public BigInteger getVat() {
            return vat;
        }

        public BigInteger getTotal() {
            return invoiceNet.add(vat);
        }

        public String getNumbers() {
            return String.join(", ", milestoneNumbers.stream().map(i -> i.toString()).collect(Collectors.toList()));
        }
    }
}