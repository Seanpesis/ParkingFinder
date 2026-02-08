package com.example.parkingfinder;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingfinder.adapter.ParkingAdapter;
import com.example.parkingfinder.model.ParkingReport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements ParkingAdapter.OnItemClickListener {

    private TextView tvEmail, tvTotalReports, tvTotalParks;
    private RecyclerView rvMyReports, rvMyParks;
    private ParkingAdapter myReportsAdapter, myParksAdapter;
    private List<ParkingReport> myReportsList, myParksList;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        tvEmail = findViewById(R.id.tvEmail);
        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvTotalParks = findViewById(R.id.tvTotalParks);
        rvMyReports = findViewById(R.id.rvMyReports);
        rvMyParks = findViewById(R.id.rvMyParks);

        mDatabase = FirebaseDatabase.getInstance().getReference("reports");

        setupRecyclerViews();
        loadProfileData();
    }

    private void setupRecyclerViews() {
        myReportsList = new ArrayList<>();
        myParksList = new ArrayList<>();

        rvMyReports.setLayoutManager(new LinearLayoutManager(this));
        myReportsAdapter = new ParkingAdapter(myReportsList, this);
        rvMyReports.setAdapter(myReportsAdapter);

        rvMyParks.setLayoutManager(new LinearLayoutManager(this));
        myParksAdapter = new ParkingAdapter(myParksList, this);
        rvMyParks.setAdapter(myParksAdapter);
    }

    private void loadProfileData() {
        tvEmail.setText(currentUser.getEmail());

        Query myReportsQuery = mDatabase.orderByChild("userId").equalTo(currentUser.getUid());
        myReportsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myReportsList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingReport report = data.getValue(ParkingReport.class);
                    if (report != null) {
                        report.setReportId(data.getKey());
                        myReportsList.add(report);
                    }
                }
                myReportsAdapter.updateList(myReportsList);
                tvTotalReports.setText("סך הכל דיווחים: " + myReportsList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load reports.", Toast.LENGTH_SHORT).show();
            }
        });

        Query myParksQuery = mDatabase.orderByChild("occupiedBy").equalTo(currentUser.getUid());
        myParksQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myParksList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingReport report = data.getValue(ParkingReport.class);
                    if (report != null) {
                        report.setReportId(data.getKey());
                        myParksList.add(report);
                    }
                }
                myParksAdapter.updateList(myParksList);
                tvTotalParks.setText("סך הכל חניות: " + myParksList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load parks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClick(ParkingReport report) {
        // Not implemented in this screen for simplicity
    }

    @Override
    public void onParkClick(ParkingReport report) {
        if (report.getReportId() == null) {
            Toast.makeText(this, "שגיאה: לא ניתן לעדכן דיווח", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reportRef = mDatabase.child(report.getReportId());

        if (report.getOccupiedBy() != null && report.getOccupiedBy().equals(currentUser.getUid())) {
            // Free up the parking spot
            reportRef.child("occupied").setValue(false);
            reportRef.child("occupiedBy").setValue(null);
        } else if (!report.isOccupied()) {
            // Park in the spot (only if it's free)
            reportRef.child("occupied").setValue(true);
            reportRef.child("occupiedBy").setValue(currentUser.getUid());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
