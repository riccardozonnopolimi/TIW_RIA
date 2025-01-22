package it.polimi.tiw.beans;

public class UserData {
    private boolean success;
    private String message;
    private int userId;
    private String username;
    private AlbumData[] myAlbums;
    private AlbumData[] otherAlbums;
    private ImmagineData[] myImages;


    public UserData(User user) {
        if (user != null) {
            this.success = true;
            this.message = "Login successful";
            this.userId = user.getId_user();
            this.username = user.getUsername();
        } else {
            this.success = false;
            this.message = "User is null!";
        }
    }

 
    public UserData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }


    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public AlbumData[] getMyAlbums() {
        return myAlbums;
    }
    public void setMyAlbums(AlbumData[] myAlbums) {
        this.myAlbums = myAlbums;
    }

    public AlbumData[] getOtherAlbums() {
        return otherAlbums;
    }
    public void setOtherAlbums(AlbumData[] otherAlbums) {
        this.otherAlbums = otherAlbums;
    }

    public ImmagineData[] getMyImages() {
        return myImages;
    }
    public void setMyImages(ImmagineData[] myImages) {
        this.myImages = myImages;
    }
}

