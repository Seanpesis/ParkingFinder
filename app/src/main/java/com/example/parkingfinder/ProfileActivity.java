package com.example.parkingfinder;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements ParkingAdapter.OnItemClickListener {

    private TextView tvEmail, tvTotalReports, tvTotalParks;
    private ParkingAdapter myReportsAdapter, myParksAdapter;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        tvEmail = findViewById(R.id.tvEmail);
        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvTotalParks = findViewById(R.id.tvTotalParks);
        RecyclerView rvMyReports = findViewById(R.id.rvMyReports);
        RecyclerView rvMyParks = findViewById(R.id.rvMyParks);

        mDatabase = FirebaseDatabase.getInstance().getReference("reports");

        setupRecyclerViews(rvMyReports, rvMyParks);
        loadProfileData();
    }

    private void setupRecyclerViews(RecyclerView rvMyReports, RecyclerView rvMyParks) {
        rvMyReports.setLayoutManager(new LinearLayoutManager(this));
        myReportsAdapter = new ParkingAdapter(this);
        rvMyReports.setAdapter(myReportsAdapter);

        rvMyParks.setLayoutManager(new LinearLayoutManager(this));
        myParksAdapter = new ParkingAdapter(this);
        rvMyParks.setAdapter(myParksAdapter);
    }

    private void loadProfileData() {
        tvEmail.setText(currentUser.getEmail());

        Query myReportsQuery = mDatabase.orderByChild("userId").equalTo(currentUser.getUid());
        myReportsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ParkingReport> myReportsList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingReport report = data.getValue(ParkingReport.class);
                    if (report != null) {
                        report.setReportId(data.getKey());
                        myReportsList.add(report);
                    }
                }
                myReportsAdapter.submitList(myReportsList);
                tvTotalReports.setText(getString(R.string.total_reports_format, myReportsList.size()));
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
                List<ParkingReport> myParksList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingReport report = data.getValue(ParkingReport.class);
                    if (report != null) {
                        report.setReportId(data.getKey());
                        myParksList.add(report);
                    }
                }
                myParksAdapter.submitList(myParksList);
                tvTotalParks.setText(getString(R.string.total_parks_format, myParksList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load parks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClick(ParkingReport report) {
        if (report.getReportId() == null) return;
        DatabaseReference reportRef = mDatabase.child(report.getReportId());

        reportRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                ParkingReport p = mutableData.getValue(ParkingReport.class);
                if (p == null) return Transaction.success(mutableData);

                if (p.getLikes() == null) p.setLikes(new HashMap<>());

                if (p.getLikes().containsKey(currentUser.getUid())) {
                    p.setLikesCount(p.getLikesCount() - 1);
                    p.getLikes().remove(currentUser.getUid());
                } else {
                    p.setLikesCount(p.getLikesCount() + 1);
                    p.getLikes().put(currentUser.getUid(), true);
                }

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
        });
    }

    @Override
    public void onParkClick(ParkingReport report) {
        if (report.getReportId() == null) return;
        DatabaseReference reportRef = mDatabase.child(report.getReportId());

        if (report.getOccupiedBy() != null && report.getOccupiedBy().equals(currentUser.getUid())) {
            reportRef.child("occupied").setValue(false);
            reportRef.child("occupiedBy").setValue(null);
        } else if (!report.isOccupied()) {
            reportRef.child("occupied").setValue(true);
            reportRef.child("occupiedBy").setValue(currentUser.getUid());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
