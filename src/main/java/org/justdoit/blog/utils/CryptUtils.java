package org.justdoit.blog.utils;

import lombok.RequiredArgsConstructor;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CryptUtils {
    private final GlobalVariables globalVariables;

    private static final String ALGORITHM = "AES";
    private static final String ENCODING = "UTF-8";

    //AES-256 암호화
    public String encrypt256(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                Key key = new SecretKeySpec(globalVariables.AES_KEY.getBytes(ENCODING), ALGORITHM);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, key);

                byte[] encryptedBytes = cipher.doFinal(value.getBytes(ENCODING));
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return value;
    }

    //AES-256 복호화
    public String decrypt256(String encryptedValue) {
        if (encryptedValue != null && !encryptedValue.isEmpty()) {
            try {
                Key key = new SecretKeySpec(globalVariables.AES_KEY.getBytes(ENCODING), ALGORITHM);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key);

                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
                return new String(decryptedBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return encryptedValue;
    }
}
