package org.innovateuk.ifs.project.invite.transactional;

import org.innovateuk.ifs.activitylog.resource.ActivityType;
import org.innovateuk.ifs.activitylog.transactional.ActivityLogService;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.finance.transactional.ProjectFinanceService;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.InviteOrganisation;
import org.innovateuk.ifs.invite.repository.InviteOrganisationRepository;
import org.innovateuk.ifs.notifications.resource.*;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.domain.OrganisationAddress;
import org.innovateuk.ifs.organisation.repository.OrganisationAddressRepository;
import org.innovateuk.ifs.project.core.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.ProjectParticipantRole;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.PartnerOrganisationRepository;
import org.innovateuk.ifs.project.core.repository.PendingPartnerProgressRepository;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.core.transactional.ProjectPartnerChangeService;
import org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration.EligibilityWorkflowHandler;
import org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration.PaymentMilestoneWorkflowHandler;
import org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration.ViabilityWorkflowHandler;
import org.innovateuk.ifs.project.invite.domain.ProjectPartnerInvite;
import org.innovateuk.ifs.project.invite.repository.ProjectPartnerInviteRepository;
import org.innovateuk.ifs.project.invite.resource.SendProjectPartnerInviteResource;
import org.innovateuk.ifs.project.invite.resource.SentProjectPartnerInviteResource;
import org.innovateuk.ifs.project.projectteam.domain.PendingPartnerProgress;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.INTERNATIONAL;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.ORGANISATION_ALREADY_EXISTS_FOR_PROJECT;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

@Service
public class ProjectPartnerInviteServiceImpl extends BaseTransactionalService implements ProjectPartnerInviteService {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ProjectPartnerInviteRepository projectPartnerInviteRepository;

    @Autowired
    private InviteOrganisationRepository inviteOrganisationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private ProjectInviteValidator projectInviteValidator;

    @Autowired
    private LoggedInUserSupplier loggedInUserSupplier;

    @Autowired
    private ProjectFinanceService projectFinanceService;

    @Autowired
    private PartnerOrganisationRepository partnerOrganisationRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private EligibilityWorkflowHandler eligibilityWorkflowHandler;

    @Autowired
    private ViabilityWorkflowHandler viabilityWorkflowHandler;

    @Autowired
    private PendingPartnerProgressRepository pendingPartnerProgressRepository;

    @Autowired
    private ProjectPartnerChangeService projectPartnerChangeService;

    @Autowired
    private OrganisationAddressRepository organisationAddressRepository;

    @Autowired
    private PaymentMilestoneWorkflowHandler paymentMilestoneWorkflowHandler;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    enum Notifications {
        INVITE_PROJECT_PARTNER_ORGANISATION
    }

    @Override
    @Transactional
    public ServiceResult<Void> invitePartnerOrganisation(long projectId, SendProjectPartnerInviteResource invite) {
        return find(projectRepository.findById(projectId), notFoundError(Project.class, projectId)).andOnSuccess(project ->
                projectInviteValidator.validate(projectId, invite).andOnSuccess(() -> {
                    InviteOrganisation inviteOrganisation = new InviteOrganisation();
                    inviteOrganisation.setOrganisationName(invite.getOrganisationName());
                    inviteOrganisation = inviteOrganisationRepository.save(inviteOrganisation);

                    ProjectPartnerInvite projectPartnerInvite = new ProjectPartnerInvite();
                    projectPartnerInvite.setInviteOrganisation(inviteOrganisation);
                    projectPartnerInvite.setEmail(invite.getEmail());
                    projectPartnerInvite.setName(invite.getUserName());
                    projectPartnerInvite.setHash(generateInviteHash());
                    projectPartnerInvite.setTarget(project);

                    projectPartnerInvite = projectPartnerInviteRepository.save(projectPartnerInvite);
                    return sendInviteNotification(projectPartnerInvite)
                            .andOnSuccessReturnVoid((sentInvite) -> sentInvite.send(loggedInUserSupplier.get(), ZonedDateTime.now()));
                })
        );
    }

    private ServiceResult<ProjectPartnerInvite> sendInviteNotification(ProjectPartnerInvite projectPartnerInvite) {
        return find(projectPartnerInvite.getTarget().getLeadOrganisation(), notFoundError(Organisation.class)).andOnSuccess(leadOrganisation -> {
            NotificationSource from = systemNotificationSource;
            NotificationTarget to = new UserNotificationTarget(projectPartnerInvite.getName(), projectPartnerInvite.getEmail());

            Map<String, Object> notificationArguments = new HashMap<>();
            notificationArguments.put("inviteUrl", String.format("%s/project-setup/project/%d/partner-invite/%s/accept", webBaseUrl, projectPartnerInvite.getProject().getId(), projectPartnerInvite.getHash()));
            notificationArguments.put("applicationId", projectPartnerInvite.getTarget().getApplication().getId());
            notificationArguments.put("projectName", projectPartnerInvite.getTarget().getName());
            notificationArguments.put("leadOrganisationName", leadOrganisation.getOrganisation().getName());

            Notification notification = new Notification(from, to, Notifications.INVITE_PROJECT_PARTNER_ORGANISATION, notificationArguments);

            return notificationService.sendNotificationWithFlush(notification, EMAIL)
                    .andOnSuccessReturn(() -> projectPartnerInvite);
        });
    }

