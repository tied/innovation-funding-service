package org.innovateuk.ifs.heukar.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.heukar.domain.HeukarPartnerOrganisation;
import org.innovateuk.ifs.heukar.mapper.HeukarPartnerOrganisationMapper;
import org.innovateuk.ifs.heukar.repository.HeukarPartnerOrganisationRepository;
import org.innovateuk.ifs.organisation.resource.HeukarPartnerOrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;


@Service
public class HeukarPartnerOrganisationServiceImpl implements HeukarPartnerOrganisationService {

    @Autowired
    private HeukarPartnerOrganisationRepository heukarPartnerOrganisationRepository;

    @Autowired
    private HeukarPartnerOrganisationMapper mapper;

    @Override
    public ServiceResult<List<HeukarPartnerOrganisationResource>> findByApplicationId(long applicationId) {
        List<HeukarPartnerOrganisationResource> collect = heukarPartnerOrganisationRepository.findAllByApplicationId(applicationId).stream()
                .map(domain -> mapper.mapToResource(domain))
                .collect(Collectors.toList());

        return ServiceResult.serviceSuccess(collect);
    }

    @Override
    public ServiceResult<HeukarPartnerOrganisation> addNewPartnerOrgToApplication(long applicationId, long organisationTypeId) {
        return ServiceResult.serviceSuccess(heukarPartnerOrganisationRepository.save(mapper
                .mapWithApplicationIdToDomain(applicationId, organisationTypeId)));
    }

    @Override
    public ServiceResult<HeukarPartnerOrganisation> updatePartnerOrganisation(Long id, long organisationTypeId) {
        Optional<HeukarPartnerOrganisation> existing = heukarPartnerOrganisationRepository.findById(id);
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Partner org with id " + id + " does not exist");
        }
        HeukarPartnerOrganisation entity = mapper.mapExistingToDomain(id, existing.get().getApplicationId(), organisationTypeId);
        return ServiceResult.serviceSuccess(heukarPartnerOrganisationRepository.save(entity));
    }

    @Override
    public ServiceResult<Void> deletePartnerOrganisation(Long id) {
        heukarPartnerOrganisationRepository.delete(mapper.mapIdToDomain(id));
        return ServiceResult.serviceSuccess();
    }

    @Override
    public ServiceResult<HeukarPartnerOrganisationResource> findOne(Long id) {
        return find(heukarPartnerOrganisationRepository.findById(id), notFoundError(HeukarPartnerOrganisation.class))
                .andOnSuccessReturn(partnerOrganisation -> mapper.mapToResource(partnerOrganisation));
    }

}
