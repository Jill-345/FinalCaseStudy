package com.example.finalcasestudy;

public class ItemFoundData {
    private String documentId;
    private String name;
    private String date;
    private String imageUrl;

    public ItemFoundData() {} // Required for Firestore

    public ItemFoundData(String documentId, String name, String date, String imageUrl) {
        this.documentId = documentId;
        this.name = name;
        this.date = date;
        this.imageUrl = imageUrl;
    }

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
