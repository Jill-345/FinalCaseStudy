package com.example.finalcasestudy;

// Model class representing the data structure for a found item
public class ItemFoundData {

    // Unique Firestore document ID for identifying the item
    private String documentId;

    // Name of the found item
    private String name;

    // Date when the item was found
    private String date;

    // URL of the item's uploaded image in Firebase Storage
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

    // Getter method to retrieve the Firestore document ID
    public String getDocumentId() {
        return documentId;
    }

    // Getter method to retrieve the item name
    public String getName() {
        return name;
    }

    // Getter method to retrieve the found date
    public String getDate() {
        return date;
    }

    // Getter method to retrieve the image URL
    public String getImageUrl() {
        return imageUrl;
    }
}
