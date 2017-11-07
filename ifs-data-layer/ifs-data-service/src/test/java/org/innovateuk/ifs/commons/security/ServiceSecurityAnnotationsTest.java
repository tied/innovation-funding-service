package org.innovateuk.ifs.commons.security;

import org.innovateuk.ifs.commons.AbstractServiceSecurityAnnotationsTest;
import org.innovateuk.ifs.commons.security.evaluator.RootCustomPermissionEvaluator;
import org.innovateuk.ifs.security.StatelessAuthenticationFilter;
import org.innovateuk.ifs.security.UidAuthenticationService;
import org.innovateuk.ifs.security.evaluator.CustomPermissionEvaluator;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import java.lang.annotation.Annotation;
import java.util.List;

import static java.util.Arrays.asList;

public class ServiceSecurityAnnotationsTest extends AbstractServiceSecurityAnnotationsTest {

    @Override
    protected List<Class<?>> excludedClasses() {
        return asList(
                UidAuthenticationService.class,
                StatelessAuthenticationFilter.class
        );
    }

    @Override
    protected RootCustomPermissionEvaluator evaluator() {
        return (CustomPermissionEvaluator) context.getBean("customPermissionEvaluator");

    }

    @Override
    protected List<Class<? extends Annotation>> securityAnnotations() {
        return asList(
                PreAuthorize.class,
                PreFilter.class,
                PostAuthorize.class,
                PostFilter.class,
                NotSecured.class
        );
    }

}
