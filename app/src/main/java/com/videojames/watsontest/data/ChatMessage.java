package com.videojames.watsontest.data;

import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

import java.util.Date;

public class ChatMessage {
    Date date = new Date();
    private String text;
    private String url;
    private String title;
    private String description;
    private String currentUser;
    private String timestamp;
    private String type;

    public ChatMessage (String input, String id) {
        this.type = "Text";
        this.text = input;
        this.currentUser = id;
    }

    public ChatMessage (RuntimeResponseGeneric runtimeResponseGeneric) {
        this.text = "";
        this.title = runtimeResponseGeneric.title();
        this.description = runtimeResponseGeneric.description();
        this.url = runtimeResponseGeneric.source();
        this.currentUser = "2";
        this.type = "Image";
        this.timestamp = date.toString();
    }

    public void setText(String input) {
        this.text = input;
    }

    public String getText() {
        return text;
    }

    public void setUrl(String input) {
        this.url = input;
    }

    public String getUrl() {
        return url;
    }

    public void setUser(String input) {
        currentUser = input;
    }

    public String getUser() {
        return currentUser;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setType (String input) {
        type = input;
    }

    public String getType() {
        return type;
    }
}