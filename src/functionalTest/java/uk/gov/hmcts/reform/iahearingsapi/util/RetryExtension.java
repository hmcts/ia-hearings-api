package uk.gov.hmcts.reform.iahearingsapi.util;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

@Slf4j
public class RetryExtension implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        int maxRetries = 0;

        Throwable testException = null;
        int retries = extensionContext.getElement()
            .map(el -> el.getAnnotation(Retry.class))
            .map(Retry::value)
            .orElse(maxRetries);
        if (retries < 1) {
            invocation.proceed();
            return;
        }
        final Object[] args = invocationContext.getArguments().toArray(new Object[0]);
        final Object targetInstance = invocationContext.getTarget().orElse(null);
        final Method method = extensionContext.getRequiredTestMethod();
        for (int i = 0; i <= retries; i++) {
            try {
                method.invoke(targetInstance, args);
                invocation.skip();
                return; // Test passed
            } catch (Throwable t) {
                testException = t instanceof java.lang.reflect.InvocationTargetException && t.getCause() != null
                    ? t.getCause() : t;
                if (i < retries) {
                    log.info("Retry " + (i + 1) + " for " + extensionContext.getDisplayName());
                }
            }
        }
        throw testException; // Rethrow after max retries
    }
}
