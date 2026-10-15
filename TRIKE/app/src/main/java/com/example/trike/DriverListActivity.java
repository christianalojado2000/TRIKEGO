package com.example.trike;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DriverListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String bookingId, terminal;
    private ArrayList<Driver> driverList = new ArrayList<>();
    private DriverAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("bookingId");
        terminal = getIntent().getStringExtra("terminal");

        if (terminal != null) terminal = terminal.trim().toLowerCase();

        loadDrivers();
    }

    private void loadDrivers() {
        db.collection("drivers")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    driverList.clear();

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Driver driver = doc.toObject(Driver.class);
                            if (driver == null) continue;

                            driver.setId(doc.getId());

                            if (driver.getTerminal() != null &&
                                    driver.getTerminal().trim().equalsIgnoreCase(terminal)) {
                                driverList.add(driver);
                            }
                        }
                    }

                    if (driverList.isEmpty()) {
                        Toast.makeText(this, "No drivers available for " + terminal, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showDriverDialog();
                    }
                });
    }

    private void showDriverDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_driver_list, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewDriverList);

        adapter = new DriverAdapter(this, driverList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Driver")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, w) -> finish())
                .create();

        adapter.setOnItemClickListener(driver -> {
            assignDriver(driver.getId(), driver.getName());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void assignDriver(String driverId, String driverName) {
        if (terminal == null || bookingId == null) return;

        String collectionName = "bookings_" + terminal.toLowerCase();

        db.collection(collectionName)
                .document(bookingId)
                .update(
                        "status", "Assigned",
                        "driverId", driverId,
                        "driverName", driverName
                )
                .addOnSuccessListener(aVoid -> {
                    db.collection("bookings")
                            .document(bookingId)
                            .update(
                                    "status", "Assigned",
                                    "driverId", driverId,
                                    "driverName", driverName
                            );
                    Toast.makeText(this, "Driver " + driverName + " assigned!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
