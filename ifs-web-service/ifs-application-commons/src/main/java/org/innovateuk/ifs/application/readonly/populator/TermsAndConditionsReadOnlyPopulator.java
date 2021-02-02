package org.innovateuk.ifs.application.readonly.populator;

import org.innovateuk.ifs.application.readonly.ApplicationReadOnlyData;
import org.innovateuk.ifs.application.readonly.ApplicationReadOnlySettings;
import org.innovateuk.ifs.application.readonly.viewmodel.TermsAndConditionsReadOnlyViewModel;
import org.innovateuk.ifs.application.readonly.viewmodel.TermsAndConditionsRowReadOnlyViewModel;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.FundingRules;
import org.innovateuk.ifs.competition.resource.GrantTermsAndConditionsResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.innovateuk.ifs.application.readonly.viewmodel.TermsAndConditionsRowReadOnlyViewModel.TermsAndConditionsRowReadOnlyViewModelBuilder.aTermsAndConditionsRowReadOnlyViewModel;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.TERMS_AND_CONDITIONS;
import static org.innovateuk.ifs.util.TermsAndConditionsUtil.VIEW_TERMS_AND_CONDITIONS_INVESTOR_PARTNERSHIPS;
import static org.innovateuk.ifs.util.TermsAndConditionsUtil.VIEW_TERMS_AND_CONDITIONS_OTHER;

@Component
public class TermsAndConditionsReadOnlyPopulator implements QuestionReadOnlyViewModelPopulator<TermsAndConditionsReadOnlyViewModel> {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private OrganisationRestService organisationRestService;

    @Autowired
    private ApplicationFinanceRestService applicationFinanceRestService;

    @Value("${ifs.subsidy.control.northern.ireland.enabled}")
    private boolean northernIrelandSubsidyControlToggle;

    @Override
    public TermsAndConditionsReadOnlyViewModel populate(QuestionResource question, ApplicationReadOnlyData data, ApplicationReadOnlySettings settings) {
        return new TermsAndConditionsReadOnlyViewModel(
                data,
                question,
                data.getCompetition().getFundingRules() == FundingRules.SUBSIDY_CONTROL && northernIrelandSubsidyControlToggle,
                getPartners(data, question),
                termsAndConditionsTerminology(data.getCompetition())
        );
    }

    private List<TermsAndConditionsRowReadOnlyViewModel> getPartners(ApplicationReadOnlyData data, QuestionResource question) {
        List<Long> acceptedOrgs = new ArrayList<>();
        if (data.getApplication().isOpen()) {
            acceptedOrgs = sectionService.getCompletedSectionsByOrganisation(data.getApplication().getId())
                    .entrySet()
                    .stream()
                    .filter(t -> t.getValue().contains(question.getSection()))
                    .map(Map.Entry::getKey)
                    .collect(toList());
        }

        List<Long> finalAcceptedOrgs = acceptedOrgs;
        Supplier<Map<Long, ApplicationFinanceResource>> supplier = financeSupplier(data.getApplication().getId());
        return organisationRestService.getOrganisationsByApplicationId(data.getApplication().getId()).getSuccess().stream()
                .map(organisation -> {
                    GrantTermsAndConditionsResource termsForPartner = getTermsForPartner(data, organisation, supplier);
                    return aTermsAndConditionsRowReadOnlyViewModel()
                            .withPartnerName(organisation.getName())
                            .withLead(data.getApplication().getLeadOrganisationId().equals(organisation.getId()))
                            .withAccepted(finalAcceptedOrgs.contains(organisation.getId()))
                            .withTermsId(termsForPartner.getId())
                            .withTermsName(termsForPartner.getName())
                            .build();
                })
                .sorted((o1, o2) -> o1.isLead() ? -1 : 1)
                .collect(toList());
    }

    private GrantTermsAndConditionsResource getTermsForPartner(ApplicationReadOnlyData data, OrganisationResource organisation, Supplier<Map<Long, ApplicationFinanceResource>> financeSupplier) {
        if (data.getCompetition().getFundingRules() == FundingRules.SUBSIDY_CONTROL
                && data.getCompetition().isFinanceType()
                && northernIrelandSubsidyControlToggle
                && competitionHasConfiguredDualTermsAndConditions(data.getCompetition())) {
            ApplicationFinanceResource finance = financeSupplier.get().get(organisation.getId());
            if (finance != null && Boolean.TRUE.equals(finance.getNorthernIrelandDeclaration())) {
                return data.getCompetition().getSubsidyControlTermsAndConditions();
            }
        }
        return data.getCompetition().getTermsAndConditions();
    }

    private boolean competitionHasConfiguredDualTermsAndConditions(CompetitionResource competition) {
        return competition.getSubsidyControlTermsAndConditions() != null
                && competition.getSubsidyControlTermsAndConditions().getId() != null;
    }

    private String termsAndConditionsTerminology(CompetitionResource competitionResource) {
        if(FundingType.INVESTOR_PARTNERSHIPS == competitionResource.getFundingType()) {
            return VIEW_TERMS_AND_CONDITIONS_INVESTOR_PARTNERSHIPS;
        }
        return VIEW_TERMS_AND_CONDITIONS_OTHER;
    }

    /* Doing this inside the supplier is pretty horrible. But stops us doing a rest request for the finances inside the loop. */
    private Supplier<Map<Long, ApplicationFinanceResource>> financeSupplier(long applicationId) {
        final Map<Long, ApplicationFinanceResource>[] financeResourceMap = new Map[]{null};
        return () -> {
            if (financeResourceMap[0] == null) {
                financeResourceMap[0] = applicationFinanceRestService.getFinanceDetails(applicationId).getSuccess()
                        .stream()
                        .collect(toMap(
                                ApplicationFinanceResource::getOrganisation,
                                Function.identity()
                        ));
            }
            return financeResourceMap[0];
        };
    }
    @Override
    public Set<QuestionSetupType> questionTypes() {
        return singleton(TERMS_AND_CONDITIONS);
    }
}