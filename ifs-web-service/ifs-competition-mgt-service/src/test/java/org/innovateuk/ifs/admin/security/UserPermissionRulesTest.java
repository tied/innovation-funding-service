package org.innovateuk.ifs.admin.security;


import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserCompositeId;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserStatus;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.innovateuk.ifs.commons.BaseIntegrationTest.getLoggedInUser;
import static org.innovateuk.ifs.commons.BaseIntegrationTest.setLoggedInUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class UserPermissionRulesTest extends BasePermissionRulesTest<UserPermissionRules> {

    @Override
    protected UserPermissionRules supplyPermissionRulesUnderTest() {
        return new UserPermissionRules();
    }

    @Test
    public void projectFinanceUserCanBeAccessed() {
        Role role = Role.PROJECT_FINANCE;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.internalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void compAdminUserCanBeAccessed() {
        Role role = Role.COMP_ADMIN;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.internalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void supportUserCanBeAccessed() {
        Role role = Role.SUPPORT;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.internalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void innovationLeadUserCanBeAccessed() {
        Role role = Role.INNOVATION_LEAD;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.internalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void projectFinanceUserCanBeEdited() {
        Role role = Role.PROJECT_FINANCE;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.canEditInternalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void compAdminUserCanBeEdited() {
        Role role = Role.COMP_ADMIN;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.canEditInternalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void supportUserCanBeEdited() {
        Role role = Role.SUPPORT;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.canEditInternalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void innovationLeadUserCanBeEdited() {
        Role role = Role.INNOVATION_LEAD;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(UserCompositeId.id(1L), getLoggedInUser()));
        assertFalse(rules.canEditInternalUser(UserCompositeId.id(1L), user));
    }

    @Test
    public void inactiveUserCannotBeEdited() {
        Role role = Role.INNOVATION_LEAD;
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.INACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertFalse(rules.canEditInternalUser(UserCompositeId.id(1L), getLoggedInUser()));
    }

    @Before
    public void setUp() {
        super.setUp();
        UserResource user = newUserResource().withRolesGlobal(singletonList(Role.IFS_ADMINISTRATOR)).build();
        setLoggedInUser(user);
    }
}
