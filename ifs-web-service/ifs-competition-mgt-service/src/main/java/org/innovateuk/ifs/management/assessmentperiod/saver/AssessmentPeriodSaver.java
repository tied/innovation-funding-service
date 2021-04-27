package org.innovateuk.ifs.management.assessmentperiod.saver;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.AssessmentPeriodResource;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competition.service.AssessmentPeriodRestService;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.management.assessmentperiod.form.AssessmentPeriodForm;
import org.innovateuk.ifs.management.assessmentperiod.form.ManageAssessmentPeriodsForm;
import org.innovateuk.ifs.management.competition.setup.milestone.form.MilestoneRowForm;
import org.innovateuk.ifs.util.TimeZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.service.ServiceResult.*;
import static org.innovateuk.ifs.competition.resource.MilestoneType.assessmentPeriodValues;

@Component
public class AssessmentPeriodSaver {

    @Autowired
    private MilestoneRestService milestoneRestService;

    @Autowired
    private AssessmentPeriodRestService assessmentPeriodRestService;

    public ServiceResult<Void> save(long competitionId, ManageAssessmentPeriodsForm form) {
        List<MilestoneResource> existingMilestones = getExistingAssessmentPeriodMilestoneResources(competitionId);

        return aggregate(IntStream.range(0, form.getAssessmentPeriods().size()).mapToObj((index) -> {
            AssessmentPeriodForm assessmentPeriodForm = form.getAssessmentPeriods().get(index);
            long assessmentPeriodId = assessmentPeriodForm.getAssessmentPeriodId();
            return assessmentPeriodForm.getMilestoneEntries().values().stream()
                    .map(milestoneRowForm ->
                        validate(index, milestoneRowForm)
                            .andOnSuccess(() -> {
                                Optional<MilestoneResource> matchingMilestoneResource = existingMilestones.stream()
                                        .filter(m ->
                                                m.getAssessmentPeriodId().equals(assessmentPeriodId)
                                                        && m.getType() == milestoneRowForm.getMilestoneType())
                                        .findAny();

                                ZonedDateTime date = milestoneRowForm.getMilestoneAsZonedDateTime();
                                if (matchingMilestoneResource.isPresent()) {
                                    if (isEditable(matchingMilestoneResource.get())) {
                                        matchingMilestoneResource.get().setDate(date);
                                        return milestoneRestService.updateMilestone(matchingMilestoneResource.get()).toServiceResult();
                                    }
                                    return serviceSuccess();
                                } else {
                                    MilestoneResource milestone = new MilestoneResource();
                                    milestone.setDate(date);
                                    milestone.setType(milestoneRowForm.getMilestoneType());
                                    milestone.setCompetitionId(competitionId);
                                    milestone.setAssessmentPeriodId(assessmentPeriodId);
                                    return milestoneRestService.create(milestone).toServiceResult().andOnSuccessReturnVoid();
                                }
                            })
            );
        })
        .flatMap(Function.identity())
        .collect(Collectors.toList()))
                .andOnSuccessReturnVoid();
}

    private boolean isEditable(MilestoneResource milestone) {
        return milestone.getDate() == null || milestone.getDate().isAfter(ZonedDateTime.now());
    }

    private List<MilestoneResource> getExistingAssessmentPeriodMilestoneResources(long competitionId) {
        return milestoneRestService.getAllMilestonesByCompetitionId(competitionId).getSuccess()
                .stream()
                .filter(e -> MilestoneType.assessmentPeriodValues().contains(e.getType()))
                .collect(Collectors.toList());
    }

    public void createNewAssessmentPeriod(long competitionId) {
        AssessmentPeriodResource period = new AssessmentPeriodResource();
        period.setCompetitionId(competitionId);
        AssessmentPeriodResource savedPeriod = assessmentPeriodRestService.create(period).getSuccess();
        assessmentPeriodValues().forEach(t -> milestoneRestService.create(new MilestoneResource(t, null, competitionId, savedPeriod.getId())).getSuccess());
    }

    private ServiceResult<Void> validate(int index, MilestoneRowForm milestone) {
        Integer day = milestone.getDay();
        Integer month = milestone.getMonth();
        Integer year = milestone.getYear();
        String fieldName = String.format("assessmentPeriods[%d].milestoneEntries[%s]", index, milestone.getMilestoneType());
        String fieldValidationError = milestone.getMilestoneType().getAlwaysOpenDescription();
        boolean dateFieldsIncludeNull = (day == null || month == null || year == null);
        if((dateFieldsIncludeNull || !isMilestoneDateValid(day, month, year))) {
            return serviceFailure(fieldError(fieldName, "", "error.milestone.invalid", fieldValidationError));
        }
        return serviceSuccess();
    }

    private boolean isMilestoneDateValid(Integer day, Integer month, Integer year) {
        try{
            TimeZoneUtil.fromUkTimeZone(year, month, day);
            return year <= 9999;
        }
        catch(DateTimeException dte){
            return false;
        }
    }
}
