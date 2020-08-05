package org.innovateuk.ifs.application.forms.sections.yourprojectcosts.form;

import org.innovateuk.ifs.commons.validation.constraints.FieldRequiredIf;
import org.innovateuk.ifs.finance.resource.cost.AdditionalCompanyCost;

import java.math.BigInteger;

@FieldRequiredIf(required = "description", argument = "costIsNotNull", predicate = true, message = "{validation.field.must.not.be.blank}")
@FieldRequiredIf(required = "cost", argument = "descriptionIsNotNull", predicate = true, message = "{validation.field.must.not.be.blank}")
public class AdditionalCostAndDescription {

    private String description;
    private BigInteger cost;

    public AdditionalCostAndDescription() {}

    public AdditionalCostAndDescription(AdditionalCompanyCost cost) {
        this.description = cost.getDescription();
        this.cost = cost.getCost();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getCost() {
        return cost;
    }

    public void setCost(BigInteger cost) {
        this.cost = cost;
    }

    public boolean isCostIsNotNull() {
        return cost != null;
    }
    public boolean isDescriptionIsNotNull() {
        return description != null;
    }
}
