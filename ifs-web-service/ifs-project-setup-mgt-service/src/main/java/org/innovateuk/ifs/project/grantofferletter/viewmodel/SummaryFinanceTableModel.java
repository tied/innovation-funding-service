package org.innovateuk.ifs.project.grantofferletter.viewmodel;

import java.math.BigDecimal;

/**
* Holder of values for the summary finance table, used on the grant offer letter template page
 */

public class SummaryFinanceTableModel {

    private final BigDecimal totalProjectCosts;
    private final BigDecimal totalProjectGrant;
    private final BigDecimal rateOfGrant;

    public SummaryFinanceTableModel(
            BigDecimal totalProjectCosts,
            BigDecimal totalProjectGrant,
            BigDecimal rateOfGrant
    ) {
        this.totalProjectCosts = totalProjectCosts;
        this.totalProjectGrant = totalProjectGrant;
        this.rateOfGrant = rateOfGrant;
    }

    public BigDecimal getTotalProjectCosts() {
        return totalProjectCosts;
    }

    public BigDecimal getTotalProjectGrant() {
        return totalProjectGrant;
    }

    public BigDecimal getRateOfGrant() {
        return rateOfGrant;
    }
}
