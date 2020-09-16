package org.innovateuk.ifs.competitionsetup.applicationformbuilder.fundingtype;

import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.FundingTypeTemplate;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.SectionBuilder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class InvestorPartnershipBuilder implements FundingTypeTemplate {

    @Override
    public FundingType type() {
        return FundingType.INVESTOR_PARTNERSHIPS;
    }

    @Override
    public List<SectionBuilder> sections() {
        return Collections.emptyList();
    }
}
