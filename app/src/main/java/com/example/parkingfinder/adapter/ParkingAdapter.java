package com.example.parkingfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkingfinder.R;
import com.example.parkingfinder.model.ParkingReport;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private List<ParkingReport> parkingList;
    private final FirebaseUser currentUser;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onLikeClick(ParkingReport report);
        void onParkClick(ParkingReport report);
    }

    public ParkingAdapter(List<ParkingReport> parkingList, OnItemClickListener listener) {
        this.parkingList = parkingList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingReport currentReport = parkingList.get(position);

        if (currentReport.getLikes() == null) {
            currentReport.setLikes(new HashMap<>());
        }

        String reporterEmail = currentReport.getReporterEmail() != null ? currentReport.getReporterEmail() : "אלמוני";

        holder.tvArea.setText(currentReport.getArea());
        holder.tvDescription.setText(currentReport.getDescription());
        holder.tvReporter.setText("דווח ע\"י: " + reporterEmail);

        // Set like button state
        holder.btnLike.setText(String.valueOf(currentReport.getLikesCount()));
        if (currentUser != null && currentReport.getLikes().containsKey(currentUser.getUid())) {
            holder.btnLike.setIconResource(R.drawable.ic_like_filled);
        } else {
            holder.btnLike.setIconResource(R.drawable.ic_like);
        }

        // Set park/un-park button state
        if (currentReport.isOccupied()) {
            if (currentUser != null && currentUser.getUid().equals(currentReport.getOccupiedBy())) {
                holder.btnPark.setText("יוצא מהחניה");
                holder.btnPark.setEnabled(true);
            } else {
                holder.btnPark.setText("תפוס");
                holder.btnPark.setEnabled(false);
            }
        } else {
            holder.btnPark.setText("החנתי שם");
            holder.btnPark.setEnabled(true);
        }

        // Set listeners
        holder.btnLike.setOnClickListener(v -> listener.onLikeClick(currentReport));
        holder.btnPark.setOnClickListener(v -> listener.onParkClick(currentReport));
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public void updateList(List<ParkingReport> newList) {
        parkingList = newList;
        notifyDataSetChanged();
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvArea, tvDescription, tvReporter;
        MaterialButton btnLike, btnPark; // Use MaterialButton

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReporter = itemView.findViewById(R.id.tvReporter);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnPark = itemView.findViewById(R.id.btnPark);
        }
    }
}
