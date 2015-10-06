package com.worth.ifs.user.service;

import com.worth.ifs.commons.service.BaseRestServiceProvider;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * UserRestServiceImpl is a utility for CRUD operations on {@link User}.
 * This class connects to the {@link com.worth.ifs.user.controller.UserController}
 * through a REST call.
 */
@Service
public class UserRestServiceImpl extends BaseRestServiceProvider implements UserRestService {

    private String userRestURL;
    private String processRoleRestURL;

    @Value("${ifs.data.service.rest.user}")
    void setUserRestUrl(String userRestURL) {
        this.userRestURL = userRestURL;
    }

    @Value("${ifs.data.service.rest.processrole}")
    void setProcessRoleRestUrl(String processRoleRestURL) {
        this.processRoleRestURL = processRoleRestURL;
    }

    public User retrieveUserByToken(String token) {
        if(StringUtils.isEmpty(token))
            return null;

        User user = getRestTemplate().getForObject(dataRestServiceURL + userRestURL + "/token/" + token, User.class);
        return user;
    }

    public User retrieveUserByEmailAndPassword(String email, String password) {
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(password))
            return null;

        User user = getRestTemplate().getForObject(dataRestServiceURL + userRestURL + "/email/" + email + "/password/" + password, User.class);
        return user;
    }

    public User retrieveUserById(Long id) {
        if(id == null || id.equals(0L))
            return null;

        User user = getRestTemplate().getForObject(dataRestServiceURL + userRestURL + "/id/" + id, User.class);
        return user;
    }

    public List<User> findAll() {
        ResponseEntity<User[]> responseEntity = getRestTemplate().getForEntity(dataRestServiceURL + userRestURL + "/findAll/", User[].class);
        User[] users =responseEntity.getBody();
        return Arrays.asList(users);
    }

    public ProcessRole findProcessRole(Long userId, Long applicationId) {
        ProcessRole processRole = getRestTemplate().getForObject(dataRestServiceURL + processRoleRestURL + "/findByUserApplication/" + userId + "/" + applicationId, ProcessRole.class);
        return processRole;
    }

    public List<ProcessRole> findProcessRole(Long applicationId) {
        ResponseEntity<ProcessRole[]> responseEntity = getRestTemplate().getForEntity(dataRestServiceURL + processRoleRestURL + "/findByUserApplication/" + applicationId, ProcessRole[].class);
        ProcessRole[] processRole = responseEntity.getBody();
        return Arrays.asList(processRole);
    }

    public List<User> findAssignableUsers(Long applicationId){
        ResponseEntity<User[]> responseEntity = getRestTemplate().getForEntity(dataRestServiceURL + userRestURL + "/findAssignableUsers/" + applicationId, User[].class);
        User[] users =responseEntity.getBody();
        return Arrays.asList(users);
    }

    public List<ProcessRole> findAssignableProcessRoles(Long applicationId){
        ResponseEntity<ProcessRole[]> responseEntity = getRestTemplate().getForEntity(dataRestServiceURL + processRoleRestURL + "/findAssignable/" + applicationId, ProcessRole[].class);
        ProcessRole[] processRoles =responseEntity.getBody();
        return Arrays.asList(processRoles);
    }

    public List<User> findRelatedUsers(Long applicationId){
        ResponseEntity<User[]> responseEntity = getRestTemplate().getForEntity(dataRestServiceURL + userRestURL + "/findRelatedUsers/"+applicationId, User[].class);
        User[] users =responseEntity.getBody();
        return Arrays.asList(users);
    }
}
