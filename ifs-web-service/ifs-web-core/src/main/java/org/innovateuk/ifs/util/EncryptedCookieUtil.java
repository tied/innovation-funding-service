package org.innovateuk.ifs.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class EncryptedCookieUtil extends AbstractCookieUtil {
    private TextEncryptor encryptor;

    @Value("${ifs.web.security.csrf.encryption.password}")
    private String encryptionPassword;

    @Value("${ifs.web.security.csrf.encryption.salt}")
    private String encryptionSalt;

    @PostConstruct
    public void init() {
        encryptor = Encryptors.text(encryptionPassword, encryptionSalt);
    }

    @Override
    protected String getValueToSave(String value) {
        return encryptor.encrypt(encodeValue(value));
    }

    @Override
    protected String getValueFromCookie(String value) {
        return decodeValue(encryptor.decrypt(value));
    }
}
