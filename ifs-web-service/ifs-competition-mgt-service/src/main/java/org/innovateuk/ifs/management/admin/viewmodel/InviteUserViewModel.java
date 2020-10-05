package org.innovateuk.ifs.management.admin.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.innovateuk.ifs.management.admin.form.InviteUserView;
import org.innovateuk.ifs.user.resource.Role;

import java.util.Set;

public class InviteUserViewModel {

    private InviteUserView type;

    private Set<Role> roles;

    private boolean cofunderEnabled;

    public InviteUserViewModel(InviteUserView type, Set<Role> roles, boolean cofunderEnabled)
    {
        this.type = type;
        this.roles = roles;
        this.cofunderEnabled = cofunderEnabled;
    }

    public InviteUserView getType() {
        return type;
    }

    public void setType(InviteUserView type) {
        this.type = type;
    }

    public String getTypeName() {
        return type.getName();
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isCofunderEnabled() {
        return cofunderEnabled;
    }

    public void setCofunderEnabled(boolean cofunderEnabled) {
        this.cofunderEnabled = cofunderEnabled;
    }

    public boolean isInternal() {
        return type.equals(InviteUserView.INTERNAL_USER);
    }

    public boolean isExternal() {
        return type.equals(InviteUserView.EXTERNAL_USER);
    }

    public String getLinkTitle() {
        return cofunderEnabled ? "Back to select user role" : "Back to manage users";
    }

    public String getBackLink() {
        return cofunderEnabled ? "/admin/select-external-role" : "/admin/users/active";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(roles)
                .append(cofunderEnabled)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        InviteUserViewModel that = (InviteUserViewModel) obj;

        return new EqualsBuilder()
                .append(type, this.type)
                .append(roles, this.roles)
                .append(cofunderEnabled, this.cofunderEnabled)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("roles", roles)
                .toString();
    }
}
