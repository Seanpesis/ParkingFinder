package com.example.parkingfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
import java.util.Objects;

public class AddReportActivity extends AppCompatActivity {

    private static final String TAG = "AddReportActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private AutoCompleteTextView actvCity;
    private TextInputEditText etStreet, etDesc;
    private ProgressBar progressBar;

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private double selectedLat = 0;
    private double selectedLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        actvCity = findViewById(R.id.actvCity);
        etStreet = findViewById(R.id.etStreet);
        etDesc = findViewById(R.id.etDesc);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        progressBar = findViewById(R.id.progressBar);

        mDatabase = FirebaseDatabase.getInstance().getReference("reports");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String[] cities = getResources().getStringArray(R.array.israeli_cities);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        actvCity.setAdapter(cityAdapter);

        getCurrentLocation();

        btnRefreshLocation.setOnClickListener(v -> getCurrentLocation());
        btnSave.setOnClickListener(v -> saveReportToFirebase());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
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
                String thoroughfare = Objects.toString(address.getThoroughfare(), "");
                String subThoroughfare = Objects.toString(address.getSubThoroughfare(), "");
                etStreet.setText(getString(R.string.street_address_format, thoroughfare, subThoroughfare));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to get address from location", e);
        }
    }

    private void saveReportToFirebase() {
        if (currentUser == null) {
            Toast.makeText(this, "עליך להיות מחובר כדי לדווח", Toast.LENGTH_SHORT).show();
            return;
        }

        String city = Objects.toString(actvCity.getText(), "").trim();
        String street = Objects.toString(etStreet.getText(), "").trim();
        String desc = Objects.toString(etDesc.getText(), "").trim();
        String area = city + ", " + street;

        if (city.isEmpty() || street.isEmpty()) {
            Toast.makeText(this, "יש למלא עיר ורחוב", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ParkingReport report = new ParkingReport(currentUser.getUid(), currentUser.getEmail(), area, desc, selectedLat, selectedLng);

        String key = mDatabase.push().getKey();
        if (key != null) {
            report.setReportId(key);
            mDatabase.child(key).setValue(report).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(AddReportActivity.this, "הדיווח נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMessage = "שגיאה בשמירה";
                    if (task.getException() != null && task.getException().getMessage() != null) {
                        errorMessage += ": " + task.getException().getMessage();
                    }
                    Toast.makeText(AddReportActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
