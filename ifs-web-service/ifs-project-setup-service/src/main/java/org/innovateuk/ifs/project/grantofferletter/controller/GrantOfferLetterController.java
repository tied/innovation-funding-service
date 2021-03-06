package org.innovateuk.ifs.project.grantofferletter.controller;

import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.service.FailingOrSucceedingResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.grantofferletter.GrantOfferLetterService;
import org.innovateuk.ifs.project.grantofferletter.form.GrantOfferLetterForm;
import org.innovateuk.ifs.project.grantofferletter.populator.GrantOfferLetterModelPopulator;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.service.ProjectRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.controller.FileUploadControllerUtils.getMultipartFileBytes;
import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;

/**
 * This Controller handles Grant Offer Letter activity for the External Competition team members
 */
@Controller
@RequestMapping("/project/{projectId}/offer")
public class GrantOfferLetterController {

    private static final String FORM_ATTR = "form";
    public static final String BASE_DIR = "project";
    public static final String TEMPLATE_NAME = "grant-offer-letter";

    @Autowired
    private GrantOfferLetterService grantOfferLetterService;

    @Autowired
    private GrantOfferLetterModelPopulator grantOfferLetterViewModelPopulator;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private ProjectRestService projectRestService;

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @GetMapping
    public String viewGrantOfferLetterPage(@P("projectId")@PathVariable("projectId") Long projectId, Model model,
                                           @RequestParam(value = "event", required = false) String event,
                                           UserResource loggedInUser) {
        if ("signing_complete".equals(event)) {
            grantOfferLetterService.importSignedOfferLetter(projectId).getSuccess();
        }
        GrantOfferLetterForm form = new GrantOfferLetterForm();

        return createGrantOfferLetterPage(projectId, model, loggedInUser, form);
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @GetMapping("/docusign")
    public String viewGrantOfferLetterPage(@PathVariable("projectId") long projectId) {
        return "redirect:" + grantOfferLetterService.getDocusignUrl(projectId).getSuccess().getContent();
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @GetMapping("/confirmation")
    public String confirmation(@PathVariable long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        ProjectResource project = projectRestService.getProjectById(projectId).getSuccess();
        CompetitionResource competition = competitionRestService.getCompetitionById(project.getCompetition()).getSuccess();
        model.addAttribute("title", competition.isProcurement() ? "Contract" : "Grant offer letter");
        return BASE_DIR + "/grant-offer-letter-confirmation";
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @PostMapping(params = "confirmSubmit")
    public String submit(@P("projectId")@PathVariable("projectId") Long projectId,
                         @ModelAttribute(FORM_ATTR) GrantOfferLetterForm form,
                         @SuppressWarnings("unused") BindingResult bindingResult,
                         ValidationHandler validationHandler,
                         Model model,
                         UserResource loggedInUser) {

        return validationHandler.performActionOrBindErrorsToField("",
                () -> createGrantOfferLetterPage(projectId, model, loggedInUser, form),
                () -> "redirect:/project/" + projectId,
                () -> grantOfferLetterService.submitGrantOfferLetter(projectId));
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @PostMapping(params = "uploadSignedGrantOfferLetterClicked")
    public String uploadSignedGrantOfferLetterFile(
            @P("projectId")@PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) GrantOfferLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            UserResource loggedInUser) {
        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "signedGrantOfferLetter", form, () -> {

            MultipartFile signedGrantOfferLetter = form.getSignedGrantOfferLetter();

            return grantOfferLetterService.addSignedGrantOfferLetter(projectId, signedGrantOfferLetter.getContentType(), signedGrantOfferLetter.getSize(),
                    signedGrantOfferLetter.getOriginalFilename(), getMultipartFileBytes(signedGrantOfferLetter));
        });
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @PostMapping(params = "uploadSignedAdditionalContractFileClicked")
    public String uploadSignedAdditionalContractFile(
            @PathVariable final long projectId,
            @ModelAttribute(FORM_ATTR) GrantOfferLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            UserResource loggedInUser) {
        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "signedAdditionalContract", form, () -> {

            MultipartFile signedAdditionalContract = form.getSignedAdditionalContract();

            return grantOfferLetterService.addSignedAdditionalContract(projectId, signedAdditionalContract.getContentType(), signedAdditionalContract.getSize(),
                    signedAdditionalContract.getOriginalFilename(), getMultipartFileBytes(signedAdditionalContract));
        });
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_SIGNED_GRANT_OFFER_LETTER')")
    @PostMapping(params = "removeSignedAdditionalContractFileClicked")
    public String removeSignedAdditionalContractFile(
            @PathVariable final long projectId,
            @ModelAttribute(FORM_ATTR) GrantOfferLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            UserResource loggedInUser) {
        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "signedAdditionalContract", form, () ->
                grantOfferLetterService.removeSignedAdditionalContract(projectId)
        );
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_SIGNED_GRANT_OFFER_LETTER')")
    @PostMapping(params = "removeSignedGrantOfferLetterClicked")
    public String removeSignedGrantOfferLetterFile(
            @P("projectId")@PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) GrantOfferLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            UserResource loggedInUser) {
        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "signedGrantOfferLetter", form, () ->
            grantOfferLetterService.removeSignedGrantOfferLetter(projectId)
        );
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @GetMapping("/download")
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadGeneratedGrantOfferLetterFile(
            @P("projectId")@PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = grantOfferLetterService.getGrantOfferFile(projectId);
        final Optional<FileEntryResource> fileDetails = grantOfferLetterService.getGrantOfferFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_SIGNED_GRANT_OFFER_LETTER')")
    @GetMapping("/signed-download")
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadGrantOfferLetterFile(
            @P("projectId")@PathVariable("projectId") final Long projectId) {
        final Optional<ByteArrayResource> content = grantOfferLetterService.getSignedGrantOfferLetterFile(projectId);
        final Optional<FileEntryResource> fileDetails = grantOfferLetterService.getSignedGrantOfferLetterFileDetails(projectId);
        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @GetMapping("/additional-contract")
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadAdditionalContractFile(
            @P("projectId")@PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = grantOfferLetterService.getAdditionalContractFile(projectId);
        final Optional<FileEntryResource> fileDetails = grantOfferLetterService.getAdditionalContractFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_GRANT_OFFER_LETTER_SECTION')")
    @GetMapping("/signed-additional-contract")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadSignedAdditionalContractFile(
            @PathVariable final long projectId) {

        final Optional<ByteArrayResource> content = grantOfferLetterService.getSignedAdditionalContractFile(projectId);
        final Optional<FileEntryResource> fileDetails = grantOfferLetterService.getSignedAdditionalContractFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    private String createGrantOfferLetterPage(Long projectId, Model model, UserResource loggedInUser, GrantOfferLetterForm form) {
        model.addAttribute("model", grantOfferLetterViewModelPopulator.populateGrantOfferLetterViewModel(projectId, loggedInUser));
        model.addAttribute("form", form);
        return BASE_DIR + "/" + TEMPLATE_NAME;
    }

    private String performActionOrBindErrorsToField(Long projectId, ValidationHandler validationHandler, Model model, UserResource loggedInUser, String fieldName, GrantOfferLetterForm form, Supplier<FailingOrSucceedingResult<?, ?>> actionFn) {

        Supplier<String> successView = () -> redirectToGrantOfferLetterPage(projectId);
        Supplier<String> failureView = () -> createGrantOfferLetterPage(projectId, model, loggedInUser, form);

        return validationHandler.performActionOrBindErrorsToField(fieldName, failureView, successView, actionFn);
    }

    private String redirectToGrantOfferLetterPage(Long projectId) {
        return "redirect:/project/" + projectId + "/offer";
    }

    private ResponseEntity<ByteArrayResource> returnFileIfFoundOrThrowNotFoundException(Long projectId, Optional<ByteArrayResource> content, Optional<FileEntryResource> fileDetails) {
        if (content.isPresent() && fileDetails.isPresent()) {
            return getFileResponseEntity(content.get(), fileDetails.get());
        } else {
            throw new ObjectNotFoundException("Could not find Collaboration Agreement for project " + projectId, singletonList(projectId));
        }
    }
}