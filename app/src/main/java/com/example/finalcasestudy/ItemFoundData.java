package com.example.finalcasestudy;

public class ItemFoundData {

    // Declare string that store the details
    private String documentId;
    private String name;
    private String date;
    private String imageUrl;

    // Default constructor required for Firestore data mapping
    public ItemFoundData() {}

    // Parameterized constructor for object creation
    public ItemFoundData(String documentId, String name, String date, String imageUrl) {
        this.documentId = documentId;
        this.name = name;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // Getter method to retrieve the necessary
    public String getDocumentId() {
        return documentId;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return date;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}
