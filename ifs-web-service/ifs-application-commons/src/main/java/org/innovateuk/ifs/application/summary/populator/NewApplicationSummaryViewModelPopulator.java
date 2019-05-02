package org.innovateuk.ifs.application.summary.populator;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.QuestionStatusRestService;
import org.innovateuk.ifs.application.service.SectionRestService;
import org.innovateuk.ifs.application.summary.ApplicationSummaryData;
import org.innovateuk.ifs.application.summary.ApplicationSummarySettings;
import org.innovateuk.ifs.application.summary.viewmodel.NewApplicationSummaryViewModel;
import org.innovateuk.ifs.application.summary.viewmodel.NewQuestionSummaryViewModel;
import org.innovateuk.ifs.application.summary.viewmodel.NewSectionSummaryViewModel;
import org.innovateuk.ifs.commons.exception.IFSRuntimeException;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toCollection;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

@Component
public class NewApplicationSummaryViewModelPopulator {

    private ApplicationRestService applicationRestService;

    private CompetitionRestService competitionRestService;

    private FormInputRestService formInputRestService;

    private FormInputResponseRestService formInputResponseRestService;

    private SectionRestService sectionRestService;

    private QuestionRestService questionRestService;

    private FinanceSummaryViewModelPopulator financeSummaryViewModelPopulator;

    private QuestionStatusRestService questionStatusRestService;

    private OrganisationRestService organisationRestService;

    private Map<QuestionSetupType, QuestionSummaryViewModelPopulator<?>> populatorMap;

    public NewApplicationSummaryViewModelPopulator(ApplicationRestService applicationRestService, CompetitionRestService competitionRestService, FormInputRestService formInputRestService, FormInputResponseRestService formInputResponseRestService, SectionRestService sectionRestService, QuestionRestService questionRestService, FinanceSummaryViewModelPopulator financeSummaryViewModelPopulator, QuestionStatusRestService questionStatusRestService, OrganisationRestService organisationRestService, List<QuestionSummaryViewModelPopulator<?>> populators) {
        this.applicationRestService = applicationRestService;
        this.competitionRestService = competitionRestService;
        this.formInputRestService = formInputRestService;
        this.formInputResponseRestService = formInputResponseRestService;
        this.sectionRestService = sectionRestService;
        this.questionRestService = questionRestService;
        this.financeSummaryViewModelPopulator = financeSummaryViewModelPopulator;
        this.questionStatusRestService = questionStatusRestService;
        this.organisationRestService = organisationRestService;
        this.populatorMap = new HashMap<>();
        populators.forEach(populator ->
                populator.questionTypes().forEach(type -> populatorMap.put(type, populator)));
    }

    public NewApplicationSummaryViewModel populate(long applicationId, UserResource user, ApplicationSummarySettings settings) {
        ApplicationResource application = applicationRestService.getApplicationById(applicationId).getSuccess();
        CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
        return populate(application, competition, user, settings);
    }

    public NewApplicationSummaryViewModel populate(ApplicationResource application, CompetitionResource competition, UserResource user, ApplicationSummarySettings settings) {
        List<QuestionResource> questions = questionRestService.findByCompetition(application.getCompetition()).getSuccess();
        List<FormInputResource> formInputs = formInputRestService.getByCompetitionId(competition.getId()).getSuccess();
        List<FormInputResponseResource> formInputResponses = formInputResponseRestService.getResponsesByApplicationId(application.getId()).getSuccess();
        Set<NewSectionSummaryViewModel> sectionViews = sectionRestService.getByCompetition(application.getCompetition()).getSuccess()
                .stream()
                .filter(section -> section.getParentSection() == null)
                .map(section -> sectionView(section, settings, new ApplicationSummaryData(application, competition, user, questions, formInputs, formInputResponses, getQuestionStatuses(application, user, settings))))
                .collect(toCollection(LinkedHashSet::new));

        return new NewApplicationSummaryViewModel(settings, sectionViews);
    }

    private NewSectionSummaryViewModel sectionView(SectionResource section, ApplicationSummarySettings settings, ApplicationSummaryData data) {
        if (!section.getChildSections().isEmpty()) {
            return sectionsWithChildren(section, settings, data);
        }
        Set<NewQuestionSummaryViewModel> questionViews = section.getQuestions()
                .stream()
                .map(questionId -> data.getQuestionIdToQuestion().get(questionId))
                .map(question ->  populateQuestionViewModel(question, data, settings))
                .collect(toCollection(LinkedHashSet::new));
        return new NewSectionSummaryViewModel(section.getName(), questionViews);
    }

    //Currently only the finance section has child sections.
    private NewSectionSummaryViewModel sectionsWithChildren(SectionResource section, ApplicationSummarySettings settings, ApplicationSummaryData data) {
        NewQuestionSummaryViewModel finance = financeSummaryViewModelPopulator.populate(data);
        return new NewSectionSummaryViewModel(section.getName(), asSet(finance));
    }

    public NewQuestionSummaryViewModel populateQuestionViewModel(QuestionResource question, ApplicationSummaryData data, ApplicationSummarySettings settings) {
        if (populatorMap.containsKey(question.getQuestionSetupType())) {
            return populatorMap.get(question.getQuestionSetupType()).populate(question, data);
        } else {
            throw new IFSRuntimeException("Populator not found for question type: " + question.getQuestionSetupType().name());
        }
    }

    private List<QuestionStatusResource> getQuestionStatuses(ApplicationResource application, UserResource user, ApplicationSummarySettings settings) {
        if (!settings.isIncludeStatuses()) {
            return Collections.emptyList();
        }
        OrganisationResource organisation = organisationRestService.getByUserAndApplicationId(user.getId(), application.getId()).getSuccess();
        return questionStatusRestService.findByApplicationAndOrganisation(application.getId(), organisation.getId()).getSuccess();
    }


}
