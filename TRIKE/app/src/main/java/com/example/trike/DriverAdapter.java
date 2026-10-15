package com.example.trike;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private final Context context;
    private final ArrayList<Driver> driverList;

    // ⭐ ADDED — click listener interface
    public interface OnItemClickListener {
        void onItemClick(Driver driver);
    }

    // ⭐ ADDED — listener variable
    private OnItemClickListener listener;

    // ⭐ ADDED — setter
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DriverAdapter(Context context, ArrayList<Driver> driverList) {
        this.context = context;
        this.driverList = driverList;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);

        holder.name.setText("Driver Name: " + driver.getName());
        holder.plate.setText("Plate Number: " + driver.getPlateNumber());
        holder.terminal.setText("Terminal: " + driver.getTerminal());

        // Main picture (face)
        if (driver.getImageUrl() != null && !driver.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(driver.getImageUrl())
                    .placeholder(R.drawable.ic_person)
                    .into(holder.driverImage);
        } else {
            holder.driverImage.setImageResource(R.drawable.ic_person);
        }

        // WHEN CLICK → SHOW LICENSE POPUP
        holder.itemView.setOnClickListener(v -> {

            // ⭐ ADDED — invoke external callback FIRST
            if (listener != null) listener.onItemClick(driver);

            if (driver.getLicenseUrl() == null || driver.getLicenseUrl().isEmpty()) {
                Toast.makeText(context, "No license image uploaded", Toast.LENGTH_SHORT).show();
                return;
            }

            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_license_image, null);

            ImageView licenseImageView = dialogView.findViewById(R.id.licenseImageView);

            Glide.with(context)
                    .load(driver.getLicenseUrl())
                    .placeholder(R.drawable.ic_person)
                    .into(licenseImageView);

            new AlertDialog.Builder(context)
                    .setTitle("Driver License")
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
        });

        // DELETE DRIVER
        holder.deleteBtn.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("drivers")
                    .document(driver.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Driver removed!", Toast.LENGTH_SHORT).show();
                        driverList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView name, plate, terminal;
        ImageButton deleteBtn;
        ImageView driverImage;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.driverName);
            plate = itemView.findViewById(R.id.plateNumber);
            terminal = itemView.findViewById(R.id.terminalText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            driverImage = itemView.findViewById(R.id.driverImage);
        }
    }
}
