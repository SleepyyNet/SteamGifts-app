package net.mabako.steamgifts.data;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = -313348528668961481L;
    private String name, avatar, url;

    private int level, created, won, comments;
    private String createdAmount, wonAmount;

    private boolean loaded = false;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public String getCreatedAmount() {
        return createdAmount;
    }

    public void setCreatedAmount(String createdAmount) {
        this.createdAmount = createdAmount;
    }

    public int getWon() {
        return won;
    }

    public void setWon(int won) {
        this.won = won;
    }

    public String getWonAmount() {
        return wonAmount;
    }

    public void setWonAmount(String wonAmount) {
        this.wonAmount = wonAmount;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
