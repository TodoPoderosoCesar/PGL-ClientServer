package net.salesianos.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecureManager {

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public SecureManager() {
        SecretKey key = loadOrGenerateKey();
        initCiphers(key);
    }

    public SecureManager(byte[] rawKey) {
        SecretKey key = new SecretKeySpec(rawKey, "AES");
        initCiphers(key);
    }

    public byte[] encrypt(byte[] data) {
        try {
            return encryptCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("[SecureManager] Error al encriptar: " + e.getMessage());
            return null;
        }
    }

    public byte[] decrypt(byte[] encryptedData) {
        try {
            return decryptCipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("[SecureManager] Error al desencriptar: " + e.getMessage());
            return null;
        }
    }

    public void printEncrypted(byte[] encryptedData) {
        System.out.println("[SecureManager] Datos cifrados (Base64): "
                + Base64.getEncoder().encodeToString(encryptedData));
    }

    public byte[] getRawKey() {
        return FileManager.readSecretKey();
    }

    private SecretKey loadOrGenerateKey() {
        byte[] savedKeyBytes = FileManager.readSecretKey();
        if (savedKeyBytes != null) {
            return new SecretKeySpec(savedKeyBytes, "AES");
        }

        // Primera vez: generar y persistir
        SecretKey newKey = generateKey();
        if (newKey != null) {
            FileManager.writeSecretKey(newKey.getEncoded());
        }
        return newKey;
    }

    private SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[SecureManager] Algoritmo AES no disponible: " + e.getMessage());
            return null;
        }
    }

    private void initCiphers(SecretKey key) {
        try {
            encryptCipher = Cipher.getInstance("AES");
            decryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.println("[SecureManager] Error al obtener Cipher AES: " + e.getMessage());
        } catch (InvalidKeyException e) {
            System.err.println("[SecureManager] Clave AES inválida: " + e.getMessage());
        }
    }
}
