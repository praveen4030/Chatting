package com.kraigs.chattingapp.Model;

public class Notification {
    String text,from,type;

    public Notification() {
    }

    public Notification(String text, String from, String type) {
        this.text = text;
        this.from = from;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
