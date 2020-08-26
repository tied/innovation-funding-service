package org.innovateuk.ifs.finance.resource.category;

import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code DefaultCostCategory} implementation for {@link FinanceRowCostCategory}.
 * Default representation for costs and defaults to summing up the costs.
 */
public class DefaultCostCategory implements FinanceRowCostCategory {
    private List<FinanceRowItem> costs = new ArrayList<>();
    private BigDecimal total = ZERO_COST;

    @Override
    public List<FinanceRowItem> getCosts() {
        return costs;
    }

    @Override
    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public void calculateTotal() {
        total = costs.stream()
                .map(c -> c.getTotal() == null ? ZERO_COST : c.getTotal())
                .reduce(ZERO_COST, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void addCost(FinanceRowItem costItem) {
        if(costItem!=null) {
            costs.add(costItem);
        }
    }

    @Override
    public boolean excludeFromTotalCost() {
        return false;
    }

    public void setCosts(List<FinanceRowItem> costItems) {
        costs = costItems;
    }
}
