package com.example.trike;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserHistoryActivity extends AppCompatActivity {

    ImageButton userfare, userH, logBtn;
    RecyclerView recyclerViewBookings;
    BookingAdapter bookingAdapter;
    List<Booking> bookingList = new ArrayList<>();

    TextView txtNoBookings;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userfare = findViewById(R.id.ufare);
        userH = findViewById(R.id.userhistory);
        logBtn = findViewById(R.id.logBtn); // ✅ replaced backBtn
        txtNoBookings = findViewById(R.id.txtNoBookings);

        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        bookingAdapter = new BookingAdapter(UserHistoryActivity.this, bookingList);
        recyclerViewBookings.setAdapter(bookingAdapter);

        listenBookings("bookings_bayan");
        listenBookings("bookings_kanto");
        listenBookings("bookings_southwoods");

        userfare.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), MainActivity.class)));

        userH.setOnClickListener(v ->
                Toast.makeText(this, "You are already on this page", Toast.LENGTH_SHORT).show());

        // ✅ LOGOUT BUTTON ACTION
        logBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UserHistoryActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void listenBookings(String collectionName) {
        db.collection(collectionName)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", error.getMessage());
                        return;
                    }

                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {

                        String id = dc.getDocument().getId();

                        if (!currentUserId.equals(dc.getDocument().getString("userId")))
                            continue;

                        String status = getSafeString(dc, "status");

                        if (status.equalsIgnoreCase("Declined")) {
                            removeBooking(id);
                            continue;
                        }

                        Booking booking = new Booking();
                        booking.setPassengerName(getSafeString(dc, "passengerName"));
                        booking.setPhoneNumber(getSafeString(dc, "phoneNumber"));
                        booking.setGuardianNumber(getSafeString(dc, "guardianNumber"));
                        booking.setPickupAddress(getSafeString(dc, "pickupAddress"));
                        booking.setLocation(getSafeString(dc, "location"));
                        booking.setDriverName(getSafeString(dc, "driverName"));
                        booking.setStatus(status);
                        booking.setEta(getSafeString(dc, "eta"));
                        booking.setTerminal(getSafeString(dc, "terminal"));
                        booking.setBookingId(id);

                        int index = getBookingIndex(id);

                        switch (dc.getType()) {
                            case ADDED:
                                if (index == -1) bookingList.add(booking);
                                break;
                            case MODIFIED:
                                if (index != -1) bookingList.set(index, booking);
                                break;
                            case REMOVED:
                                removeBooking(id);
                                break;
                        }
                    }

                    bookingList.sort((b1, b2) ->
                            b2.getBookingId().compareTo(b1.getBookingId()));

                    bookingAdapter.notifyDataSetChanged();
                    updateNoBookingMessage();
                });
    }

    private int getBookingIndex(String id) {
        for (int i = 0; i < bookingList.size(); i++) {
            if (bookingList.get(i).getBookingId().equals(id)) return i;
        }
        return -1;
    }

    private void removeBooking(String id) {
        int index = getBookingIndex(id);
        if (index != -1) {
            bookingList.remove(index);
            bookingAdapter.notifyItemRemoved(index);
            updateNoBookingMessage();
        }
    }

    private String getSafeString(DocumentChange dc, String field) {
        Object value = dc.getDocument().get(field);
        return value != null ? value.toString() : "";
    }

    private void updateNoBookingMessage() {
        txtNoBookings.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
