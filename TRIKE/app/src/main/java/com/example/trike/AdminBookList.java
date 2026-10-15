package com.example.trike;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminBookList extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private AdminBookingAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private ListenerRegistration bookingListener;

    private String terminalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_book_list);

        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        adapter = new AdminBookingAdapter(this, bookingList);
        recyclerViewBookings.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        terminalName = getIntent().getStringExtra("terminal");
        if (terminalName == null || terminalName.isEmpty()) {
            terminalName = "bayan";
        }

        listenForBookingUpdates(terminalName.toLowerCase());
    }

    private void listenForBookingUpdates(String terminal) {
        String collectionName = "bookings_" + terminal;

        if (bookingListener != null) {
            bookingListener.remove();
        }

        bookingListener = db.collection(collectionName)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot == null) return;

                    for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        Booking booking = doc.toObject(Booking.class);
                        if (booking == null) continue;

                        booking.setBookingId(doc.getId());
                        booking.setTerminal(terminal);

                        switch (change.getType()) {
                            case ADDED:
                                boolean exists = false;
                                for (Booking b : bookingList) {
                                    if (b.getBookingId().equals(booking.getBookingId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) bookingList.add(booking);
                                break;

                            case MODIFIED:
                                for (int i = 0; i < bookingList.size(); i++) {
                                    if (bookingList.get(i).getBookingId().equals(booking.getBookingId())) {
                                        bookingList.set(i, booking);
                                        break;
                                    }
                                }
                                break;

                            case REMOVED:
                                bookingList.removeIf(b ->
                                        b.getBookingId().equals(booking.getBookingId()));
                                break;
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (bookingList.isEmpty()) {
                        Toast.makeText(this, "No bookings found for " + terminal, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingListener != null) {
            bookingListener.remove();
        }
    }
}
