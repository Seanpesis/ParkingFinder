package com.example.parkingfinder;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.parkingfinder.model.ParkingReport;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddReportActivity extends AppCompatActivity {

    // משתנה שמחזיק את הכתובת למסד הנתונים
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        // אתחול החיבור ל-Firebase
        // אנחנו רוצים לשמור הכל תחת "תיקייה" שנקראת "reports"
        mDatabase = FirebaseDatabase.getInstance().getReference("reports");

        EditText etArea = findViewById(R.id.etArea);
        EditText etDesc = findViewById(R.id.etDesc);
        EditText etName = findViewById(R.id.etName);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String area = etArea.getText().toString();
            String desc = etDesc.getText().toString();
            String name = etName.getText().toString();

            if(area.isEmpty() || desc.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            // יצירת האובייקט
            ParkingReport newReport = new ParkingReport(area, desc, name);

            // יצירת מפתח ייחודי (ID) לדיווח הזה בענן
            String key = mDatabase.push().getKey();

            if (key != null) {
                // שמירת האובייקט בתוך ה-ID שנוצר
                mDatabase.child(key).setValue(newReport).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "נשמר בהצלחה בענן!", Toast.LENGTH_SHORT).show();
                        finish(); // סגירת המסך
                    } else {
                        Toast.makeText(this, "תקלה בשמירה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}