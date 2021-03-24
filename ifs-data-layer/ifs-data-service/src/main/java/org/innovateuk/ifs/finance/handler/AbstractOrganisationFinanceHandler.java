package org.innovateuk.ifs.finance.handler;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.finance.domain.*;
import org.innovateuk.ifs.finance.handler.item.FinanceRowHandler;
import org.innovateuk.ifs.finance.repository.*;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.form.transactional.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

public abstract class AbstractOrganisationFinanceHandler implements OrganisationTypeFinanceHandler {
    private static final Log LOG = LogFactory.getLog(AbstractOrganisationFinanceHandler.class);

    @Autowired
    protected QuestionService questionService;

    @Autowired
    protected ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    protected ProjectFinanceRepository projectFinanceRepository;

    @Autowired
    protected ApplicationFinanceRowRepository applicationFinanceRowRepository;

    @Autowired
    protected ProjectFinanceRowRepository projectFinanceRowRepository;

    @Autowired
    private FinanceRowMetaFieldRepository financeRowMetaFieldRepository;

    @Override
    public Iterable<ApplicationFinanceRow> initialiseCostType(ApplicationFinance applicationFinance, FinanceRowType costType) {
        if (initialiseCostTypeSupported(costType)) {
            try {
                List<ApplicationFinanceRow> cost = getCostHandler(costType).initializeCost(applicationFinance);
                cost.forEach(c -> {
                    c.setType(costType);
                    c.setTarget(applicationFinance);
                });
                return applicationFinanceRowRepository.saveAll(cost);
            } catch (IllegalArgumentException e) {
                LOG.error(String.format("No FinanceRowHandler for type: %s", costType.getType()), e);
            }
        }
        return null;
    }

    @Override
    public Iterable<ProjectFinanceRow> initialiseCostType(ProjectFinance projectFinance, FinanceRowType costType) {
        if (initialiseCostTypeSupported(costType)) {
            try {
                List<ProjectFinanceRow> cost = getCostHandler(costType).initializeCost(projectFinance);
                cost.forEach(c -> {
                    c.setType(costType);
                    c.setTarget(projectFinance);
                });
                return projectFinanceRowRepository.saveAll(cost);
            } catch (IllegalArgumentException e) {
                LOG.error(String.format("No FinanceRowHandler for type: %s", costType.getType()), e);
            }
        }
        return null;
    }

    protected abstract boolean initialiseCostTypeSupported(FinanceRowType costType);

    protected abstract Map<FinanceRowType, FinanceRowCostCategory> createCostCategories(Competition competition, Finance finance);

    protected abstract Map<FinanceRowType, FinanceRowCostCategory> afterTotalCalculation(Map<FinanceRowType, FinanceRowCostCategory> costCategories);

    @Override
    public Map<FinanceRowType, FinanceRowCostCategory> getOrganisationFinances(long applicationFinanceId) {
        return find(applicationFinanceRepository.findById(applicationFinanceId), notFoundError(ApplicationFinance.class, applicationFinanceId)).andOnSuccessReturn(finance -> {
            List<FinanceRow<? extends Finance>> costs = getApplicationCosts(applicationFinanceId, finance);
            return updateCostCategoryValuesForTotals(addCostsAndTotalsToCategories(costs, finance.getApplication().getCompetition(), finance));
        }).getSuccess();
    }

    private List<FinanceRow<? extends Finance>> getApplicationCosts(long applicationFinanceId, ApplicationFinance finance) {
        List<ApplicationFinanceRow> applicationFinanceRows = applicationFinanceRowRepository.findByTargetId(applicationFinanceId);
        List<FinanceRow<? extends Finance>> financeRows = applicationFinanceRows.stream()
                .map(FinanceRow.class::cast)
                .collect(Collectors.toList());
        return filterKtpFecCostCategoriesIfRequired(finance, financeRows);
    }

    private List<FinanceRow<? extends Finance>> filterKtpFecCostCategoriesIfRequired(Finance finance, List<FinanceRow<? extends Finance>> financeRows) {
        if (finance.getApplication().getCompetition().isKtp()) {
            financeRows = financeRows.stream()
                    .filter(financeRow -> BooleanUtils.isFalse(finance.getFecModelEnabled())
                            ? !FinanceRowType.getFecSpecificFinanceRowTypes().contains(financeRow.getType())
                            : !FinanceRowType.getNonFecSpecificFinanceRowTypes().contains(financeRow.getType()))
                    .collect(Collectors.toList());
        }

        return financeRows;
    }