    @Override
    public ServiceResult<List<SentProjectPartnerInviteResource>> getPartnerInvites(long projectId) {
        return serviceSuccess(projectPartnerInviteRepository.findByProjectId(projectId).stream()
                .filter(invite -> invite.getStatus() == InviteStatus.SENT)
                .map(this::mapToSentResource)
                .collect(toList()));
    }

    private SentProjectPartnerInviteResource mapToSentResource(ProjectPartnerInvite projectPartnerInvite) {
        return new SentProjectPartnerInviteResource(projectPartnerInvite.getId(),
                projectPartnerInvite.getSentOn(),
                projectPartnerInvite.getProject().getName(),
                ofNullable(projectPartnerInvite.getUser()).map(User::getId).orElse(null),
                projectPartnerInvite.getStatus(),
                projectPartnerInvite.getInviteOrganisation().getOrganisationName(),
                projectPartnerInvite.getName(),
                projectPartnerInvite.getEmail(),
                projectPartnerInvite.getProject().getApplication().getId(),
                projectPartnerInvite.getProject().getApplication().getCompetition().getId());
    }

    @Override
    @Transactional
    public ServiceResult<Void> resendInvite(long inviteId) {
        return find(projectPartnerInviteRepository.findById(inviteId), notFoundError(ProjectPartnerInvite.class, inviteId))
                .andOnSuccess(this::sendInviteNotification)
                .andOnSuccessReturnVoid((sentInvite) -> sentInvite.resend(loggedInUserSupplier.get(), ZonedDateTime.now()));
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteInvite(long inviteId) {
        return find(projectPartnerInviteRepository.findById(inviteId), notFoundError(ProjectPartnerInvite.class, inviteId))
                .andOnSuccessReturnVoid(projectPartnerInviteRepository::delete);
    }

    @Override
    public ServiceResult<SentProjectPartnerInviteResource> getInviteByHash(String hash) {
        return find(projectPartnerInviteRepository.getByHash(hash), notFoundError(ProjectPartnerInvite.class, hash))
                .andOnSuccessReturn(this::mapToSentResource);
    }

    @Override
    @Transactional
    public ServiceResult<Void> acceptInvite(long inviteId, long organisationId) {
        return find(projectPartnerInviteRepository.findById(inviteId), notFoundError(ProjectPartnerInvite.class, inviteId))
                .andOnSuccess(invite ->
                        find(organisation(organisationId))
                                .andOnSuccess((organisation) -> {
                                    Project project = invite.getProject();
                                    invite.getInviteOrganisation().setOrganisation(organisation);

                                    PartnerOrganisation partnerOrganisation = new PartnerOrganisation(project, organisation, false);

                                    if (partnerOrganisationRepository.findOneByProjectIdAndOrganisationId(project.getId(), organisation.getId()) != null) {
                                        return serviceFailure(ORGANISATION_ALREADY_EXISTS_FOR_PROJECT);
                                    }

                                    partnerOrganisation = partnerOrganisationRepository.save(partnerOrganisation);

                                    linkAddressesToOrganisation(organisation, partnerOrganisation);

                                    pendingPartnerProgressRepository.save(new PendingPartnerProgress(partnerOrganisation));

                                    ProjectUser projectUser = new ProjectUser(invite.getUser(), project, ProjectParticipantRole.PROJECT_PARTNER, organisation);
                                    projectUser = projectUserRepository.save(projectUser);
                                    projectPartnerChangeService.updateProjectWhenPartnersChange(project.getId());
                                    projectFinanceService.createProjectFinance(project.getId(),
                                            organisation.getId());


                                    eligibilityWorkflowHandler.projectCreated(partnerOrganisation, projectUser);
                                    viabilityWorkflowHandler.projectCreated(partnerOrganisation, projectUser);

                                    Competition competition = project.getApplication().getCompetition();

                                    if (competition.isProcurement()) {
                                        paymentMilestoneWorkflowHandler.projectCreated(partnerOrganisation, projectUser);
                                    }

                                    if (competition.applicantNotRequiredForViabilityChecks(organisation.getOrganisationTypeEnum())) {
                                        viabilityWorkflowHandler.viabilityNotApplicable(partnerOrganisation, null);
                                    }
                                    if (competition.applicantNotRequiredForEligibilityChecks(organisation.getOrganisationTypeEnum())) {
                                        eligibilityWorkflowHandler.notRequestingFunding(partnerOrganisation, null);
                                    }

                                    invite.open();

                                    activityLogService.recordActivityByProjectIdAndOrganisationIdAndAuthorId(project.getId(), organisationId, invite.getSentBy().getId(), ActivityType.ORGANISATION_ADDED);
                                    return serviceSuccess();
                                }));
    }

    private void linkAddressesToOrganisation(Organisation organisation, PartnerOrganisation partnerOrganisation) {
        if (organisation.isInternational()) {
            Optional<OrganisationAddress> organisationAddress = organisationAddressRepository.findFirstByOrganisationIdAndAddressTypeIdOrderByModifiedOnDesc(organisation.getId(), INTERNATIONAL.getId());
            if (organisationAddress.isPresent()) {
                Address addressToLink;

                //if the organisation has an unlinked international address then this will be their first application and this is the address created first.
                if (organisationAddress.get().getApplicationAddresses().isEmpty()) {
                    addressToLink = organisationAddress.get().getAddress();
                    organisationAddress.get().setAddress(null);
                    organisationAddressRepository.delete(organisationAddress.get());
                } else {
                    addressToLink = new Address(organisationAddress.get().getAddress());
                }

                partnerOrganisation.setInternationalAddress(addressToLink);
            }
        }

    }
}