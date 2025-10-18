package com.example.finalcasestudy;

public class MatchingResultFoundData {
    private String documentId;
    private String itemName;
    private String date;
    private String imageUrl;

    public MatchingResultFoundData() {}

    public MatchingResultFoundData(String documentId, String itemName, String date, String imageUrl) {
        this.documentId = documentId;
        this.itemName = itemName;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
