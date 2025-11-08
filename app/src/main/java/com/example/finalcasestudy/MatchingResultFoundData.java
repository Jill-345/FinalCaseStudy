package com.example.finalcasestudy;

public class MatchingResultFoundData {

    // Declare string that store the details
    private String documentId;
    private String itemName;
    private String date;
    private String imageUrl;

    public MatchingResultFoundData() {}

    // Adapter constructor to initialize all the string
    public MatchingResultFoundData(String documentId, String itemName, String date, String imageUrl) {
        this.documentId = documentId;
        this.itemName = itemName;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // Getter and setter for the details
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
