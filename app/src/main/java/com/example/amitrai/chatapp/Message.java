package com.example.amitrai.chatapp;

/**
 * Created by amitrai on 28/12/16.
 */

public class Message {
    private String UserName;
    private String Message;

    public Message(String UserName, String Message){
        this.Message = Message;
        this.UserName = UserName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
