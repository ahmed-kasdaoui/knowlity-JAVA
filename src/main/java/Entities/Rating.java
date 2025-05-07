package Entities;

import java.sql.Timestamp;
import java.sql.Timestamp;

public class Rating {
    private int id;
    private int blogId;
    private int userId;
    private int rating;
    private Timestamp createdAt;

    public Rating() {
    }

    public Rating(int blogId, int rating) {
        this.blogId = blogId;
        this.rating = rating;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlogId() {
        return blogId;
    }

    public void setBlogId(int blogId) {
        this.blogId = blogId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
