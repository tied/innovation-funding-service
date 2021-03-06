package org.innovateuk.ifs.project.grantofferletter.viewmodel;

import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterStateResource;
import org.innovateuk.ifs.project.projectdetails.viewmodel.BasicProjectDetailsViewModel;

/**
 * A view model that backs the Project grant offer letter page
 **/
public class GrantOfferLetterModel implements BasicProjectDetailsViewModel {

    private String title;
    private final Long projectId;
    private final String projectName;
    private final boolean leadPartner;
    private final boolean projectManager;
    private final boolean financeContact;
    private FileDetailsViewModel grantOfferLetterFile;
    private FileDetailsViewModel signedGrantOfferLetterFile;
    private FileDetailsViewModel additionalContractFile;
    private FileDetailsViewModel signedAdditionalContractFile;
    private GrantOfferLetterStateResource golState;
    private boolean useDocusign;
    private boolean procurement;
    private boolean ktp;

    public GrantOfferLetterModel(String title, Long projectId, String projectName, boolean leadPartner, FileDetailsViewModel grantOfferLetterFile,
                                 FileDetailsViewModel signedGrantOfferLetterFile, FileDetailsViewModel additionalContractFile,
                                 FileDetailsViewModel signedAdditionalContractFile, boolean projectManager, boolean financeContact,
                                 GrantOfferLetterStateResource golState, boolean useDocusign, boolean procurement, boolean ktp) {
        this.title = title;
        this.projectId = projectId;
        this.projectName = projectName;
        this.leadPartner = leadPartner;
        this.grantOfferLetterFile = grantOfferLetterFile;
        this.signedGrantOfferLetterFile = signedGrantOfferLetterFile;
        this.additionalContractFile = additionalContractFile;
        this.signedAdditionalContractFile = signedAdditionalContractFile;
        this.projectManager = projectManager;
        this.financeContact = financeContact;
        this.golState = golState;
        this.useDocusign = useDocusign;
        this.procurement = procurement;
        this.ktp = ktp;
    }

    @Override
    public Long getProjectId() {
        return projectId;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public boolean isLeadPartner() {
        return leadPartner;
    }

    public GrantOfferLetterStateResource getGolState() {
        return golState;
    }

    public boolean isSubmitted() {
        return golState.isSignedGrantOfferLetterReceivedByInternalTeam();
    }

    public FileDetailsViewModel getGrantOfferLetterFile() {
        return grantOfferLetterFile;
    }

    public boolean isGrantOfferLetterApproved() {
        return golState.isSignedGrantOfferLetterApproved();
    }

    public FileDetailsViewModel getAdditionalContractFile() {
        return additionalContractFile;
    }

    public FileDetailsViewModel getSignedAdditionalContractFile() {
        return signedAdditionalContractFile;
    }

    public boolean isOfferSigned() {
        return signedGrantOfferLetterFile != null;
    }

    public boolean isAdditionalContractSigned() {
        return signedAdditionalContractFile != null;
    }

    public FileDetailsViewModel getSignedGrantOfferLetterFile() {
        return signedGrantOfferLetterFile;
    }

    public boolean isUseDocusign() {
        return useDocusign;
    }

    public String getTitle() {
        return title;
    }

    public String getDocumentName() {
        return title.toLowerCase();
    }

    public String getFullDocumentName() {
        if ("Grant offer letter".equals(title)) {
            return "grant offer letter (GOL)";
        }
        return getDocumentName();
    }

    public boolean isProcurement() {
        return procurement;
    }

    public boolean isKtp() {
        return ktp;
    }

    public boolean isShowSubmitButton() {
        return projectManager
                && !isSubmitted()
                && isOfferSigned()
                && grantOfferLetterFile != null;
    }

    public boolean isProjectManager() {
        return projectManager;
    }

    public boolean isFinanceContact() {
        return financeContact;
    }

    public boolean isGrantOfferLetterSent() {
        return golState.isGeneratedGrantOfferLetterAlreadySentToProjectTeam();
    }

    public boolean isGrantOfferLetterRejected() {
        return golState.isSignedGrantOfferLetterRejected();
    }

    public boolean isShowGrantOfferLetterRejectedMessage() {

        if (!isProjectManager() && !(isLeadPartner() && isFinanceContact())) {
            return false;
        }

        return isGrantOfferLetterRejected();
    }

    public boolean isAbleToRemoveSignedGrantOffer() {

        if (getSignedGrantOfferLetterFile() == null) {
            return false;
        }

        if (isGrantOfferLetterApproved()) {
            return false;
        }

        if (isGrantOfferLetterRejected()) {
            return isProjectManager();
        }

        if (!isSubmitted()) {
            return isLeadPartner();
        }

        return false;
    }

    public boolean isAbleToRemoveSignedAdditionalContractFile() {
        if (getSignedAdditionalContractFile() == null) {
            return false;
        }

        if (isGrantOfferLetterApproved()) {
            return false;
        }

        if (isGrantOfferLetterRejected()) {
            return isProjectManager() || isFinanceContact();
        }

        if (!isSubmitted()) {
            return isLeadPartner();
        }

        return false;
    }

    public boolean isShowGrantOfferLetterReceivedByInnovateMessage() {

        if (isGrantOfferLetterApproved()) {
            return false;
        }

        if (!isLeadPartner()) {
            return isSubmitted() && !isGrantOfferLetterRejected();
        }

        return isSubmitted();
    }

    public boolean isShowAwaitingSignatureFromLeadPartnerMessage() {

        if (isLeadPartner()) {
            return false;
        }

        if (!isGrantOfferLetterSent()) {
            return false;
        }

        return !isSubmitted();
    }

    public boolean isShowGrantOfferLetterApprovedByInnovateMessage() {
        return isGrantOfferLetterApproved();
    }

}
