package com.videojames.sousbot.data;

import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

import java.util.Date;


/**
 * Creates a ChatMessage object containing either an image or text data.
 */
public class ChatMessage {
    Date date = new Date();
    private String text;
    private String url;
    private String title;
    private String description;
    private boolean currentUser;
    private String timestamp;
    private String type;

    /**
     * Creates a text type chat message,
     * @param input = The received message
     * @param id = The identity of the sender (true = user false = bot)
     * The timestamp is the recorded date and time for error handling purposes.
     */
    public ChatMessage (String input, boolean id) {
        this.type = "Text";
        this.text = input;
        this.currentUser = id;
        this.timestamp = date.toString();
    }

    /**
     * Creates an image message
     * @param runtimeResponseGeneric =  The received RuntimeResponseGeneric JSON message from Watson.
     *  Leaves the text field empty and sets the title, description and URL based on the received
     *  JSON information. The user is set to false (the bot) automatically and the message type is set
     *  to be an image.
     *  The timestamp is the recorded date and time for error handling purposes.
     */
    public ChatMessage (RuntimeResponseGeneric runtimeResponseGeneric) {
        this.text = "";
        this.title = runtimeResponseGeneric.title();
        this.description = runtimeResponseGeneric.description();
        this.url = runtimeResponseGeneric.source();
        this.currentUser = false;
        this.type = "Image";
        this.timestamp = date.toString();
    }

    /** Sets the message text
     *
     * @param input = The received message
     */
    public void setText(String input) {
        this.text = input;
    }

    /** Returns the message text
     */
    public String getText() {
        return text;
    }

    /** Sets the URL of an image message
     *
     * @param input = The URL
     */
    public void setUrl(String input) {
        this.url = input;
    }

    /** Returns the URL
     */
    public String getUrl() {
        return url;
    }

    /** Sets the message sender with a boolean check for if it is the user or bot
     *
     * @param input = The identity of the sender (true = user false = bot)
     */
    public void setUser(boolean input) {
        currentUser = input;
    }

    /**
     * @return Returns the sender of the message
     */
    public boolean getUser() {
        return currentUser;
    }

    /** Returns the send time of the message
     */
    public String getTimestamp() {
        return timestamp;
    }

    /** Sets the message type
     * @param input = "text" or "image"
     */
    public void setType (String input) {
        type = input;
    }

    /** Returns the message type
     */
    public String getType() {
        return type;
    }
}