package com.example.parkingfinder.model;

public class ParkingReport {
    private String area;
    private String description;
    private String reportedBy;
    private int likesCount;
    private double latitude;
    private double longitude;

    // בנאי ריק עבור Firebase
    public ParkingReport() {
        // Firebase משתמש בזה כדי לבנות את האובייקט כשהוא מוריד מידע
    }

    public ParkingReport(String area, String description, String reportedBy) {
        this.area = area;
        this.description = description;
        this.reportedBy = reportedBy;
        this.likesCount = 0;
    }

    // Getters
    public String getArea() { return area; }
    public String getDescription() { return description; }
    public String getReportedBy() { return reportedBy; }
    public int getLikesCount() { return likesCount; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    // Setters
    public void setArea(String area) { this.area = area; }
    public void setDescription(String description) { this.description = description; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void addLike() {
        this.likesCount++;
    }
}