package com.example.parkingfinder.data;

import com.example.parkingfinder.model.ParkingReport;
import java.util.ArrayList;

// מחלקה זו מדמה מסד נתונים (Database)
public class DataManager {
    // רשימה סטטית שתהיה נגישה מכל מקום באפליקציה
    // היא שומרת אובייקטים מסוג ParkingReport
    private static ArrayList<ParkingReport> reports = new ArrayList<>();

    // פונקציה לקבלת כל הדיווחים
    public static ArrayList<ParkingReport> getReports() {
        return reports; // מחזיר את הרשימה
    }

    // פונקציה להוספת דיווח חדש לרשימה
    public static void addReport(ParkingReport report) {
        reports.add(report); // מוסיף את הדיווח למערך
    }

    // פונקציה ליצירת נתונים התחלתיים
    static {
        reports.add(new ParkingReport("תל אביב", "חניון רידינג, יש מקום", "דני"));
        reports.add(new ParkingReport("הרצליה", "קניון שבעת הכוכבים קומה 2", "רונית"));
    }
}