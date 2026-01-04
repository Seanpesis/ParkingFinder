package com.example.parkingfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    private RecyclerView recyclerView;
    private ParkingAdapter adapter;
    private ArrayList<ParkingReport> reportsList; // הרשימה המקומית שלנו
    private DatabaseReference mDatabase; // חיבור למסד הנתונים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול הרשימה
        reportsList = new ArrayList<>();

        // חיבור לרכיבי התצוגה
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // אתחול האדפטר עם הרשימה הריקה (שתתמלא עוד רגע)
        adapter = new ParkingAdapter(reportsList);
        recyclerView.setAdapter(adapter);

        // חיבור ל-Firebase לתיקיית "reports"
        mDatabase = FirebaseDatabase.getInstance().getReference("reports");

        // --- קריאת הנתונים מהענן ---
        // אנחנו מוסיפים "מאזין" (Listener) שפועל כל פעם שיש שינוי בנתונים
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // שלב 1: מנקים את הרשימה הישנה כדי לא ליצור כפילויות
                reportsList.clear();

                // שלב 2: רצים בלולאה על כל הדיווחים שהגיעו מהענן
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // המרת המידע הגולמי (JSON) לאובייקט Java מסוג ParkingReport
                    ParkingReport report = postSnapshot.getValue(ParkingReport.class);
                    // הוספה לרשימה שלנו
                    reportsList.add(report);
                }

                // שלב 3: מודיעים לאדפטר שהמידע השתנה כדי שיצייר מחדש את המסך
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // טיפול במקרה של שגיאה (למשל אין אינטרנט או הרשאות)
                Toast.makeText(MainActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });

        // כפתור הוספה
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddReportActivity.class);
            startActivity(intent);
        });
    }
}