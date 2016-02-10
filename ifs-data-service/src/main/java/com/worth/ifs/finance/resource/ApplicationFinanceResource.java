package com.worth.ifs.finance.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.resource.category.CostCategory;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.finance.resource.cost.GrantClaim;
import com.worth.ifs.user.domain.OrganisationSize;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.EnumMap;

/**
 * Application finance reosurce holda the organisation's finance resources for an application
 */
public class ApplicationFinanceResource {
    private final Log log = LogFactory.getLog(getClass());

    Long id;
    private Long organisation;
    private Long application;
    private OrganisationSize organisationSize;
    private EnumMap<CostType, CostCategory> financeOrganisationDetails;

    public ApplicationFinanceResource(ApplicationFinance applicationFinance) {
        if (applicationFinance != null) {
            this.id = applicationFinance.getId();
            this.organisation = applicationFinance.getOrganisation().getId();
            this.application = applicationFinance.getApplication().getId();
            this.organisationSize = applicationFinance.getOrganisationSize();
        }
    }

    public ApplicationFinanceResource() {
    }

    public ApplicationFinanceResource(Long id, Long organisation, Long application, OrganisationSize organisationSize) {
        this.id = id;
        this.organisation = organisation;
        this.application = application;
        this.organisationSize = organisationSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Long organisation) {
        this.organisation = organisation;
    }

    public Long getApplication() {
        return application;
    }

    public void setApplication(Long application) {
        this.application = application;
    }

    public OrganisationSize getOrganisationSize() {
        return organisationSize;
    }

    public void setOrganisationSize(OrganisationSize organisationSize) {
        this.organisationSize = organisationSize;
    }

    public EnumMap<CostType, CostCategory> getFinanceOrganisationDetails() {
        return financeOrganisationDetails;
    }

    public void setFinanceOrganisationDetails(EnumMap<CostType, CostCategory> financeOrganisationDetails) {
        this.financeOrganisationDetails = financeOrganisationDetails;
    }

    public CostCategory getFinanceOrganisationDetails(CostType costType) {
        if (financeOrganisationDetails != null) {
            return financeOrganisationDetails.get(costType);
        } else {
            return null;
        }
    }

    public BigDecimal getTotal() {
        if (financeOrganisationDetails == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = financeOrganisationDetails.entrySet().stream()
                .filter(cat -> cat != null &&
                        cat.getValue() != null &&
                        cat.getValue().getTotal() != null)
                .filter(cat -> !cat.getValue().excludeFromTotalCost())
                .map(cat -> cat.getValue().getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total == null) {
            return BigDecimal.ZERO;
        }

        return total;
    }

    public GrantClaim getGrantClaim() {
        if (financeOrganisationDetails != null && financeOrganisationDetails.containsKey(CostType.FINANCE)) {
            CostCategory costCategory = financeOrganisationDetails.get(CostType.FINANCE);
            return costCategory.getCosts().stream()
                    .findAny()
                    .filter(c -> c instanceof GrantClaim)
                    .map(c -> (GrantClaim) c)
                    .orElse(null);
        } else {
            return null;
        }
    }

    public Integer getGrantClaimPercentage() {
        CostCategory costCategory = getFinanceOrganisationDetails(CostType.FINANCE);
        return (costCategory != null ? costCategory.getTotal().intValueExact() : 0);
    }

    public BigDecimal getTotalFundingSought() {
        BigDecimal totalFundingSought = getTotal()
                .multiply(new BigDecimal(getGrantClaimPercentage()))
                .divide(new BigDecimal(100))
                .subtract(getTotalOtherFunding());

        return totalFundingSought.max(BigDecimal.ZERO);
    }

    public BigDecimal getTotalContribution() {
        return getTotal()
                .subtract(getTotalOtherFunding())
                .subtract(getTotalFundingSought())
                .max(BigDecimal.ZERO);
    }

    public BigDecimal getTotalOtherFunding() {
        CostCategory otherFundingCategory = getFinanceOrganisationDetails(CostType.OTHER_FUNDING);
        return (otherFundingCategory != null ? otherFundingCategory.getTotal() : BigDecimal.ZERO);
    }

    @JsonIgnore
    public Log getLog() {
        return this.log;
    }
}