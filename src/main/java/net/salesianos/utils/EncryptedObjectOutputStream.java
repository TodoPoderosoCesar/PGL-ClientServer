package net.salesianos.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class EncryptedObjectOutputStream {

    private final DataOutputStream dataOut;
    private final SecureManager secureManager;

    public EncryptedObjectOutputStream(OutputStream out, SecureManager secureManager) {
        this.dataOut = new DataOutputStream(out);
        this.secureManager = secureManager;
    }

    public synchronized void writeObject(Serializable obj) throws IOException {
        // 1. Serializar a bytes en memoria
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        byte[] serialized = baos.toByteArray();

        // 2. Cifrar
        byte[] encrypted = secureManager.encrypt(serialized);
        if (encrypted == null) {
            throw new IOException("[EncryptedObjectOutputStream] El cifrado devolvió null.");
        }

        // 3. Enviar: longitud (4 bytes) + datos cifrados
        dataOut.writeInt(encrypted.length);
        dataOut.write(encrypted);
        dataOut.flush();
    }

    public void flush() throws IOException {
        dataOut.flush();
    }

    public void close() throws IOException {
        dataOut.close();
    }
}
