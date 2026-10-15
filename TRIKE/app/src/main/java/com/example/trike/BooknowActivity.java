package com.example.trike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BooknowActivity extends AppCompatActivity {

    private String selectedLocation = "";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText inputPhone, inputName, inputPickupAddress, inputGuardianNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booknow);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Spinner spinner = findViewById(R.id.locationSpinner);
        Button proceedButton = findViewById(R.id.proceedButton);
        inputPhone = findViewById(R.id.inputPhoneNumber);
        inputName = findViewById(R.id.inputPassengerName);
        inputPickupAddress = findViewById(R.id.inputPickupAddress);
        inputGuardianNumber = findViewById(R.id.inputGuardianNumber);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Kanto-Southwoods: ₱80.00");
        arrayList.add("Kanto-Bayan: ₱50.00");
        arrayList.add("Kanto-Ilaya: ₱80.00");
        arrayList.add("Kanto-TC Graceland: ₱100.00");
        arrayList.add("Southwoods-Bayan: ₱100.00");
        arrayList.add("Bayan-Southwoods: ₱100.00");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedLocation = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLocation = "";
            }
        });

        proceedButton.setOnClickListener(v -> {
            String phoneNumber = inputPhone.getText().toString().trim();
            String passengerName = inputName.getText().toString().trim();
            String pickupAddress = inputPickupAddress.getText().toString().trim();
            String guardianNumber = inputGuardianNumber.getText().toString().trim();

            if (passengerName.isEmpty() || pickupAddress.isEmpty() || selectedLocation.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(BooknowActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            saveBookingToFirestore(selectedLocation, phoneNumber, passengerName, pickupAddress, guardianNumber);
        });
    }

    private void saveBookingToFirestore(String location, String phoneNumber, String passengerName, String pickupAddress, String guardianNumber) {
        String eta = (new Random().nextInt(11) + 5) + " mins";
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";


        String terminal;
        String lowerLocation = location.toLowerCase();

        if (lowerLocation.startsWith("kanto")) {
            terminal = "kanto";
        } else if (lowerLocation.startsWith("bayan")) {
            terminal = "bayan";
        } else if (lowerLocation.startsWith("southwoods")) {
            terminal = "southwoods";
        } else {
            terminal = "other";
        }

        String collectionName = "bookings_" + terminal;

        Map<String, Object> booking = new HashMap<>();
        booking.put("passengerName", passengerName);
        booking.put("phoneNumber", phoneNumber);
        booking.put("guardianNumber", guardianNumber);
        booking.put("pickupAddress", pickupAddress);
        booking.put("location", location);
        booking.put("status", "Pending");
        booking.put("userId", userId);
        booking.put("eta", eta);
        booking.put("terminal", terminal);

        db.collection(collectionName)
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    booking.put("bookingId", docId);
                    db.collection(collectionName).document(docId).update("bookingId", docId);

                    Toast.makeText(BooknowActivity.this, "Booking sent to " + terminal + " admin!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(BooknowActivity.this, UserHistoryActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(BooknowActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
