package com.teddyfresnes.msgpreparer;
import java.util.UUID;

public class Message {
    private String text;
    private boolean isAutoReply;
    private boolean isSpam;
    private String id;

    public Message(String text, boolean isAutoReply, boolean isSpam) {
        this.text = text;
        this.isAutoReply = isAutoReply;
        this.isSpam = isSpam;
        this.id = generateUniqueId();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isAutoReply() {
        return isAutoReply;
    }

    public void setAutoReply(boolean autoReply) {
        isAutoReply = autoReply;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setSpam(boolean spam) {
        isSpam = spam;
    }

    public String getPreview() {
        if (text.length() <= 20) {
            return text;
        } else {
            return text.substring(0, 20) + "...";
        }
    }

    public String getId() {
        return id;
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}