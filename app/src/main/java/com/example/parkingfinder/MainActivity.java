package com.example.parkingfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import com.example.parkingfinder.adapter.ParkingAdapter;
import com.example.parkingfinder.model.ParkingReport;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // הגדרת משתנים
    private RecyclerView recyclerView;
    private ParkingAdapter adapter;
    private ArrayList<ParkingReport> reportsList;

    private DatabaseReference mDatabase; // חיבור למסד הנתונים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // חיבור לקובץ העיצוב שהוכן למעלה

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reportsList = new ArrayList<>();

        //  חיבור לרכיבים במסך
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        Button btnGoToMap = findViewById(R.id.btnGoToMap);

        //  הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParkingAdapter(reportsList); // יצירת האדפטר
        recyclerView.setAdapter(adapter); // חיבור האדפטר לרשימה

        //  חיבור ל-Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("reports");

        //  הפעלת האזנה לשינויים במסד הנתונים
        getAllReportsFromFirebase();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddReportActivity.class);
                startActivity(intent);
            }
        });

        btnGoToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    // פונקציה שקוראת את הנתונים ומעדכנת את הרשימה בזמן אמת
    private void getAllReportsFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // מנקים את הרשימה הישנה כדי לא ליצור כפילויות
                reportsList.clear();

                // רצים על כל הדיווחים שיש בענן
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingReport report = data.getValue(ParkingReport.class);

                    // הוספה לרשימה שלנו
                    if (report != null) {
                        reportsList.add(report);
                    }
                }

                // מודיעים לאדפטר שהמידע התעדכן
                adapter.notifyDataSetChanged();

                if (!reportsList.isEmpty()) {
                    recyclerView.scrollToPosition(reportsList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}