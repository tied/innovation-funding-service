package org.innovateuk.ifs.invite.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.invite.resource.InviteUserResource;
import org.innovateuk.ifs.invite.resource.RoleInviteResource;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InviteUserRestServiceImplTest extends BaseRestServiceUnitTest<InviteUserRestServiceImpl> {

    private static final String inviteRestBaseUrl = "/inviteUser";

    @Override
    protected InviteUserRestServiceImpl registerRestServiceUnderTest() {
        return new InviteUserRestServiceImpl();
    }

    @Test
    public void saveUserInvite() throws Exception {
        InviteUserResource inviteUserResource = new InviteUserResource();
        String url = inviteRestBaseUrl + "/saveInvite";
        setupPostWithRestResultExpectations(url, inviteUserResource, HttpStatus.OK);

        RestResult<Void> result = service.saveUserInvite(inviteUserResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void getInvite() throws Exception {
        RoleInviteResource roleInviteResource = new RoleInviteResource();
        String url = inviteRestBaseUrl + "/getInvite/";
        String inviteHash = "hash";
        setupGetWithRestResultAnonymousExpectations(url + inviteHash, RoleInviteResource.class, roleInviteResource);
        RestResult<RoleInviteResource> result = service.getInvite(inviteHash);
        assertTrue(result.isSuccess());
        assertEquals(roleInviteResource, result.getSuccessObject());
    }

    @Test
    public void checkExistingUser() throws Exception {
        String url = inviteRestBaseUrl + "/checkExistingUser/";
        String inviteHash = "hash";
        setupGetWithRestResultAnonymousExpectations(url + inviteHash, Boolean.class, true);
        RestResult<Boolean> returnedResponse = service.checkExistingUser(inviteHash);
        assertTrue(returnedResponse.isSuccess());
        assertTrue(returnedResponse.getSuccessObject());
    }
}