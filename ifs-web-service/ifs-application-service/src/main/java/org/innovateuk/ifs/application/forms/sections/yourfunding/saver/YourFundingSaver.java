package org.innovateuk.ifs.application.forms.sections.yourfunding.saver;

import org.innovateuk.ifs.application.forms.sections.yourfunding.form.*;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.commons.exception.IFSRuntimeException;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.category.BaseOtherFundingCostCategory;
import org.innovateuk.ifs.finance.resource.cost.*;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRowRestService;
import org.innovateuk.ifs.finance.service.FinanceRowRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static java.math.RoundingMode.HALF_UP;
import static org.innovateuk.ifs.application.forms.sections.yourprojectcosts.form.AbstractCostRowForm.UNSAVED_ROW_PREFIX;
import static org.innovateuk.ifs.finance.resource.cost.FinanceRowItem.MAX_DECIMAL_PLACES;

@Component
public class YourFundingSaver extends AbstractYourFundingSaver {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationFinanceRestService applicationFinanceRestService;

    @Autowired
    private OrganisationRestService organisationRestService;

    @Autowired
    private ApplicationFinanceRowRestService financeRowRestService;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private ApplicationRestService applicationRestService;

    @Override
    protected FinanceRowRestService getFinanceRowService() {
        return financeRowRestService;
    }

    public ServiceResult<Void> save(long applicationId, long organisationId, AbstractYourFundingAmountForm<? extends BaseOtherFundingRowForm> form) {
        ApplicationFinanceResource finance = applicationFinanceRestService.getFinanceDetails(applicationId, organisationId).getSuccess();
        return super.save(finance, form);
    }

    public ServiceResult<Void> save(long applicationId, long organisationId, AbstractYourFundingPercentageForm<? extends BaseOtherFundingRowForm> form) {
        ApplicationFinanceResource finance = applicationFinanceRestService.getFinanceDetails(applicationId, organisationId).getSuccess();
        return super.save(finance, form);
    }

    public Optional<Long> autoSave(String field, String value, long applicationId, UserResource user) {
        OrganisationResource organisation = organisationRestService.getByUserAndApplicationId(user.getId(), applicationId).getSuccess();
        ApplicationFinanceResource finance = applicationFinanceRestService.getFinanceDetails(applicationId, organisation.getId()).getSuccess();
        try {
            if (field.equals("grantClaimPercentage")) {
                GrantClaimPercentage grantClaim = (GrantClaimPercentage) finance.getGrantClaim();
                grantClaim.setPercentage(new BigDecimal(value).setScale(MAX_DECIMAL_PLACES, HALF_UP));
                getFinanceRowService().update(grantClaim).getSuccess();
            } else if (field.equals("amount")) {
                GrantClaimAmount grantClaim = (GrantClaimAmount) finance.getGrantClaim();
                grantClaim.setAmount(new BigDecimal(value));
                getFinanceRowService().update(grantClaim).getSuccess();
            } else if (field.equals("otherFunding")) {
                ApplicationResource application = applicationRestService.getApplicationById(applicationId).getSuccess();
                CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
                FinanceRowType type = competition.isKtp() ? FinanceRowType.PREVIOUS_FUNDING : FinanceRowType.OTHER_FUNDING;
                BaseOtherFundingCostCategory otherFundingCategory = (BaseOtherFundingCostCategory) finance.getFinanceOrganisationDetails(type);
                BaseOtherFunding otherFunding = otherFundingCategory.getOtherFunding();
                otherFunding.setOtherPublicFunding(Boolean.parseBoolean(value) ? "Yes" : "No");
                getFinanceRowService().update(otherFunding).getSuccess();
            } else if (field.startsWith("otherFundingRows")) {
                String id = field.substring(field.indexOf('[') + 1, field.indexOf(']'));
                String rowField = field.substring(field.indexOf("].") + 2);
                BaseOtherFunding cost;

                if (id.startsWith(UNSAVED_ROW_PREFIX)) {
                    ApplicationResource application = applicationRestService.getApplicationById(applicationId).getSuccess();
                    CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
                    if (competition.isKtp()) {
                        cost = (PreviousFunding) getFinanceRowService().create(new PreviousFunding(finance.getId())).getSuccess();
                    } else {
                        cost = (OtherFunding) getFinanceRowService().create(new OtherFunding(finance.getId())).getSuccess();
                    }
                } else {
                    cost = (BaseOtherFunding) getFinanceRowService().get(Long.parseLong(id)).getSuccess();
                }

                switch (rowField) {
                    case "source":
                        cost.setFundingSource(value);
                        break;
                    case "date":
                        cost.setSecuredDate(value);
                        break;
                    case "fundingAmount":
                        cost.setFundingAmount(new BigDecimal(value));
                        break;
                    default:
                        throw new IFSRuntimeException(String.format("Auto save other funding field not handled %s", rowField), Collections.emptyList());
                }
                getFinanceRowService().update(cost);
                return Optional.of(cost.getId());
            } else {
                throw new IFSRuntimeException(String.format("Auto save field not handled %s", field), Collections.emptyList());
            }
        } catch (Exception e) {
            LOG.debug("Error auto saving", e);
            LOG.info(String.format("Unable to auto save field (%s) value (%s)", field, value));
        }
        return Optional.empty();
    }
}
