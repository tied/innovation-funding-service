package org.innovateuk.ifs.async.controller;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 * A method interceptor that is applied to web layer Controller handler methods via {@link ThreadsafeModelAdvisor} and
 * replaces the method argument of Model (if any) with a ThreadsafeModel that wraps the original Model in a threadsafe
 * wrapper.  The Controller method is then called with the wrapped Model rather than the original.
 */
@Component
public class ThreadsafeModelMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        for (int i = 0; i < invocation.getArguments().length; i++) {

            if (invocation.getArguments()[i] instanceof Model) {
                invocation.getArguments()[i] = new ThreadsafeModel((Model) invocation.getArguments()[i]);
            }
        }

        return invocation.proceed();
    }
}
