package org.innovateuk.ifs.docusign.transactional;

import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.docusign.domain.DocusignDocument;
import org.innovateuk.ifs.docusign.resource.DocusignRequest;
import org.innovateuk.ifs.schedule.transactional.ScheduleResponse;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DocusignService {
    @NotSecured(value = "This Service is to be used within other secured services", mustBeSecuredByOtherServices = true)
    ServiceResult<DocusignDocument> send(DocusignRequest request);

    @NotSecured(value = "This Service is to be used within other secured services", mustBeSecuredByOtherServices = true)
    ServiceResult<DocusignDocument> resend(long docusignDocumentId, DocusignRequest request);

    @PreAuthorize("hasAuthority('system_maintainer')")
    @SecuredBySpring(value = "DOWNLOAD_FILE_IF_SIGNED", description = "System maintainer will import files on schedule" )
    ServiceResult<ScheduleResponse> downloadFileIfSigned();

    @NotSecured(value = "This Service is to be used within other secured services", mustBeSecuredByOtherServices = true)
    String getDocusignUrl(String envelopeId, long userId, String name, String email, String redirect);

    @NotSecured(value = "This Service is to be used within other secured services", mustBeSecuredByOtherServices = true)
    ServiceResult<Void> importDocument(String envelopeId);
}
