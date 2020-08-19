package org.innovateuk.ifs.organisation.resource;

import java.util.EnumSet;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

public enum OrganisationTypeEnum {
    BUSINESS(1),
    RESEARCH(2),
    RTO(3),
    PUBLIC_SECTOR_OR_CHARITY(4),
    UNIVERSITY(5),
    CATAPULT(6);

    private static final EnumSet<OrganisationTypeEnum> researchParticipationTypes =
            EnumSet.of(RESEARCH, RTO, PUBLIC_SECTOR_OR_CHARITY);

    private static final EnumSet<OrganisationTypeEnum> knowledgeBaseCollaboratorTypes =
            EnumSet.of(BUSINESS, PUBLIC_SECTOR_OR_CHARITY);

    public static final EnumSet<OrganisationTypeEnum> knowledgeBaseTypes =
            EnumSet.of(CATAPULT, RTO, UNIVERSITY);

    private final long id;

    OrganisationTypeEnum(long organisationTypeId) {
        this.id = organisationTypeId;
    }

    public long getId() {
        return id;
    }

    public static OrganisationTypeEnum getFromId(long organisationTypeId) {
        return simpleFindFirst(values(), v -> v.id == organisationTypeId).orElse(null);
    }

    public static boolean isResearch(OrganisationTypeEnum organisationType) {
        return organisationType.equals(RESEARCH);
    }

    public static boolean isResearch(long organisationTypeId) {
        return isResearch(getFromId(organisationTypeId));
    }

    public static boolean isResearchParticipationType(OrganisationTypeEnum organisationType) {
        return researchParticipationTypes.contains(organisationType);
    }

    public static boolean isKnowledgeBaseType(OrganisationTypeEnum organisationType) {
        return knowledgeBaseTypes.contains(organisationType);
    }

    public static boolean isResearchParticipationOrganisation(long organisationTypeId) {
        return isResearchParticipationType(getFromId(organisationTypeId));
    }

    public static boolean isKnowledgeBaseCollaborator(OrganisationTypeEnum organisationType) {
        return knowledgeBaseCollaboratorTypes.contains(organisationType);
    }

    public static boolean isValidKnowledgeBaseCollaborator(long organisationTypeId) {
        return isKnowledgeBaseCollaborator(getFromId(organisationTypeId));
    }
}