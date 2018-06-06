package org.innovateuk.ifs.registration.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.exception.InviteAlreadyAcceptedException;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.service.EthnicityRestService;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.registration.form.RegistrationForm;
import org.innovateuk.ifs.registration.form.ResendEmailVerificationForm;
import org.innovateuk.ifs.registration.service.RegistrationCookieService;
import org.innovateuk.ifs.user.resource.EthnicityResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.innovateuk.ifs.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.*;
import static org.innovateuk.ifs.login.HomeController.getRedirectUrlForUser;

@Controller
@RequestMapping("/registration")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = RegistrationController.class)
@PreAuthorize("permitAll")
public class RegistrationController {
    public static final String BASE_URL = "/registration/register";

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Autowired
    @Qualifier("mvcValidator")
    private Validator validator;
    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationCookieService registrationCookieService;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private InviteRestService inviteRestService;
    @Autowired
    private EthnicityRestService ethnicityRestService;

    @Autowired
    protected CookieFlashMessageFilter cookieFlashMessageFilter;

    private static final Log LOG = LogFactory.getLog(RegistrationController.class);

    private final static String EMAIL_FIELD_NAME = "email";

    @GetMapping("/success")
    public String registrationSuccessful(
            @RequestHeader(value = "referer", required = false) final String referer,
            final HttpServletRequest request, HttpServletResponse response) {
        registrationCookieService.deleteInviteHashCookie(response);
        if (referer == null || !referer.contains(request.getServerName() + "/registration/register")) {
            throw new ObjectNotFoundException("Attempt to access registration page directly...", emptyList());
        }
        return "registration/successful";
    }

    @GetMapping("/verified")
    public String verificationSuccessful(final HttpServletRequest request, final HttpServletResponse response) {
        if (!hasVerifiedCookieSet(request)) {
            throw new ObjectNotFoundException("Attempt to access registration page directly...", emptyList());
        } else {
            cookieFlashMessageFilter.removeFlashMessage(response);
            return "registration/verified";
        }
    }

    @GetMapping("/verify-email/{hash}")
    public String verifyEmailAddress(@PathVariable("hash") final String hash,
                                     final HttpServletResponse response) {
        userService.verifyEmail(hash);
        cookieFlashMessageFilter.setFlashMessage(response, "verificationSuccessful");
        return "redirect:/registration/verified";
    }

    @GetMapping("/register")
    public String registerForm(@ModelAttribute("registrationForm") RegistrationForm registrationForm,
                               Model model,
                               UserResource user,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (user != null) {
            return getRedirectUrlForUser(user);
        }

        if (getOrganisationId(request) == null) {
            return "redirect:/";
        }

        try {
            addRegistrationFormToModel(registrationForm, model, request, response);
        } catch (InviteAlreadyAcceptedException e) {
            LOG.info("invite already accepted", e);
            cookieFlashMessageFilter.setFlashMessage(response, "inviteAlreadyAccepted");
            return "redirect:/login";
        }

        String destination = "registration/register";

        if (!processOrganisation(request, model)) {
            destination = "redirect:/";
        }

        return destination;
    }

    private List<EthnicityResource> getEthnicityOptions() {
        return ethnicityRestService.findAllActive().getSuccess();
    }

    private boolean processOrganisation(HttpServletRequest request, Model model) {
        OrganisationResource organisation = getOrganisation(request);
        if (organisation != null) {
            addOrganisationNameToModel(model, organisation);
            return true;
        }
        return false;
    }

    private void addRegistrationFormToModel(RegistrationForm registrationForm, Model model, HttpServletRequest request, HttpServletResponse response) {
        setOrganisationIdCookie(request, response);
        setInviteeEmailAddress(registrationForm, request, model);
        model.addAttribute("registrationForm", registrationForm);
        model.addAttribute("ethnicityOptions", getEthnicityOptions());
    }

    /**
     * When the current user is an invitee, use the invited email address in the registration flow.
     */
    private boolean setInviteeEmailAddress(RegistrationForm registrationForm, HttpServletRequest request, Model model) {
        Optional<String> inviteHash = registrationCookieService.getInviteHashCookieValue(request);
        if (inviteHash.isPresent()) {
            RestResult<ApplicationInviteResource> invite = inviteRestService.getInviteByHash(inviteHash.get());
            if (invite.isSuccess() && InviteStatus.SENT.equals(invite.getSuccess().getStatus())) {
                ApplicationInviteResource inviteResource = invite.getSuccess();
                registrationForm.setEmail(inviteResource.getEmail());
                model.addAttribute("invitee", true);
                return true;
            } else {
                LOG.debug("Invite already accepted.");
                throw new InviteAlreadyAcceptedException();
            }
        }
        return false;
    }

    private OrganisationResource getOrganisation(HttpServletRequest request) {
        return organisationService.getOrganisationByIdForAnonymousUserFlow(getOrganisationId(request));
    }

