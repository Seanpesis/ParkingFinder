package com.example.parkingfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.parkingfinder.model.ParkingReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddReportActivity extends AppCompatActivity {

    // הגדרת משתנים לרכיבי המסך
    private EditText etArea, etDesc, etName;
    private Button btnSave, btnRefreshLocation, btnCancel;
    private ProgressBar progressBar;

    // משתנים לטיפול במיקום ובמסד הנתונים
    private FusedLocationProviderClient fusedLocationClient; // רכיב ה-GPS
    private DatabaseReference mDatabase; // החיבור ל-Firebase

    // משתנים לשמירת הקואורדינטות שנמצאו
    private double selectedLat = 0;
    private double selectedLng = 0;

    // קוד מזהה לבקשת הרשאה
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        etArea = findViewById(R.id.etArea);
        etDesc = findViewById(R.id.etDesc);
        etName = findViewById(R.id.etName);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        progressBar = findViewById(R.id.progressBar);

        //  אתחול Firebase ושירותי מיקום
        mDatabase = FirebaseDatabase.getInstance().getReference("reports");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getCurrentLocation();

        btnRefreshLocation.setOnClickListener(v -> getCurrentLocation());

        btnSave.setOnClickListener(v -> saveReportToFirebase());

        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * פונקציה שאחראית למצוא את המיקום הנוכחי של המכשיר
     */
    private void getCurrentLocation() {
        // בדיקה אם יש לנו הרשאה לגשת למיקום
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // אם אין הרשאה - נבקש אותה מהמשתמש (קופץ חלון)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Toast.makeText(this, "מאתר מיקום...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // בדיקה אם נמצא מיקום
                if (location != null) {
                    selectedLat = location.getLatitude(); // שמירת קו רוחב
                    selectedLng = location.getLongitude(); // שמירת קו אורך

                    updateAddressFromLocation(selectedLat, selectedLng);
                } else {
                    Toast.makeText(AddReportActivity.this, "לא נמצא מיקום, וודא שה-GPS דולק", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * פונקציה שהופכת קואורדינטות לכתובת טקסט (למשל: הרצל 5 תל אביב)
     */
    private void updateAddressFromLocation(double lat, double lng) {
        // כלי של גוגל להמרת כתובות
        Geocoder geocoder = new Geocoder(this, new Locale("he")); // he = עברית

        try {
            // מנסה למצוא כתובת אחת לפי המיקום
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // לוקח את שורת הכתובת הראשונה ומציג אותה בשדה הטקסט
                String addressText = address.getAddressLine(0);
                etArea.setText(addressText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // אם נכשל, נציג פשוט את הקואורדינטות
            etArea.setText("מיקום: " + lat + ", " + lng);
        }
    }

    /**
     * פונקציה לשמירת הנתונים ב-Firebase
     */
    private void saveReportToFirebase() {
        // שליפת הטקסטים מהשדות
        String area = etArea.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String name = etName.getText().toString().trim();

        // ולידציה: בדיקה ששום דבר לא ריק
        if (area.isEmpty()) {
            etArea.setError("חובה להזין אזור");
            return;
        }
        if (desc.isEmpty()) {
            etDesc.setError("חובה להזין תיאור");
            return;
        }
        if (name.isEmpty()) {
            etName.setError("חובה להזין שם");
            return;
        }

        // הצגת ספינר טעינה והסתרת כפתור השמירה
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.INVISIBLE);

        // יצירת אובייקט הדיווח
        ParkingReport report = new ParkingReport(area, desc, name);

        // הוספת נתוני המיקום לאובייקט
        if (selectedLat != 0 && selectedLng != 0) {
            report.setLatitude(selectedLat);
            report.setLongitude(selectedLng);
        }

        // יצירת מפתח ייחודי ב-Firebase
        String key = mDatabase.push().getKey();

        if (key != null) {
            // שליחה לענן
            mDatabase.child(key).setValue(report).addOnCompleteListener(task -> {
                // מפסיקים את הטעינה
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    Toast.makeText(AddReportActivity.this, "הדיווח נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת המסך וחזרה לרשימה
                } else {
                    btnSave.setVisibility(View.VISIBLE); // מחזירים את הכפתור
                    Toast.makeText(AddReportActivity.this, "שגיאה בשמירה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // פונקציה שמטפלת בתשובה של המשתמש לבקשת ההרשאה
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // אם המשתמש אישר - ננסה שוב לקחת מיקום
                getCurrentLocation();
            } else {
                Toast.makeText(this, "האפליקציה חייבת מיקום כדי לשמור חניה", Toast.LENGTH_LONG).show();
            }
        }
    }
}