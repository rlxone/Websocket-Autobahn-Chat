package com.example.websocket.model;

public class ChatMessage {
    private String messageText;
    private UserType userType;
    private String messageDateTime;
    
    public String getMessageTime() {
        return messageDateTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageDateTime = messageTime;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getMessageText() {
        return messageText;
    }

    public UserType getUserType() {
        return userType;
    }
}
