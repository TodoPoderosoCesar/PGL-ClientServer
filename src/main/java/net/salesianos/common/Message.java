package net.salesianos.common;
import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private final MessageType type;
    private final String sender;
    private final Object payload;

    public Message(MessageType type, String sender, Object payload) {
        this.type      = type;
        this.sender = sender;
        this.payload   = payload;
    }

    public Message(MessageType type, String sender) {
        this(type, sender, null);
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
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
                + ", remitente='" + sender + '\''
                + ", payload=" + payload + '}';
    }
}
