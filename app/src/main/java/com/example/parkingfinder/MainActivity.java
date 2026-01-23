package com.example.parkingfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingfinder.adapter.ParkingAdapter;
import com.example.parkingfinder.model.ParkingReport;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements ParkingAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ParkingAdapter adapter;
    private List<ParkingReport> allReports;
    private AutoCompleteTextView actvCitySearch;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
            return;
        }

        allReports = new ArrayList<>();

        actvCitySearch = findViewById(R.id.actvCitySearch);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        setupCitySearch();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // We pass an empty list to start, it will be populated from Firebase
        adapter = new ParkingAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("reports");

        getAllReportsFromFirebase();

        fabAdd.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddReportActivity.class)));
    }

    private void setupCitySearch() {
        String[] cities = getResources().getStringArray(R.array.israeli_cities);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        actvCitySearch.setAdapter(cityAdapter);
        actvCitySearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            filterReportsByCity(selectedCity);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.action_map) {
            startActivity(new Intent(this, MapsActivity.class));
            return true;
        } else if (itemId == R.id.action_refresh) {
            actvCitySearch.setText("", false);
            getAllReportsFromFirebase();
            return true;
        } else if (itemId == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllReportsFromFirebase() {
        mDatabase.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReports.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingReport report = data.getValue(ParkingReport.class);
                    // *** FIX: Only add reports that are NOT occupied to the main list ***
                    if (report != null && !report.isOccupied()) {
                        report.setReportId(data.getKey());
                        allReports.add(report);
                    }
                }
                Collections.reverse(allReports);
                filterReportsByCity(actvCitySearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterReportsByCity(String city) {
        List<ParkingReport> filteredReports;
        if (city == null || city.isEmpty() || city.equals("כל הערים")) {
            filteredReports = new ArrayList<>(allReports);
        } else {
            filteredReports = allReports.stream()
                    .filter(report -> report.getArea().contains(city))
                    .collect(Collectors.toList());
        }
        adapter.updateList(filteredReports);
    }

    @Override
    public void onLikeClick(ParkingReport report) {
        // ... like logic is correct
    }

    @Override
    public void onParkClick(ParkingReport report) {
        if (report.getReportId() == null) return;

        DatabaseReference reportRef = mDatabase.child(report.getReportId());

        if (report.isOccupied()) {
            // Can only be freed by the user who occupied it
            if (currentUser != null && currentUser.getUid().equals(report.getOccupiedBy())) {
                reportRef.child("occupied").setValue(false);
                reportRef.child("occupiedBy").setValue(null);
            }
        } else {
            // Can be occupied by any user
            reportRef.child("occupied").setValue(true);
            reportRef.child("occupiedBy").setValue(currentUser.getUid());
        }
    }
}
