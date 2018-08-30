package org.innovateuk.ifs.eugrant.organisation.controller;

import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.address.service.AddressRestService;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.eugrant.EuOrganisationType;
import org.innovateuk.ifs.eugrant.organisation.form.AddressForm;
import org.innovateuk.ifs.eugrant.organisation.form.OrganisationCreationForm;
import org.innovateuk.ifs.eugrant.organisation.form.OrganisationTypeForm;
import org.innovateuk.ifs.eugrant.organisation.service.OrganisationCookieService;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.service.OrganisationSearchRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Provides a base class for each of the organisation registration controllers.
 */
public abstract class AbstractOrganisationCreationController {

    protected static final String BASE_URL = "/organisation/create";
    protected static final String LEAD_ORGANISATION_TYPE = "lead-organisation-type";
    protected static final String FIND_ORGANISATION = "find-organisation";
    protected static final String CONFIRM_ORGANISATION = "confirm-organisation";

    protected static final String ORGANISATION_FORM = "organisationForm";

    protected static final String SELECTED_POSTCODE = "selectedPostcode";
    protected static final String USE_SEARCH_RESULT_ADDRESS = "useSearchResultAddress";

    protected static final String TEMPLATE_PATH = "organisation";

    private static final String BINDING_RESULT_ORGANISATION_FORM = "org.springframework.validation.BindingResult.organisationForm";

    @Autowired
    protected OrganisationCookieService registrationCookieService;

    @Autowired
    protected OrganisationSearchRestService organisationSearchRestService;

    @Autowired
    protected AddressRestService addressRestService;

    protected Validator validator;

    @Autowired
    @Qualifier("mvcValidator")
    protected void setValidator(Validator validator) {
        this.validator = validator;
    }

    protected OrganisationCreationForm getFormDataFromCookie(OrganisationCreationForm organisationForm, Model model, HttpServletRequest request) {
        return processedOrganisationCreationFormFromCookie(model, request).
                orElseGet(() -> processedOrganisationCreationFormFromRequest(organisationForm, request));
    }

    private OrganisationCreationForm processedOrganisationCreationFormFromRequest(OrganisationCreationForm organisationForm, HttpServletRequest request){
        addOrganisationType(organisationForm, organisationTypeIdFromCookie(request));
        return organisationForm;
    }

    private Optional<OrganisationCreationForm> processedOrganisationCreationFormFromCookie(Model model, HttpServletRequest request) {
        Optional<OrganisationCreationForm> organisationCreationFormFromCookie = registrationCookieService.getOrganisationCreationCookieValue(request);
        organisationCreationFormFromCookie.ifPresent(organisationCreationForm -> {

            populateOrganisationCreationForm(request, organisationCreationForm);

            BindingResult bindingResult = new BeanPropertyBindingResult(organisationCreationForm, ORGANISATION_FORM);
            organisationFormValidate(organisationCreationForm, bindingResult);
            model.addAttribute(BINDING_RESULT_ORGANISATION_FORM, bindingResult);

            BindingResult addressBindingResult = new BeanPropertyBindingResult(organisationCreationForm.getAddressForm().getSelectedPostcode(), SELECTED_POSTCODE);
            organisationFormAddressFormValidate(organisationCreationForm, bindingResult, addressBindingResult);
        });
        return organisationCreationFormFromCookie;
    }

    private void populateOrganisationCreationForm(HttpServletRequest request, OrganisationCreationForm organisationCreationForm) {
        searchOrganisation(organisationCreationForm);
        addOrganisationType(organisationCreationForm, organisationTypeIdFromCookie(request));
    }

    protected void addOrganisationType(OrganisationCreationForm organisationForm, Optional<EuOrganisationType> organisationTypeId) {
        organisationTypeId.ifPresent(organisationForm::setOrganisationType);
    }

    protected void organisationFormAddressFormValidate(OrganisationCreationForm organisationForm, BindingResult bindingResult, BindingResult addressBindingResult) {
        if (organisationForm.isTriedToSave() && !organisationForm.isUseSearchResultAddress()) {
            AddressForm addressForm = organisationForm.getAddressForm();
            if (addressForm.getSelectedPostcode() != null) {
                validator.validate(addressForm.getSelectedPostcode(), addressBindingResult);
            } else if (!addressForm.isManualAddress()) {
                bindingResult.rejectValue(USE_SEARCH_RESULT_ADDRESS, "NotEmpty", "You should either fill in your address, or use the registered address as your operating address.");
            }
        }
    }

    protected Optional<EuOrganisationType> organisationTypeIdFromCookie(HttpServletRequest request) {
        Optional<OrganisationTypeForm> organisationTypeForm = registrationCookieService.getOrganisationTypeCookieValue(request);

        if (organisationTypeForm.isPresent()) {
            return Optional.ofNullable(organisationTypeForm.get().getOrganisationType());
        } else {
            return Optional.empty();
        }
    }

    private void organisationFormValidate(OrganisationCreationForm organisationForm, BindingResult bindingResult) {
        if (organisationForm.getAddressForm().isTriedToSearch() && isBlank(organisationForm.getAddressForm().getPostcodeInput())) {
            ValidationMessages.rejectValue(bindingResult, "addressForm.postcodeInput", "EMPTY_POSTCODE_SEARCH");
        }
        validator.validate(organisationForm, bindingResult);
    }

    private void searchOrganisation(OrganisationCreationForm organisationForm) {
        if (organisationForm.isOrganisationSearching()) {
            if (isNotBlank(organisationForm.getOrganisationSearchName())) {
                String trimmedSearchString = StringUtils.normalizeSpace(organisationForm.getOrganisationSearchName());
                List<OrganisationSearchResult> searchResults;
                searchResults = organisationSearchRestService.searchOrganisation(getIdFromEu(organisationForm.getOrganisationType()), trimmedSearchString)
                        .handleSuccessOrFailure(
                                f -> new ArrayList<>(),
                                s -> s
                        );
                organisationForm.setOrganisationSearchResults(searchResults);
            } else {
                organisationForm.setOrganisationSearchResults(new ArrayList<>());
            }
        }
    }

    /**
     * after user has selected a organisation, get the details and add it to the form and the model.
     */
    protected OrganisationSearchResult addSelectedOrganisation(OrganisationCreationForm organisationForm, Model model) {
        if (!organisationForm.isManualEntry() && isNotBlank(organisationForm.getSearchOrganisationId())) {
            OrganisationSearchResult organisationSearchResult = organisationSearchRestService.getOrganisation(getIdFromEu(organisationForm.getOrganisationType()), organisationForm.getSearchOrganisationId()).getSuccess();
            organisationForm.setOrganisationName(organisationSearchResult.getName());
            model.addAttribute("selectedOrganisation", organisationSearchResult);
            return organisationSearchResult;
        }
        return null;
    }

    //This is horrible
    private Long getIdFromEu(EuOrganisationType type) {
        return OrganisationTypeEnum.valueOf(type.name()).getId();
    }
}
