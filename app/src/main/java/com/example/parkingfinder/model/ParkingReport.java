package com.example.parkingfinder.model;

import java.util.HashMap;
import java.util.Map;

public class ParkingReport {
    private String reportId;
    private String userId;
    private String reporterEmail;
    private String area;
    private String description;
    private long timestamp;
    private double latitude;
    private double longitude;

    // New fields for likes
    private int likesCount = 0;
    private Map<String, Boolean> likes = new HashMap<>();

    // New fields for parking status
    private boolean isOccupied = false;
    private String occupiedBy = null; // UID of the user who parked

    public ParkingReport() {
        // Default constructor required for calls to DataSnapshot.getValue(ParkingReport.class)
    }

    public ParkingReport(String userId, String reporterEmail, String area, String description, double latitude, double longitude) {
        this.userId = userId;
        this.reporterEmail = reporterEmail;
        this.area = area;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public String getOccupiedBy() { return occupiedBy; }
    public void setOccupiedBy(String occupiedBy) { this.occupiedBy = occupiedBy; }
}
