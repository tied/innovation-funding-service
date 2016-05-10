package com.worth.ifs.finance.security;

import com.worth.ifs.finance.resource.CostFieldResource;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

import static com.worth.ifs.security.SecurityRuleUtil.isAnonymous;

@Component
@PermissionRules
public class CostFieldPermissionsRules {

    @PermissionRule(value = "READ", description = "All logged in users can see the reference cost field reference data")
    public boolean loggedInUsersCanReadCostFieldReferenceData(final CostFieldResource costFieldToRead, final UserResource user){
        return !isAnonymous(user);
    }

}
