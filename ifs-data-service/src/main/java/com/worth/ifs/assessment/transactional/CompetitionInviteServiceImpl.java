package com.worth.ifs.assessment.transactional;

import com.worth.ifs.assessment.mapper.CompetitionInviteMapper;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.invite.domain.CompetitionInvite;
import com.worth.ifs.invite.domain.CompetitionParticipant;
import com.worth.ifs.invite.domain.RejectionReason;
import com.worth.ifs.invite.repository.CompetitionInviteRepository;
import com.worth.ifs.invite.repository.CompetitionParticipantRepository;
import com.worth.ifs.invite.repository.RejectionReasonRepository;
import com.worth.ifs.invite.resource.CompetitionInviteResource;
import com.worth.ifs.invite.resource.RejectionReasonResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE;
import static com.worth.ifs.commons.error.CommonFailureKeys.COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE;
import static com.worth.ifs.util.EntityLookupCallbacks.find;

/**
 * Service for managing {@link com.worth.ifs.invite.domain.CompetitionInvite}s.
 */
@Service
public class CompetitionInviteServiceImpl implements CompetitionInviteService {

    @Autowired
    private CompetitionInviteRepository competitionInviteRepository;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Autowired
    private RejectionReasonRepository rejectionReasonRepository;

    @Autowired
    private CompetitionInviteMapper mapper;

    @Override
    public ServiceResult<CompetitionInviteResource> getInvite(String inviteHash) {
        return getByHash(inviteHash).andOnSuccessReturn(mapper::mapToResource);
    }

    @Override
    public ServiceResult<CompetitionInviteResource> openInvite(String inviteHash) {
        return getByHash(inviteHash).andOnSuccessReturn(invite -> mapper.mapToResource(openInvite(invite)));
    }

    @Override
    public ServiceResult<Void> acceptInvite(String inviteHash) {
        return getParticipantByInviteHash(inviteHash)
                .andOnSuccess(invite -> accept(invite))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Void> rejectInvite(String inviteHash, RejectionReasonResource rejectionReason, String rejectionComment) {
        return getRejectionReason(rejectionReason)
                .andOnSuccess(reason -> getParticipantByInviteHash(inviteHash)
                        .andOnSuccess(invite -> reject(invite, reason, rejectionComment))).andOnSuccessReturnVoid();
    }

    private ServiceResult<CompetitionInvite> getByHash(String inviteHash) {
        return find(competitionInviteRepository.getByHash(inviteHash), notFoundError(CompetitionInvite.class, inviteHash));
    }

    private CompetitionInvite openInvite(CompetitionInvite invite) {
        return competitionInviteRepository.save(invite.open());
    }

    private ServiceResult<CompetitionParticipant> getParticipantByInviteHash(String inviteHash) {
        return find(competitionParticipantRepository.getByInviteHash(inviteHash), notFoundError(CompetitionParticipant.class, inviteHash));
    }

    private ServiceResult<CompetitionParticipant> accept(CompetitionParticipant participant) {
        try {
            participant.accept();
        }
        catch (IllegalStateException e) {
            return ServiceResult.serviceFailure(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE);
        }
        return ServiceResult.serviceSuccess(competitionParticipantRepository.save(participant));
    }

    private ServiceResult<CompetitionParticipant> reject(CompetitionParticipant participant, RejectionReason rejectionReason, String rejectionComment) {
        try {
            participant.reject(rejectionReason, rejectionComment);
        }
        catch (IllegalStateException e) {
            return ServiceResult.serviceFailure(COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE);
        }
        return ServiceResult.serviceSuccess(competitionParticipantRepository.save(participant));
    }

    private ServiceResult<RejectionReason> getRejectionReason(final RejectionReasonResource rejectionReason) {
        return find(rejectionReasonRepository.findOne(rejectionReason.getId()), notFoundError(RejectionReason.class, rejectionReason.getId()));
    }
}
