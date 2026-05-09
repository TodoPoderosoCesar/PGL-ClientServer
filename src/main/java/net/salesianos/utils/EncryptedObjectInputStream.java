package net.salesianos.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class EncryptedObjectInputStream {

    private final DataInputStream dataIn;
    private final SecureManager secureManager;

    public EncryptedObjectInputStream(InputStream in, SecureManager secureManager) {
        this.dataIn = new DataInputStream(in);
        this.secureManager = secureManager;
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        // 1. Leer longitud (bloqueante)
        int length = dataIn.readInt();
        if (length <= 0) {
            throw new IOException("[EncryptedObjectInputStream] Longitud inválida: " + length);
        }

        // 2. Leer exactamente 'length' bytes
        byte[] encrypted = new byte[length];
        dataIn.readFully(encrypted);

        // 3. Descifrar
        byte[] decrypted = secureManager.decrypt(encrypted);
        if (decrypted == null) {
            throw new IOException("[EncryptedObjectInputStream] El descifrado devolvió null.");
        }

        // 4. Deserializar
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decrypted))) {
            return ois.readObject();
        }
    }

    public void close() throws IOException {
        dataIn.close();
    }
}
