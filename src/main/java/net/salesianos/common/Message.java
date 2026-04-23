package net.salesianos.common;
import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private final MessageType tipo;
    private final String remitente;
    private final Object payload;

    // ----------------------------------------------------------------
    // Constructores
    // ----------------------------------------------------------------

    public Message(MessageType tipo, String remitente, Object payload) {
        this.tipo      = tipo;
        this.remitente = remitente;
        this.payload   = payload;
    }

    public Message(MessageType tipo, String remitente) {
        this(tipo, remitente, null);
    }

    public MessageType getTipo() {
        return tipo;
    }

    public String getRemitente() {
        return remitente;
    }

    public Object getPayload() {
        return payload;
    }

    public String getPayloadAsString() {
        return (String) payload;
    }

    @Override
    public String toString() {
        return "Message{tipo=" + tipo
                + ", remitente='" + remitente + '\''
                + ", payload=" + payload + '}';
    }
}
