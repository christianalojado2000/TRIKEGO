package com.example.trike;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DriverActivity extends AppCompatActivity {

    private static final int PICK_DRIVER_IMAGE = 100;
    private static final int PICK_LICENSE_IMAGE = 200;
    private static final String TAG = "DriverActivity";

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private DriverAdapter adapter;
    private ArrayList<Driver> driverList;
    private CollectionReference driversRef;

    private Uri driverImageUri = null;
    private Uri licenseImageUri = null;

    private View dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        recyclerView = findViewById(R.id.recyclerViewDrivers);
        fab = findViewById(R.id.fabAddDriver);

        driverList = new ArrayList<>();
        adapter = new DriverAdapter(this, driverList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        driversRef = FirebaseFirestore.getInstance().collection("drivers");

        // Firestore listener
        driversRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Firestore listener error: " + e.getMessage());
                return;
            }
            driverList.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Driver driver = doc.toObject(Driver.class);
                    if (driver != null) {
                        driver.setId(doc.getId());
                        driverList.add(driver);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });

        fab.setOnClickListener(v -> showAddDriverDialog());
    }

    private void showAddDriverDialog() {
        dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_driver, null);

        EditText nameInput = dialogView.findViewById(R.id.driverNameInput);
        EditText plateInput = dialogView.findViewById(R.id.plateNumberInput);
        EditText terminalInput = dialogView.findViewById(R.id.terminalInput);

        ImageView driverPreview = dialogView.findViewById(R.id.driverImagePreview);
        ImageView licensePreview = dialogView.findViewById(R.id.licenseImagePreview);

        // ⭐ FIXED IDs HERE
        Button selectDriverBtn = dialogView.findViewById(R.id.btnSelectImage);
        Button selectLicenseBtn = dialogView.findViewById(R.id.btnSelectLicense);

        driverImageUri = null;
        licenseImageUri = null;

        selectDriverBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_DRIVER_IMAGE);
        });

        selectLicenseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_LICENSE_IMAGE);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Driver")
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Add", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String name = nameInput.getText().toString().trim();
            String plate = plateInput.getText().toString().trim();
            String terminal = terminalInput.getText().toString().trim();

            if (name.isEmpty() || plate.isEmpty() || terminal.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (driverImageUri == null) {
                Toast.makeText(this, "Please select a driver image", Toast.LENGTH_SHORT).show();
                return;
            }

            if (licenseImageUri == null) {
                Toast.makeText(this, "Please select a license image", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadDriver(name, plate, terminal, dialog);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            if (requestCode == PICK_DRIVER_IMAGE) {
                driverImageUri = data.getData();
                ImageView preview = dialogView.findViewById(R.id.driverImagePreview);
                preview.setImageURI(driverImageUri);
            }

            if (requestCode == PICK_LICENSE_IMAGE) {
                licenseImageUri = data.getData();
                ImageView preview2 = dialogView.findViewById(R.id.licenseImagePreview);
                preview2.setImageURI(licenseImageUri);
            }
        }
    }

    private void uploadDriver(String name, String plate, String terminal, AlertDialog dialog) {
        String driverId = driversRef.document().getId();

        StorageReference driverRef = FirebaseStorage.getInstance()
                .getReference("driver_images/" + driverId + ".jpg");

        driverRef.putFile(driverImageUri).addOnSuccessListener(task ->
                driverRef.getDownloadUrl().addOnSuccessListener(driverUrl -> {

                    StorageReference licenseRef = FirebaseStorage.getInstance()
                            .getReference("license_images/" + driverId + ".jpg");

                    licenseRef.putFile(licenseImageUri).addOnSuccessListener(task2 ->
                            licenseRef.getDownloadUrl().addOnSuccessListener(licenseUrl -> {

                                saveDriver(driverId, name, plate, terminal,
                                        driverUrl.toString(), licenseUrl.toString(), dialog);

                            })).addOnFailureListener(e ->
                            Toast.makeText(this, "License upload failed", Toast.LENGTH_SHORT).show()
                    );

                })).addOnFailureListener(e ->
                Toast.makeText(this, "Driver image upload failed", Toast.LENGTH_SHORT).show()
        );
    }

    private void saveDriver(String driverId, String name, String plate, String terminal,
                            String imageUrl, String licenseUrl, AlertDialog dialog) {

        Driver driver = new Driver(driverId, name, plate, terminal, imageUrl, licenseUrl);

        driversRef.document(driverId).set(driver)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Driver Added!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore save failed: " + e.getMessage());
                    Toast.makeText(this, "Failed to save driver", Toast.LENGTH_SHORT).show();
                });
    }
}
