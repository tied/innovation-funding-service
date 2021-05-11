package org.innovateuk.ifs.management.assessment.populator;

import org.apache.commons.collections4.map.LinkedMap;
import org.innovateuk.ifs.assessment.service.AssessmentPeriodService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.management.assessment.viewmodel.AssessorAssessmentPeriodChoiceViewModel;
import org.innovateuk.ifs.management.assessment.viewmodel.AssessorAssessmentPeriodChoiceViewModel.AssessmentPeriodViewModel;
import org.innovateuk.ifs.management.assessment.viewmodel.AssessorAssessmentPeriodChoiceViewModel.MilestoneViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AssessorAssessmentPeriodChoiceModelPopulator {

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private MilestoneRestService milestoneRestService;

    @Autowired
    private AssessmentPeriodService assessmentPeriodService;

    public AssessorAssessmentPeriodChoiceViewModel populate(long competitionId) {

        List<MilestoneResource> milestones = milestoneRestService.getAllMilestonesByCompetitionId(competitionId).getSuccess();

        Map<Long, List<MilestoneResource>> periodIdToMilestoneMap = milestones
                .stream()
                .filter(milestone -> milestone.getAssessmentPeriodId() != null)
                .collect(Collectors.groupingBy(MilestoneResource::getAssessmentPeriodId));
        List<AssessmentPeriodViewModel> assessmentPeriods = periodIdToMilestoneMap.entrySet().stream()
                .map(e -> {
                    LinkedMap<String, MilestoneViewModel> milestoneFormEntries = new LinkedMap<>();
                    e.getValue().forEach(milestone ->
                            milestoneFormEntries.put(milestone.getType().getMilestoneDescription(), populateMilestoneFormEntries(milestone))
                    );
                    AssessmentPeriodViewModel assessmentPeriodViewModel = new AssessmentPeriodViewModel();
                    assessmentPeriodViewModel.setAssessmentPeriodId(e.getKey());
                    assessmentPeriodViewModel.setMilestoneEntries(milestoneFormEntries);

                    String displayName = assessmentPeriodService.displayName(e.getKey(), competitionId);
                    assessmentPeriodViewModel.setDisplayName(displayName);

                    return assessmentPeriodViewModel;
                })
                .collect(Collectors.toList());
        CompetitionResource competitionResource = competitionRestService.getCompetitionById(competitionId).getSuccess();

        return new AssessorAssessmentPeriodChoiceViewModel(
                competitionId,
                competitionResource.getName(),
                assessmentPeriods
        );
    }

    private MilestoneViewModel populateMilestoneFormEntries(MilestoneResource milestone) {
        return new MilestoneViewModel(milestone.getType(), milestone.getDate());
    }
}
