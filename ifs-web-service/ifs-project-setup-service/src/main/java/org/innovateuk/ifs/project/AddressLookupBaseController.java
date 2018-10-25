package org.innovateuk.ifs.project;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.AddressTypeResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.address.service.AddressRestService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.form.AddressForm;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.projectdetails.form.ProjectDetailsAddressForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This controller handles address lookups
 */
//TODO this is a candidate for refactor

public class AddressLookupBaseController {
    public static final String FORM_ATTR_NAME = "form";
    protected static final String MANUAL_ADDRESS = "manual-address";
    protected static final String SEARCH_ADDRESS = "search-address";
    protected static final String SELECT_ADDRESS = "select-address";

    @Autowired
    private AddressRestService addressRestService;

    protected void processAddressLookupFields(ProjectDetailsAddressForm form) {
        addAddressOptions(form);
        addSelectedAddress(form);
    }

    protected Optional<OrganisationAddressResource> getAddress(final OrganisationResource organisation, final OrganisationAddressType addressType) {
        return organisation.getAddresses().stream().filter(a -> OrganisationAddressType.valueOf(a.getAddressType().getName()).equals(addressType)).findFirst();
    }

    protected OrganisationAddressResource getOrganisationAddressResourceOrNull(
            ProjectDetailsAddressForm form,
            OrganisationResource organisationResource,
            OrganisationAddressType addressTypeToUseForNewAddress){
        OrganisationAddressResource organisationAddressResource = null;
        form.getAddressForm().setTriedToSave(true);
        AddressResource newAddressResource = form.getAddressForm().getSelectedPostcode();
        organisationAddressResource = new OrganisationAddressResource(organisationResource, newAddressResource, new AddressTypeResource((long)addressTypeToUseForNewAddress.ordinal(), addressTypeToUseForNewAddress.name()));
        return organisationAddressResource;
    }

    /**
     * Get the list of postcode options, with the entered postcode. Add those results to the form.
     */
    private void addAddressOptions(ProjectDetailsAddressForm projectDetailsAddressViewModelForm) {
        if (StringUtils.hasText(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput())) {
            AddressForm addressForm = projectDetailsAddressViewModelForm.getAddressForm();
            addressForm.setPostcodeOptions(searchPostcode(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput()));
            addressForm.setPostcodeInput(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput());
        }
    }

    /**
     * if user has selected a address from the dropdown, get it from the list, and set it as selected.
     */
    private void addSelectedAddress(ProjectDetailsAddressForm projectDetailsAddressViewModelForm) {
        AddressForm addressForm = projectDetailsAddressViewModelForm.getAddressForm();
        if (StringUtils.hasText(addressForm.getSelectedPostcodeIndex()) && addressForm.getSelectedPostcode() == null) {
            addressForm.setSelectedPostcode(addressForm.getPostcodeOptions().get(Integer.parseInt(addressForm.getSelectedPostcodeIndex())));
        }
    }

    private List<AddressResource> searchPostcode(String postcodeInput) {
        RestResult<List<AddressResource>> addressLookupRestResult = addressRestService.doLookup(postcodeInput);
        return addressLookupRestResult.handleSuccessOrFailure(
                failure -> new ArrayList<>(),
                addresses -> addresses);
    }

    protected FieldError createPostcodeSearchFieldError() {
        return new FieldError("form", "addressForm.postcodeInput", "", true, new String[] {"EMPTY_POSTCODE_SEARCH"}, new Object[] {}, null);
    }
}
