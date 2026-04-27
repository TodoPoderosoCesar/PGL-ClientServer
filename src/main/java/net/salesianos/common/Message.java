package net.salesianos.common;
import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private final MessageType type;
    private final String remitente;
    private final Object payload;

    public Message(MessageType type, String remitente, Object payload) {
        this.type      = type;
        this.remitente = remitente;
        this.payload   = payload;
    }

    public Message(MessageType type, String remitente) {
        this(type, remitente, null);
    }

    public MessageType getTipo() {
        return type;
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
        return "Message{type=" + type
                + ", remitente='" + remitente + '\''
                + ", payload=" + payload + '}';
    }
}
