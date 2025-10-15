package com.example.finalcasestudy;

public class ItemFoundData {
    private String name;
    private String date;
    private String imageUrl;

    // Required empty constructor for Firestore
    public ItemFoundData() {
    }

    // Constructor for manual creation (no image)
    public ItemFoundData(String name, String date) {
        this.name = name;
        this.date = date;
    }

    // Constructor with image
    public ItemFoundData(String name, String date, String imageUrl) {
        this.name = name;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Optional setters (if you want to modify later)
    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
