package com.shumen.chatapp;

import java.util.Date;

/**
 * Created by Administrator on 22-Nov-17.
 */

public class ChatMessage {
    private String messageUserName;
    private String messageText;
    private String messageUserEmail;
    private long messageTime;

    public ChatMessage(String messageUserName, String messageText, String messageUserEmail) {
        this.messageUserName = messageUserName;
        this.messageText = messageText;
        this.messageUserEmail = messageUserEmail;
        this.messageTime= new Date().getTime();
    }

    public ChatMessage() {
    }

    public String getMessageUserName() {
        return messageUserName;
    }

    public void setMessageUserName(String messageUserName) {
        this.messageUserName = messageUserName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUserEmail() {
        return messageUserEmail;
    }

    public void setMessageUserEmail(String messageUserEmail) {
        this.messageUserEmail = messageUserEmail;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
