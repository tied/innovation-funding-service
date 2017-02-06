package org.innovateuk.ifs.publiccontent.transactional;

import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentEventResource;
import org.innovateuk.ifs.publiccontent.mapper.ContentEventMapper;
import org.innovateuk.ifs.publiccontent.repository.ContentEventRepository;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PUBLIC_CONTENT_IDS_INCONSISTENT;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;

/**
 * Service for operations around the usage and processing of public content.
 */
@Service
public class PublicContentEventServiceImpl extends BaseTransactionalService implements PublicContentEventService {

    @Autowired
    private ContentEventRepository contentEventRepository;

    @Autowired
    private ContentEventMapper contentEventMapper;

    @Override
    public ServiceResult<Void> saveEvent(PublicContentEventResource eventResource) {
        contentEventRepository.save(contentEventMapper.mapToDomain(eventResource));
        return serviceSuccess();
    }

    @Override
    public ServiceResult<Void> resetAndSaveEvents(Long publicContentId, List<PublicContentEventResource> eventResources) {
        if(eventResourcesAllHaveIDOrEmpty(publicContentId, eventResources)) {
            contentEventRepository.deleteByPublicContentId(publicContentId);
            contentEventRepository.save(contentEventMapper.mapToDomain(eventResources));
            return serviceSuccess();
        }

        return serviceFailure(new Error(PUBLIC_CONTENT_IDS_INCONSISTENT));
    }

    private Boolean eventResourcesAllHaveIDOrEmpty(Long publicContentId, List<PublicContentEventResource> eventResources) {
        return eventResources.isEmpty() || eventResources.stream().allMatch(eventResource -> eventResource.getPublicContent().equals(publicContentId));
    }
}
