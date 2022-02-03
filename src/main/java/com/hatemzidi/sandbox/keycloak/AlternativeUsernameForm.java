package com.hatemzidi.sandbox.keycloak;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public final class AlternativeUsernameForm extends AlternativeUsernamePasswordForm {

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            log.debug("alternative-auth-username-form : Form was canceled.");
            context.cancelLogin();
            return;
        }

        if (!validateForm(context, formData)) {
            log.debug("alternative-auth-username-form : Oops! User is not valid : wrong credentials or unknown.");
            log.debug("alternative-auth-username-form : Continuing the execution flow anyway and hoping to find a conditional flow after.");
            context.getEvent().error(Errors.USER_NOT_FOUND);
            context.attempted();
            return;

        }

        log.debug("alternative-auth-username-form : User is valid.");
        context.success();
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateUser(context, formData);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (!formData.isEmpty()) forms.setFormData(formData);

        return forms.createLoginUsername();
    }

    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsername();
    }

    @Override
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        if (context.getRealm().isLoginWithEmailAllowed())
            return Messages.INVALID_USERNAME_OR_EMAIL;
        return Messages.INVALID_USERNAME;
    }
}