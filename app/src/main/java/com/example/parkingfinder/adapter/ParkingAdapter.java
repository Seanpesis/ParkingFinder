package com.example.parkingfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkingfinder.R;
import com.example.parkingfinder.model.ParkingReport;
import java.util.ArrayList;

// מחלקה שמנהלת את התצוגה של הרשימה
public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    // רשימת הדיווחים שנוצג
    private ArrayList<ParkingReport> parkingList;

    // בנאי שמקבל את הרשימה
    public ParkingAdapter(ArrayList<ParkingReport> parkingList) {
        this.parkingList = parkingList;
    }

    // יצירת השורה הוויזואלית (Inflate)
    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ ה-XML של שורה בודדת (ניצור אותו בהמשך)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    // חיבור המידע (Data) לתצוגה (View)
    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        // שליפת הדיווח הנוכחי לפי המיקום ברשימה
        ParkingReport currentReport = parkingList.get(position);

        // עדכון הטקסטים על המסך
        holder.tvArea.setText("אזור: " + currentReport.getArea());
        holder.tvDescription.setText(currentReport.getDescription());
        holder.tvReporter.setText("דווח ע''י: " + currentReport.getReportedBy());
        holder.btnLike.setText("לייקים: " + currentReport.getLikesCount());

        // האזנה ללחיצה על כפתור לייק
        holder.btnLike.setOnClickListener(v -> {
            currentReport.addLike(); // עדכון בלוגיקה
            notifyItemChanged(position); // רענון השורה הספציפית הזו במסך
        });

        holder.tvArea.setOnClickListener(v -> {
            // יצירת כתובת לחיפוש בגוגל מפות
            String mapUri = "geo:0,0?q=" + currentReport.getArea();
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(mapUri));
            // התחלת האפליקציה החיצונית
            v.getContext().startActivity(intent);
        });
    }

    // כמה פריטים יש ברשימה?
    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    // מחלקה פנימית שמחזיקה את הרכיבים של שורה אחת
    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvArea, tvDescription, tvReporter;
        Button btnLike;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            // מציאת הרכיבים לפי ה-ID שלהם בקובץ ה-XML
            tvArea = itemView.findViewById(R.id.tvArea);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReporter = itemView.findViewById(R.id.tvReporter);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}