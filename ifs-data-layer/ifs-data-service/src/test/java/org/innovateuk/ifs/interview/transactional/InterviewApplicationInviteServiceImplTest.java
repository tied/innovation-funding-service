package org.innovateuk.ifs.interview.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.interview.domain.InterviewAssignment;
import org.innovateuk.ifs.interview.domain.InterviewAssignmentMessageOutcome;
import org.innovateuk.ifs.interview.resource.InterviewAssignmentState;
import org.innovateuk.ifs.invite.resource.ApplicantInterviewInviteResource;
import org.innovateuk.ifs.invite.resource.AssessorInviteSendResource;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.innovateuk.ifs.workflow.domain.ActivityType;
import org.innovateuk.ifs.workflow.resource.State;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.interview.builder.InterviewAssignmentBuilder.newInterviewAssignment;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class InterviewApplicationInviteServiceImplTest extends BaseServiceUnitTest<InterviewApplicationInviteService> {

    private static final long COMPETITION_ID = 1L;
    private static final Organisation LEAD_ORGANISATION = newOrganisation().withName("lead org").build();
    private static final ActivityState PENDING_FEEDBACK_ACTIVITY_STATE = new ActivityState(ActivityType.ASSESSMENT_INTERVIEW_PANEL, State.PENDING);

    @Override
    protected InterviewApplicationInviteServiceImpl supplyServiceUnderTest() {
        return new InterviewApplicationInviteServiceImpl();
    }

    @Test
    public void getEmailTemplate() {
        when(notificationTemplateRendererMock.renderTemplate(eq(systemNotificationSourceMock), any(NotificationTarget.class), eq("invite_applicants_to_interview_panel_text.txt"),
                any(Map.class))).thenReturn(serviceSuccess("Content"));

        ServiceResult<ApplicantInterviewInviteResource> result = service.getEmailTemplate();

        assertTrue(result.isSuccess());
        assertEquals(result.getSuccess().getContent(),"Content");
    }

    @Test
    public void sendInvites() {
        AssessorInviteSendResource sendResource = new AssessorInviteSendResource("Subject", "Content");
        List<InterviewAssignment> interviewAssignments = newInterviewAssignment()
                .withParticipant(
                        newProcessRole()
                                .withRole(Role.INTERVIEW_LEAD_APPLICANT)
                                .withOrganisationId(LEAD_ORGANISATION.getId())
                                .withUser(newUser()
                                        .withFirstName("Someone").withLastName("SomeName").withEmailAddress("someone@example.com").build())
                                .build()
                )
                .withTarget(
                        newApplication()
                                .withCompetition(newCompetition().build())
                                .build()

                )
                .build(1);

        InterviewAssignmentMessageOutcome outcome = new InterviewAssignmentMessageOutcome();
        outcome.setAssessmentInterviewPanel(interviewAssignments.get(0));
        outcome.setMessage(sendResource.getContent());
        outcome.setSubject(sendResource.getSubject());

        when(interviewAssignmentRepositoryMock.findByTargetCompetitionIdAndActivityStateState(
                COMPETITION_ID, InterviewAssignmentState.CREATED.getBackingState())).thenReturn(interviewAssignments);
        when(activityStateRepositoryMock.findOneByActivityTypeAndState(
                ActivityType.ASSESSMENT_INTERVIEW_PANEL, InterviewAssignmentState.AWAITING_FEEDBACK_RESPONSE.getBackingState())).thenReturn(PENDING_FEEDBACK_ACTIVITY_STATE);
        when(notificationSenderMock.sendNotification(any(Notification.class))).thenReturn(serviceSuccess(null));

        ServiceResult<Void> result = service.sendInvites(COMPETITION_ID, sendResource);

        assertTrue(result.isSuccess());
        verify(notificationSenderMock, only()).sendNotification(any(Notification.class));
        verify(interviewAssignmentWorkflowHandler).notifyInterviewPanel(interviewAssignments.get(0), outcome);
    }
}