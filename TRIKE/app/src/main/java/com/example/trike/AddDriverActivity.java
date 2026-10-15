package com.example.trike;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddDriverActivity extends AppCompatActivity {

    private ImageView driverImage, licenseImage;
    private EditText edtName, edtPlate, edtTerminal;
    private Button btnSave;
    private Uri driverImageUri, licenseImageUri;

    private final int PICK_DRIVER = 1;
    private final int PICK_LICENSE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);

        driverImage = findViewById(R.id.driverImage);
        licenseImage = findViewById(R.id.licenseImage);


        driverImage.setOnClickListener(v -> openGallery(PICK_DRIVER));
        licenseImage.setOnClickListener(v -> openGallery(PICK_LICENSE));

        btnSave.setOnClickListener(v -> saveDriver());
    }

    private void openGallery(int code) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == PICK_DRIVER) {
                driverImageUri = data.getData();
                driverImage.setImageURI(driverImageUri);
            }

            if (requestCode == PICK_LICENSE) {
                licenseImageUri = data.getData();
                licenseImage.setImageURI(licenseImageUri);
            }
        }
    }

    private void saveDriver() {
        String name = edtName.getText().toString().trim();
        String plate = edtPlate.getText().toString().trim();
        String terminal = edtTerminal.getText().toString().trim();

        if (name.isEmpty() || plate.isEmpty() || terminal.isEmpty() ||
                driverImageUri == null || licenseImageUri == null) {
            Toast.makeText(this, "Complete all fields + upload both images", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving...");
        pd.show();

        // Upload DRIVER IMAGE first
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("drivers/" + UUID.randomUUID());

        storageRef.putFile(driverImageUri).addOnSuccessListener(task1 ->
                storageRef.getDownloadUrl().addOnSuccessListener(driverUrl -> {

                    // Upload LICENSE image second
                    StorageReference licenseRef = FirebaseStorage.getInstance()
                            .getReference("licenses/" + UUID.randomUUID());

                    licenseRef.putFile(licenseImageUri).addOnSuccessListener(task2 ->
                            licenseRef.getDownloadUrl().addOnSuccessListener(licenseUrl -> {

                                // Save to Firestore
                                String id = UUID.randomUUID().toString();

                                Driver driver = new Driver(
                                        id,
                                        name,
                                        plate,
                                        terminal,
                                        driverUrl.toString(),
                                        licenseUrl.toString()
                                );

                                FirebaseFirestore.getInstance().collection("drivers")
                                        .document(id)
                                        .set(driver)
                                        .addOnSuccessListener(aVoid -> {
                                            pd.dismiss();
                                            Toast.makeText(this, "Driver Saved!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            pd.dismiss();
                                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                            })).addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(this, "License upload failed", Toast.LENGTH_SHORT).show();
                    });

                })).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Driver image upload failed", Toast.LENGTH_SHORT).show();
        });
    }
}
