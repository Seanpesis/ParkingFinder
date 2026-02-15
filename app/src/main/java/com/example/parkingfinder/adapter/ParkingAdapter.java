package com.example.parkingfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingfinder.R;
import com.example.parkingfinder.model.ParkingReport;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ParkingAdapter extends ListAdapter<ParkingReport, ParkingAdapter.ParkingViewHolder> {

    private final OnItemClickListener listener;
    private final FirebaseUser currentUser;

    public interface OnItemClickListener {
        void onLikeClick(ParkingReport report);
        void onParkClick(ParkingReport report);
    }

    public ParkingAdapter(@NonNull OnItemClickListener listener) {
        super(ParkingReport.DIFF_CALLBACK);
        this.listener = listener;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingReport currentReport = getItem(position);
        holder.bind(currentReport, currentUser, listener);
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvArea, tvDescription, tvReporter;
        MaterialButton btnLike, btnPark;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReporter = itemView.findViewById(R.id.tvReporter);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnPark = itemView.findViewById(R.id.btnPark);
        }

        public void bind(final ParkingReport report, final FirebaseUser currentUser, final OnItemClickListener listener) {
            String reporterEmail = report.getReporterEmail() != null ? report.getReporterEmail() : "אלמוני";

            tvArea.setText(report.getArea());
            tvDescription.setText(report.getDescription());
            tvReporter.setText(itemView.getContext().getString(R.string.reported_by_format, reporterEmail));

            btnLike.setText(String.valueOf(report.getLikesCount()));
            if (currentUser != null && report.getLikes() != null && report.getLikes().containsKey(currentUser.getUid())) {
                btnLike.setIconResource(R.drawable.ic_like_filled);
            } else {
                btnLike.setIconResource(R.drawable.ic_like);
            }

            if (report.isOccupied()) {
                if (currentUser != null && currentUser.getUid().equals(report.getOccupiedBy())) {
                    btnPark.setText("יוצא מהחניה");
                    btnPark.setEnabled(true);
                } else {
                    btnPark.setText("תפוס");
                    btnPark.setEnabled(false);
                }
            } else {
                btnPark.setText("החנתי שם");
                btnPark.setEnabled(true);
            }

            btnLike.setOnClickListener(v -> listener.onLikeClick(report));
            btnPark.setOnClickListener(v -> listener.onParkClick(report));
        }
    }
}
