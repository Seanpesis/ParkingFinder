package com.example.parkingfinder.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class ParkingReport {
    private String reportId;
    private String userId;
    private String reporterEmail;
    private String area;
    private String description;
    private long timestamp;
    private double latitude;
    private double longitude;

    private int likesCount = 0;
    private Map<String, Boolean> likes = new HashMap<>();

    private boolean isOccupied = false;
    private String occupiedBy = null;

    public ParkingReport() {}

    public ParkingReport(String userId, String reporterEmail, String area, String description, double latitude, double longitude) {
        this.userId = userId;
        this.reporterEmail = reporterEmail;
        this.area = area;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

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

    public static final DiffUtil.ItemCallback<ParkingReport> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParkingReport oldItem, @NonNull ParkingReport newItem) {
            return oldItem.reportId.equals(newItem.reportId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParkingReport oldItem, @NonNull ParkingReport newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingReport that = (ParkingReport) o;
        return timestamp == that.timestamp &&
                latitude == that.latitude &&
                longitude == that.longitude &&
                likesCount == that.likesCount &&
                isOccupied == that.isOccupied &&
                Objects.equals(reportId, that.reportId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(reporterEmail, that.reporterEmail) &&
                Objects.equals(area, that.area) &&
                Objects.equals(description, that.description) &&
                Objects.equals(likes, that.likes) &&
                Objects.equals(occupiedBy, that.occupiedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, userId, reporterEmail, area, description, timestamp, latitude, longitude, likesCount, likes, isOccupied, occupiedBy);
    }
}
