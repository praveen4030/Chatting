package com.kraigs.chattingapp.Model;

public class ScreenItem {
    String Title,Description,photo,blogID,asset;
    int ScreenImg,background;

    public ScreenItem(String title, String description, String photo, String blogID, int screenImg, int background, String asset) {
        Title = title;
        Description = description;
        this.photo = photo;
        this.blogID = blogID;
        ScreenImg = screenImg;
        this.background = background;
        this.asset = asset;
    }

    public ScreenItem(String title, String description,String mAsset) {
        Title = title;
        Description = description;
        asset = mAsset;

    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
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
