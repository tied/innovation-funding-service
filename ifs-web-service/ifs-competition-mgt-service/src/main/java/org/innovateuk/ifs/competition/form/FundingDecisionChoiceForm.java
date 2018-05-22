package org.innovateuk.ifs.competition.form;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Contains the Funding Decision choice value.
 */
public class FundingDecisionChoiceForm {
    @NotBlank
    private String fundingDecision;

    public String getFundingDecision() {
        return fundingDecision;
    }

    public void setFundingDecision(String fundingDecision) {
        this.fundingDecision = fundingDecision;
    }
}
