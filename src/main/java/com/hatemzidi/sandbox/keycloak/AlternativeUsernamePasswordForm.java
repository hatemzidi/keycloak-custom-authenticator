package com.hatemzidi.sandbox.keycloak;


import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.events.Errors;
import org.keycloak.services.ServicesLogger;

import javax.ws.rs.core.MultivaluedMap;

public class AlternativeUsernamePasswordForm extends UsernamePasswordForm implements Authenticator {
    protected static ServicesLogger log = ServicesLogger.LOGGER;

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            log.debug("Form was canceled.");
            context.cancelLogin();
            return;
        }

        if (!validateForm(context, formData)) {
            log.debug("Oops! User is not valid : wrong credentials or unknown");

            boolean isRequired = context.getExecution().isRequired();
            if (!isRequired) {
                log.debug("Execution is not required. Continuing the execution flow to the next 'alternative' task.");
                context.attempted();
                return;
            }

            log.debug("Resetting the flow, all stops here.");
            context.getEvent().error(Errors.USER_NOT_FOUND);
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;

        }

        log.debug("User is valid and authenticated.");
        context.success();
    }
}