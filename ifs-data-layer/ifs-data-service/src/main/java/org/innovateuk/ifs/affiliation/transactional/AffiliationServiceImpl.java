package org.innovateuk.ifs.affiliation.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.Affiliation;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.AffiliationMapper;
import org.innovateuk.ifs.user.resource.AffiliationListResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * A Service that covers basic operations concerning Affiliations
 */
@Service
public class AffiliationServiceImpl extends BaseTransactionalService implements AffiliationService {

    @Autowired
    private AffiliationMapper affiliationMapper;

    @Override
    public ServiceResult<AffiliationListResource> getUserAffiliations(long userId) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId)).andOnSuccess(user ->
                            serviceSuccess(new AffiliationListResource(
                                    simpleMap(user.getAffiliations(), affiliationMapper::mapToResource)
                            ))
        );
    }

    @Override
    @Transactional
    public ServiceResult<Void> updateUserAffiliations(long userId, AffiliationListResource affiliations) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId)).andOnSuccess(user -> {
            List<Affiliation> targetAffiliations = user.getAffiliations();
            targetAffiliations.clear();
            affiliationMapper.mapToDomain(affiliations.getAffiliationResourceList())
                    .forEach(affiliation -> {
                        affiliation.setUser(user);
                        targetAffiliations.add(affiliation);
                    });
            userRepository.save(user);
            return serviceSuccess();
        });
    }
}
