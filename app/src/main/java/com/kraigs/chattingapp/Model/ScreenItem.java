package com.kraigs.chattingapp.Model;

public class ScreenItem {
    String Title,Description,photo,blogID;
    int ScreenImg,background;

    public ScreenItem(String title, String description, int background, int screenImg) {
        Title = title;
        Description = description;
        this.background = background;
        ScreenImg = screenImg;
    }

    public ScreenItem(String title, String photo, String blogID) {
        Title = title;
        this.photo = photo;
        this.blogID = blogID;
    }


    public String getBlogID() {
        return blogID;
    }

    public void setBlogID(String blogID) {
        this.blogID = blogID;
    }

    public int getBackground() {
        return background;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setScreenImg(int screenImg) {
        ScreenImg = screenImg;
    }

    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

    public int getScreenImg() {
        return ScreenImg;
    }
}
