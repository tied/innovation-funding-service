package org.innovateuk.ifs.eugrant.contact.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;

public class EuContactForm extends BaseBindingResultTarget {

    @NotBlank(message = "{validation.standard.eugrant.notblank}")
    private String name;

    @NotBlank(message = "{validation.standard.eugrant.notblank}")
    private String jobTitle;

    @NotBlank(message = "{validation.standard.eugrant.notblank}")
    private String email;

    @NotBlank(message = "{validation.standard.eugrant.notblank}")
    private String telephone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EuContactForm that = (EuContactForm) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(jobTitle, that.jobTitle)
                .append(email, that.email)
                .append(telephone, that.telephone)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(jobTitle)
                .append(email)
                .append(telephone)
                .toHashCode();
    }
}