    @PostMapping("/register")
    public String registerFormSubmit(@Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
                                     BindingResult bindingResult,
                                     HttpServletResponse response,
                                     UserResource user,
                                     HttpServletRequest request,
                                     Model model) {

        try {
            if (setInviteeEmailAddress(registrationForm, request, model)) {
                bindingResult = new BeanPropertyBindingResult(registrationForm, "registrationForm");
                validator.validate(registrationForm, bindingResult);
            }
        } catch (InviteAlreadyAcceptedException e) {
            LOG.info("invite already accepted", e);
            cookieFlashMessageFilter.setFlashMessage(response, "inviteAlreadyAccepted");

            return "redirect:/login";
        }

        checkForExistingEmail(registrationForm.getEmail(), bindingResult);

        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "registrationForm", bindingResult);

        ValidationHandler validationHandler = ValidationHandler.newBindingResultHandler(bindingResult);

        // TODO : INFUND-3691
        return validationHandler.failNowOrSucceedWith(
                () -> registerForm(registrationForm, model, user, request, response),
                () -> createUser(registrationForm, getOrganisationId(request), getCompetitionId(request)).handleSuccessOrFailure(
                        failure -> {
                            validationHandler.addAnyErrors(failure,
                                    fieldErrorsToFieldErrors(
                                            e -> newFieldError(e, e.getFieldName(), e.getFieldRejectedValue(), "registration." + e.getErrorKey())
                                    ),
                                    asGlobalErrors()
                            );

                            return registerForm(registrationForm, model, user, request, response);
                        },
                        userResource -> {
                            removeCompetitionIdCookie(response);
                            acceptInvite(response, request, userResource); // might want to move this, to after email verifications.
                            registrationCookieService.deleteOrganisationIdCookie(response);

                            return "redirect:/registration/success";
                        }
                )
        );
    }

    @GetMapping("/resend-email-verification")
    public String resendEmailVerification(final ResendEmailVerificationForm resendEmailVerificationForm, final Model model) {
        model.addAttribute("resendEmailVerificationForm", resendEmailVerificationForm);
        return "registration/resend-email-verification";
    }

    @PostMapping("/resend-email-verification")
    public String resendEmailVerification(@Valid final ResendEmailVerificationForm resendEmailVerificationForm, final BindingResult bindingResult, final Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("resendEmailVerificationForm", resendEmailVerificationForm);
            return "registration/resend-email-verification";
        }

        userService.resendEmailVerificationNotification(resendEmailVerificationForm.getEmail());
        return "registration/resend-email-verification-send";
    }

    private void removeCompetitionIdCookie(HttpServletResponse response) {
        registrationCookieService.deleteCompetitionIdCookie(response);
    }

    private Long getCompetitionId(HttpServletRequest request) {
        return registrationCookieService.getCompetitionIdCookieValue(request).orElse(null);
    }

    private boolean acceptInvite(HttpServletResponse response, HttpServletRequest request, UserResource userResource) {
        Optional<String> inviteHash = registrationCookieService.getInviteHashCookieValue(request);
        if (inviteHash.isPresent()) {
            RestResult<Void> restResult = inviteRestService.acceptInvite(inviteHash.get(), userResource.getId());
            if (restResult.isSuccess()) {
                registrationCookieService.deleteInviteHashCookie(response);
            }
            return restResult.isSuccess();
        }
        return false;
    }

    private void checkForExistingEmail(String email, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors(EMAIL_FIELD_NAME) && StringUtils.hasText(email)) {
            Optional<UserResource> existingUserSearch = userService.findUserByEmail(email);

            if (existingUserSearch.isPresent()) {
                bindingResult.rejectValue(EMAIL_FIELD_NAME, "validation.standard.email.exists");
            }
        }
    }

    private ServiceResult<UserResource> createUser(RegistrationForm registrationForm, Long organisationId, Long competitionId) {
        return userService.createLeadApplicantForOrganisationWithCompetitionId(
                registrationForm.getFirstName(),
                registrationForm.getLastName(),
                registrationForm.getPassword(),
                registrationForm.getEmail(),
                registrationForm.getTitle(),
                registrationForm.getPhoneNumber(),
                registrationForm.getGender(),
                Long.parseLong(registrationForm.getEthnicity()),
                registrationForm.getDisability(),
                organisationId,
                competitionId,
                registrationForm.getAllowMarketingEmails());
    }

    private void addOrganisationNameToModel(Model model, OrganisationResource organisation) {
        model.addAttribute("organisationName", organisation.getName());
    }

    private Long getOrganisationId(HttpServletRequest request) {
        return registrationCookieService.getOrganisationIdCookieValue(request).orElse(null);
    }

    private void setOrganisationIdCookie(HttpServletRequest request, HttpServletResponse response) {
        Long organisationId = getOrganisationId(request);
        if (organisationId != null) {
            registrationCookieService.saveToOrganisationIdCookie(organisationId, response);
        }
    }

    private boolean hasVerifiedCookieSet(final HttpServletRequest request) {
        final Optional<Cookie> cookie = cookieUtil.getCookie(request, CookieFlashMessageFilter.COOKIE_NAME);
        return cookie.isPresent() && cookieUtil.getCookieValue(request, CookieFlashMessageFilter.COOKIE_NAME).equals("verificationSuccessful");
    }
}