    @Override
    public Map<FinanceRowType, FinanceRowCostCategory> getProjectOrganisationFinances(long projectFinanceId) {
        return find(projectFinanceRepository.findById(projectFinanceId), notFoundError(ProjectFinance.class, projectFinanceId)).andOnSuccessReturn(finance -> {
            List<FinanceRow<? extends Finance>> costs = getProjectCosts(projectFinanceId, finance);
            return updateCostCategoryValuesForTotals(addCostsAndTotalsToCategories(costs, finance.getProject().getApplication().getCompetition(), finance));
        }).getSuccess();
    }

    private List<FinanceRow<? extends Finance>> getProjectCosts(long projectFinanceId, ProjectFinance finance) {
        List<ProjectFinanceRow> projectFinanceRows = projectFinanceRowRepository.findByTargetId(projectFinanceId);
        List<FinanceRow<? extends Finance>> financeRows = projectFinanceRows.stream()
                .map(FinanceRow.class::cast)
                .collect(Collectors.toList());
        return filterKtpFecCostCategoriesIfRequired(finance, financeRows);
    }

    private Map<FinanceRowType, FinanceRowCostCategory> addCostsAndTotalsToCategories(List<? extends FinanceRow> costs, Competition competition, Finance finance) {
        Map<FinanceRowType, FinanceRowCostCategory> costCategories = createCostCategories(competition, finance);
        costCategories = addCostsToCategories(costCategories, costs);
        costCategories = calculateTotals(costCategories);
        return costCategories;
    }

    private Map<FinanceRowType, FinanceRowCostCategory> updateCostCategoryValuesForTotals(Map<FinanceRowType, FinanceRowCostCategory> costCategories) {
        costCategories = calculateTotals(costCategories);
        return costCategories;
    }

    private Map<FinanceRowType, FinanceRowCostCategory> calculateTotals(Map<FinanceRowType, FinanceRowCostCategory> costCategories) {
        costCategories.values()
                .forEach(FinanceRowCostCategory::calculateTotal);
        return afterTotalCalculation(costCategories);
    }

    private Map<FinanceRowType, FinanceRowCostCategory> addCostsToCategories(Map<FinanceRowType, FinanceRowCostCategory> costCategories, List<? extends FinanceRow> costs) {
        costs.stream().forEach(c -> addCostToCategory(costCategories, c));
        return costCategories;
    }

    private void addCostToCategory(Map<FinanceRowType, FinanceRowCostCategory> costCategories, FinanceRow cost) {
        FinanceRowType costType = cost.getType();
        FinanceRowHandler financeRowHandler = getCostHandler(costType);
        FinanceRowItem costItem = financeRowHandler.toResource(cost);
        FinanceRowCostCategory financeRowCostCategory = costCategories.get(costType);
        financeRowCostCategory.addCost(costItem);
    }

    @Override
    public ApplicationFinanceRow toApplicationDomain(FinanceRowItem costItem) {
        return buildFinanceRowHandler(costItem).toApplicationDomain(costItem);
    }

    @Override
    public ProjectFinanceRow toProjectDomain(FinanceRowItem costItem) {
        return buildFinanceRowHandler(costItem).toProjectDomain(costItem);
    }

    private FinanceRowHandler buildFinanceRowHandler(FinanceRowItem costItem) {
        FinanceRowHandler financeRowHandler = getCostHandler(costItem.getCostType());
        List<FinanceRowMetaField> financeRowMetaFields = financeRowMetaFieldRepository.findAll();
        financeRowHandler.setCostFields(financeRowMetaFields);
        return financeRowHandler;
    }

    @Override
    public FinanceRowItem toResource(FinanceRow cost) {
        FinanceRowHandler financeRowHandler = getRowHandler(cost);
        return financeRowHandler.toResource(cost);
    }

    private FinanceRowHandler getRowHandler(FinanceRow cost) {
        return getCostHandler(cost.getType());
    }

    @Override
    public List<FinanceRowItem> toResources(List<? extends FinanceRow> costs) {
        return costs.stream().map(c -> toResource(c)).collect(Collectors.toList());
    }

    @Override
    public ApplicationFinanceRow updateCost(final ApplicationFinanceRow financeRow) {
        return find(applicationFinanceRowRepository.findById(financeRow.getId()), notFoundError(ApplicationFinanceRow.class, financeRow.getId()))
                .andOnSuccess(existing -> serviceSuccess(applicationFinanceRowRepository.save(financeRow))).getSuccess();
    }

    @Override
    public ApplicationFinanceRow addCost(ApplicationFinanceRow financeRow) {
        return applicationFinanceRowRepository.save(financeRow);
    }
}