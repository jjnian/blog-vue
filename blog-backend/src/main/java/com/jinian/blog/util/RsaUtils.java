package com.jinian.blog.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密工具类
 * 应用启动时自动生成RSA密钥对，提供公钥获取和私钥解密功能
 */
@Slf4j
@Component
public class RsaUtils {

    private static final String RSA_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private KeyPair keyPair;

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            keyPair = keyPairGenerator.generateKeyPair();
            log.info("RSA密钥对生成成功，密钥长度: {} bits", KEY_SIZE);
        } catch (NoSuchAlgorithmException e) {
            log.error("RSA密钥对生成失败", e);
            throw new RuntimeException("RSA密钥对生成失败", e);
        }
    }

    /**
     * 获取Base64编码的公钥字符串
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    /**
     * 使用私钥解密Base64编码的密文
     */
    public String decrypt(String cipherTextBase64) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherTextBase64));
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * 从Base64字符串恢复公钥对象
     */
    public PublicKey restorePublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(keySpec);
    }

    /**
     * 从Base64字符串恢复私钥对象
     */
    public PrivateKey restorePrivateKey(String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(keySpec);
    }
}
