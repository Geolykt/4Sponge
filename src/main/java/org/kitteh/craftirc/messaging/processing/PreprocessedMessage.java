package org.kitteh.craftirc.messaging.processing;

public class PreprocessedMessage {

    private final String original;
    private final String user;
    private String newMessage;

    public PreprocessedMessage(String message, String sender) {
        original = message;
        newMessage = original;
        user = sender;
    }

    public String getOriginal() {
        return original;
    }

    public String getMessage() {
        return newMessage;
    }

    public void setMessage(String message) {
        newMessage = message;
    }

    public String getSender() {
        return user;
    }
}
