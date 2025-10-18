package com.example.finalcasestudy;

public class ItemLostData {
    private String documentId;
    private String name;
    private String date;
    private String imageUrl;

    public ItemLostData() {} // Required for Firestore

    public ItemLostData(String documentId, String name, String date, String imageUrl) {
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
