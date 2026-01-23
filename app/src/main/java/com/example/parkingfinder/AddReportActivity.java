package com.example.parkingfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.parkingfinder.model.ParkingReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddReportActivity extends AppCompatActivity {

    private AutoCompleteTextView actvCity;
    private TextInputEditText etStreet, etDesc;
    private Button btnSave, btnRefreshLocation;
    private ProgressBar progressBar;

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private double selectedLat = 0;
    private double selectedLng = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        actvCity = findViewById(R.id.actvCity);
        etStreet = findViewById(R.id.etStreet);
        etDesc = findViewById(R.id.etDesc);
        btnSave = findViewById(R.id.btnSave);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        progressBar = findViewById(R.id.progressBar);

        mDatabase = FirebaseDatabase.getInstance().getReference("reports");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Populate the city list from the array resource
        String[] cities = getResources().getStringArray(R.array.israeli_cities);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        actvCity.setAdapter(cityAdapter);

        getCurrentLocation();

        btnRefreshLocation.setOnClickListener(v -> getCurrentLocation());
        btnSave.setOnClickListener(v -> saveReportToFirebase());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                updateAddressFromLocation(selectedLat, selectedLng);
            } else {
                Toast.makeText(AddReportActivity.this, "לא נמצא מיקום, וודא שה-GPS דולק", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAddressFromLocation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, new Locale("he"));
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                if (city != null) {
                    actvCity.setText(city, false);
                }
                etStreet.setText(address.getThoroughfare() + " " + address.getSubThoroughfare());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveReportToFirebase() {
        if (currentUser == null) {
            Toast.makeText(this, "עליך להיות מחובר כדי לדווח", Toast.LENGTH_SHORT).show();
            return;
        }

        String city = actvCity.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String area = city + ", " + street;

        if (city.isEmpty() || street.isEmpty()) {
            Toast.makeText(this, "יש למלא עיר ורחוב", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();

        ParkingReport report = new ParkingReport(userId, email, area, desc, selectedLat, selectedLng);

        String key = mDatabase.push().getKey();
        if (key != null) {
            report.setReportId(key);
            mDatabase.child(key).setValue(report).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(AddReportActivity.this, "הדיווח נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddReportActivity.this, "שגיאה בשמירה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "האפליקציה חייבת מיקום כדי לשמור חניה", Toast.LENGTH_LONG).show();
            }
        }
    }
}
