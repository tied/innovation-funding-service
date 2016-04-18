package com.worth.ifs.validator.transactional;

import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.finance.transactional.CostService;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.validator.util.ValidationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to validate serveral objects
 */
@Service
public class ValidatorServiceImpl extends BaseTransactionalService implements ValidatorService {
    @Autowired
    private FormInputResponseRepository formInputResponseRepository;
    @Autowired
    CostService costService;

    private static final Log LOG = LogFactory.getLog(ValidatorServiceImpl.class);

    @Override
    public List<BindingResult> validateFormInputResponse(Long applicationId, Long formInputId){
        List<BindingResult> results = new ArrayList<>();
        List<FormInputResponse> response = formInputResponseRepository.findByApplicationIdAndFormInputId(applicationId, formInputId);
        if(response.size() > 1){
            for (FormInputResponse formInputResponse : response) {
                results.add(ValidationUtil.validateResponse(formInputResponse, false));
            }
        } else {
            results.add(ValidationUtil.validateResponse(response.get(0), false));
        }
        return results;
    }

    @Override
    public BindingResult validateFormInputResponse(Long applicationId, Long formInputId, Long markedAsCompleteById) {
        FormInputResponse response = formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(applicationId, markedAsCompleteById, formInputId);
        return ValidationUtil.validateResponse(response, false);
    }

    @Override
    public List<ValidationMessages> validateCostItem(Long applicationId, String costTypeName, Long questionId, Long markedAsCompleteById) {
        return getProcessRole(markedAsCompleteById).andOnSuccess(role -> {
            return costService.financeDetails(applicationId, role.getOrganisation().getId()).andOnSuccess(financeDetails -> {
                return costService.getCostItems(financeDetails.getId(), "", questionId).andOnSuccessReturn(costItems -> {
                    return ValidationUtil.validateCostItem(costItems);
                });
            });
        }).getSuccessObject();
    }
}
