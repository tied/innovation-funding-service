package org.innovateuk.ifs.management.competition.setup.core.form;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.WordUtils;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.util.TimeZoneUtil;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.Set;



/**
 * Default Milestone Form Entry without any validations for the Milestones form.
 */
public class GenericMilestoneRowForm {

    private static final Log LOG = LogFactory.getLog(GenericMilestoneRowForm.class);

    protected static final Set<MilestoneType> WITH_TIME_TYPES = ImmutableSet.of(MilestoneType.SUBMISSION_DATE, MilestoneType.REGISTRATION_DATE);
    protected static final Set<MilestoneType> WITH_MIDDAY_TIME = ImmutableSet.of(MilestoneType.ASSESSOR_ACCEPTS, MilestoneType.ASSESSOR_DEADLINE);

    @Min(value = 2000, message = "{validation.standard.date.format}")
    @Max(value = 9999, message = "{validation.standard.date.format}")
    protected Integer year;
    protected Integer month;
    protected Integer day;

    protected MilestoneTime time;

    protected MilestoneType milestoneType;
    protected String dayOfWeek;
    protected ZonedDateTime date;
    protected boolean editable;

    public GenericMilestoneRowForm() {
    }

    public GenericMilestoneRowForm(MilestoneType milestoneType, ZonedDateTime dateTime) {
        this(milestoneType, dateTime, true);
    }

    public GenericMilestoneRowForm(MilestoneType milestoneType, ZonedDateTime dateTime, boolean editable) {
        this.setMilestoneType(milestoneType);
        this.editable = editable;
        if(dateTime != null) {
            this.setDay(dateTime.getDayOfMonth());
            this.setMonth(dateTime.getMonth().getValue());
            this.setYear(dateTime.getYear());
            this.setDate(dateTime);
            if (isTimeOption()) {
                this.setTime(MilestoneTime.fromZonedDateTime(dateTime));
            }
        } else if (isMiddayTime()) {
            this.setTime(MilestoneTime.TWELVE_PM);
        }
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDayOfWeek() {
        return getNameOfDay();
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public MilestoneType getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(MilestoneType milestoneType) {
        this.milestoneType = milestoneType;
    }

    public String getMilestoneNameType() {
        return milestoneType.name();
    }

    public boolean isTimeOption() {
        return WITH_TIME_TYPES.contains(milestoneType);
    }

    public boolean isFirstMilestone() {
        return MilestoneType.OPEN_DATE.equals(milestoneType);
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public boolean isMiddayTime() {
        return WITH_MIDDAY_TIME.contains(milestoneType);
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public MilestoneTime getTime() {
        return time;
    }

    public void setTime(MilestoneTime time) {
        this.time = time;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isReadonly() {
        return !editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    protected String getNameOfDay() {
        String dayName =  getMilestoneDate(day, month, year);
        if (dayName == null) {
            dayOfWeek = "-";
        }
        else {
            try {
                dayOfWeek = WordUtils.capitalize(dayName.toLowerCase());
            } catch (Exception e) {
                LOG.trace(e);
            }
        }
        return dayOfWeek;
    }

    protected String getMilestoneDate (Integer day, Integer month, Integer year) {
        if (day != null && month != null && year != null) {
            try {
                return TimeZoneUtil.fromUkTimeZone(year, month, day).getDayOfWeek().name();
            } catch (DateTimeException e) {
                LOG.trace(e);
            }
        }

        return null;
    }

    public ZonedDateTime getMilestoneAsZonedDateTime() {

        if (day != null && month != null && year != null){
            if ( time != null && (isTimeOption() || isMiddayTime())) {
                return TimeZoneUtil.fromUkTimeZone(year, month, day, time.getHour());
            } else {
                return TimeZoneUtil.fromUkTimeZone(year, month, day);
            }
        } else {
            return null;
        }
    }
}
