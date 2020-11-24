package org.innovateuk.ifs.competitionsetup.applicationformbuilder.template;

import com.google.common.collect.Lists;
import org.innovateuk.ifs.category.repository.ResearchCategoryRepository;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.CompetitionOrganisationConfig;
import org.innovateuk.ifs.competition.repository.GrantTermsAndConditionsRepository;
import org.innovateuk.ifs.competition.resource.CollaborationLevel;
import org.innovateuk.ifs.competition.resource.CompetitionTypeEnum;
import org.innovateuk.ifs.competitionsetup.applicationformbuilder.builder.SectionBuilder;
import org.innovateuk.ifs.organisation.domain.OrganisationType;
import org.innovateuk.ifs.organisation.repository.OrganisationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.TRUE;
import static org.innovateuk.ifs.competition.resource.ApplicationFinanceType.NO_FINANCES;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.CommonBuilders.*;
import static org.innovateuk.ifs.competitionsetup.applicationformbuilder.template.EoiTemplate.eoiDefaultQuestions;

@Component
public class HestaTemplate implements CompetitionTemplate {

    @Autowired
    private GrantTermsAndConditionsRepository grantTermsAndConditionsRepository;

    @Autowired
    private OrganisationTypeRepository organisationTypeRepository;

    @Autowired
    private ResearchCategoryRepository researchCategoryRepository;

    @Override
    public CompetitionTypeEnum type() {
        return CompetitionTypeEnum.HESTA;
    }

    @Override
    public Competition initializeOrganisationConfig(Competition competition) {
        if (competition.getCompetitionOrganisationConfig() == null) {
            CompetitionOrganisationConfig competitionOrganisationConfig = new CompetitionOrganisationConfig();
            competitionOrganisationConfig.setCompetition(competition);
            competition.setCompetitionOrganisationConfig(competitionOrganisationConfig);
            competitionOrganisationConfig.setInternationalLeadOrganisationAllowed(TRUE);
            competitionOrganisationConfig.setInternationalOrganisationsAllowed(TRUE);
        }

        return competition;
    }

    @Override
    public Competition copyTemplatePropertiesToCompetition(Competition competition) {
        Iterable<OrganisationType> organisationTypes = organisationTypeRepository.findAll();
        List<OrganisationType> organisationTypesList = Lists.newArrayList(organisationTypes);

        competition.setTermsAndConditions(grantTermsAndConditionsRepository.findFirstByNameOrderByVersionDesc("Hesta"));
        competition.setLeadApplicantTypes(organisationTypesList);
        competition.setAcademicGrantPercentage(100);
        competition.setMinProjectDuration(1);
        competition.setMaxProjectDuration(60);
        competition.setCollaborationLevel(CollaborationLevel.SINGLE_OR_COLLABORATIVE);
        competition.setUseResubmissionQuestion(false);
        competition.setApplicationFinanceType(NO_FINANCES);
        competition.setResubmission(false);

        return competition;
    }

    @Override
    public List<SectionBuilder> sections() {
        return newArrayList(
                projectDetails()
                        .withQuestions(newArrayList(
                                applicationTeam(),
                                applicationDetails(),
                                researchCategory(),
                                equalityDiversityAndInclusion(),
                                projectSummary(),
                                scope()
                        )),
                applicationQuestions()
                        .withQuestions(eoiDefaultQuestions()),
                eoiTermsAndConditions()
        );
    }

    private SectionBuilder eoiTermsAndConditions() {
        SectionBuilder termsSection = termsAndConditions();
        termsSection.getQuestions().get(0)
                .withMultipleStatuses(false);
        return termsSection
                .withDescription("You are agreeing to these by submitting your application.");
    }
}