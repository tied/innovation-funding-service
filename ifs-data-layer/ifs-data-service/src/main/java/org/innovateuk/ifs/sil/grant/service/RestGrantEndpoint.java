package org.innovateuk.ifs.sil.grant.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.AbstractRestTemplateAdaptor;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.sil.grant.resource.Grant;
import org.innovateuk.ifs.util.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.commons.error.CommonFailureKeys.CONTACT_NOT_UPDATED;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;

@Component
public class RestGrantEndpoint implements GrantEndpoint {
    private static final Log LOG = LogFactory.getLog(RestGrantEndpoint.class);

    @Autowired
    @Qualifier("sil_adaptor")
    private AbstractRestTemplateAdaptor adaptor;

    @Value("${sil.rest.baseURL}")
    private String silRestServiceUrl;

    @Value("${sil.rest.grantSend:/sendproject}")
    private String path;

    @Override
    public ServiceResult<Void> send(Grant grant) {
        final Either<ResponseEntity<Void>, ResponseEntity<Void>> response =
                adaptor.restPostWithEntity(silRestServiceUrl + path, grant,
                        Void.class, Void.class, HttpStatus.OK, HttpStatus.ACCEPTED);
        if (response.isLeft()){
            LOG.debug("Sent grant NOK : " + grant);
            return serviceFailure(new Error(CONTACT_NOT_UPDATED));
        } else {
            LOG.debug("Sent grant OK : " + grant);
            return serviceSuccess();
        }
    }
}
