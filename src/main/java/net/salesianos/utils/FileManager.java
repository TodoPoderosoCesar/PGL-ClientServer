package net.salesianos.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {

    private static final String KEY_FILE = "./secret";

    private FileManager() {}

    public static void writeSecretKey(byte[] encodedKey) {
        try (FileOutputStream fos = new FileOutputStream(KEY_FILE)) {
            fos.write(encodedKey);
            fos.flush();
            System.out.println("[FileManager] Clave AES guardada en: " + KEY_FILE);
        } catch (IOException e) {
            System.err.println("[FileManager] Error al escribir la clave: " + e.getMessage());
        }
    }

    public static byte[] readSecretKey() {
        try (FileInputStream fis = new FileInputStream(KEY_FILE)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            // El fichero aún no existe — se generará una clave nueva
            return null;
        }
    }
}
