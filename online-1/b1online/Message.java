package b1online;

import java.io.Serializable;

public class Message implements Serializable {
    private String sender;
    private String receiver;
    private String message;

    public Message(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
        return sender+": "+message;
    }
}
