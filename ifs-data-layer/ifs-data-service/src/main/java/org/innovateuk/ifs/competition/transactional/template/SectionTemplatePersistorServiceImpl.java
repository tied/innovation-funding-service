package org.innovateuk.ifs.competition.transactional.template;

import org.innovateuk.ifs.application.domain.Section;
import org.innovateuk.ifs.application.repository.SectionRepository;
import org.innovateuk.ifs.competition.domain.Competition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SectionTemplatePersistorServiceImpl implements SectionTemplatePersistorService {
    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private QuestionTemplatePersistorServiceImpl questionTemplatePersistorServiceServiceImpl;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Section> persistByPrecedingEntity(Competition template) {
        if (template.getSections() == null) {
            return null;
        }

        List<Section> sectionsWithoutParentSections = getTopLevelSections(template);

        new ArrayList<>(sectionsWithoutParentSections).forEach(section -> createChildSectionRecursively(template, null).apply(section));

        return sectionsWithoutParentSections;
    }

    @Override
    public void cleanForPrecedingEntity(Competition competition) {
        getTopLevelSections(competition).stream().forEach(section -> cleanForPrecedingEntityRecursively(section));
    }

    private List<Section> getTopLevelSections(Competition competition) {
        return competition.getSections().stream()
                    .filter(s -> s.getParentSection() == null)
                    .collect(Collectors.toList());
    }

    private List<Section> createChildSectionsRecursively(Competition competition, Section parentSectionTemplate) {
        return parentSectionTemplate.getChildSections().stream().map(section -> createChildSectionRecursively(competition, parentSectionTemplate).apply(section)).collect(Collectors.toList());
    }

    private Function<Section, Section> createChildSectionRecursively(Competition competition, Section parentSection) {
        return (Section section) -> {
            entityManager.detach(section);
            section.setCompetition(competition);
            section.setId(null);
            section.setParentSection(parentSection);
            sectionRepository.save(section);

            questionTemplatePersistorServiceServiceImpl.persistByPrecedingEntity(section);

            createChildSectionsRecursively(competition, section);

            return section;
        };
    }

    private void cleanForPrecedingEntityRecursively(Section section) {
        section.getChildSections().stream().forEach(s -> cleanForPrecedingEntityRecursively(s));

        questionTemplatePersistorServiceServiceImpl.cleanForPrecedingEntity(section);

        entityManager.detach(section);
        sectionRepository.delete(section);
    }
}
