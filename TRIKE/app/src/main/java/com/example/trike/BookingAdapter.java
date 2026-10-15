package com.example.trike;

import android.app.AlertDialog;
import android.content.Context;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final Context context;
    private final List<Booking> bookingList;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.txtLocation.setText("To: " + safe(booking.getLocation()));
        holder.txtStatus.setText("Status: " + safe(booking.getStatus()));
        holder.txtPassenger.setText("Passenger: " + safe(booking.getPassengerName()));
        holder.txtPhoneNumber.setText("Phone: " + safe(booking.getPhoneNumber()));
        holder.txtGuardianNumber.setText("Guardian Number: " + safe(booking.getGuardianNumber()));
        holder.txtEta.setText("ETA: " + safe(booking.getEta()));
        holder.txtDriver.setText("Driver: " + safe(booking.getDriverName()));
        holder.txtPickup.setText("Pickup: " + safe(booking.getPickupAddress()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Determine correct collection
        String terminalCollection;
        switch (safe(booking.getTerminal()).toLowerCase()) {
            case "kanto": terminalCollection = "bookings_kanto"; break;
            case "southwoods": terminalCollection = "bookings_southwoods"; break;
            case "bayan": terminalCollection = "bookings_bayan"; break;
            default: terminalCollection = "bookings"; break;
        }

        String status = booking.getStatus();

        // ===== BUTTON STATE LOGIC =====
        if ("Assigned".equalsIgnoreCase(status)) {
            holder.btnArrived.setVisibility(View.VISIBLE);
            holder.btnArrived.setText("Already Picked Up");
            holder.btnArrived.setOnClickListener(v ->
                    markPickedUp(db, terminalCollection, booking, holder)
            );

        } else if ("PickedUp".equalsIgnoreCase(status)) {
            holder.btnArrived.setVisibility(View.VISIBLE);
            holder.btnArrived.setText("Arrived");
            holder.btnArrived.setOnClickListener(v ->
                    markArrived(db, terminalCollection, booking, holder)
            );

        } else if ("Arrived".equalsIgnoreCase(status)) {
            holder.btnArrived.setVisibility(View.VISIBLE);
            holder.btnArrived.setText("Delete");
            holder.btnArrived.setOnClickListener(v ->
                    showDeleteConfirmationDialog(db, terminalCollection, booking, holder)
            );

        } else {
            holder.btnArrived.setVisibility(View.GONE);
        }
    }

    // ===== MARK PICKED UP =====
    private void markPickedUp(FirebaseFirestore db, String terminalCollection, Booking booking, BookingViewHolder holder) {
        db.collection(terminalCollection)
                .document(booking.getBookingId())
                .update("status", "PickedUp")
                .addOnSuccessListener(unused -> {

                    sendPickupSMS(booking);

                    booking.setStatus("PickedUp");
                    holder.txtStatus.setText("Status: PickedUp");
                    holder.btnArrived.setText("Arrived");

                    Toast.makeText(context, "Passenger Picked Up ✅", Toast.LENGTH_SHORT).show();

                    notifyDataSetChanged();
                });
    }

    // ===== MARK ARRIVED =====
    private void markArrived(FirebaseFirestore db, String terminalCollection, Booking booking, BookingViewHolder holder) {

        db.collection(terminalCollection)
                .document(booking.getBookingId())
                .update("status", "Arrived")
                .addOnSuccessListener(unused -> {

                    sendArrivalSMS(booking);

                    booking.setStatus("Arrived");
                    holder.txtStatus.setText("Status: Arrived");
                    holder.btnArrived.setText("Delete");

                    Toast.makeText(context, "Passenger Arrived Safely ✅", Toast.LENGTH_SHORT).show();

                    notifyDataSetChanged();
                });
    }

    // ===== DELETE CONFIRMATION =====
    private void showDeleteConfirmationDialog(FirebaseFirestore db, String terminalCollection, Booking booking, BookingViewHolder holder) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Booking")
                .setMessage("Delete this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    db.collection(terminalCollection)
                            .document(booking.getBookingId()).delete();

                    int pos = holder.getAdapterPosition();
                    bookingList.remove(pos);
                    notifyItemRemoved(pos);

                    Toast.makeText(context, "Booking Deleted ✅", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ===== SMS: PICKED UP =====
    private void sendPickupSMS(Booking booking) {
        String msg = "Good day! Passenger " + safe(booking.getPassengerName()) +
                " has been PICKED UP by the driver.";

        sendSMS(booking.getPhoneNumber(), msg);
        sendSMS(booking.getGuardianNumber(), msg);
    }

    // ===== SMS: ARRIVED =====
    private void sendArrivalSMS(Booking booking) {
        String msg = "Good day! Passenger " + safe(booking.getPassengerName()) +
                " has ARRIVED safely at the destination.";

        sendSMS(booking.getPhoneNumber(), msg);
        sendSMS(booking.getGuardianNumber(), msg);
    }

    // ===== GENERAL SMS SENDER =====
    private void sendSMS(String number, String msg) {
        if (number == null || number.isEmpty()) return;

        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(number, null, msg, null, null);
        } catch (Exception e) {
            Toast.makeText(context, "SMS Failed ❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView txtLocation, txtStatus, txtPassenger, txtPhoneNumber, txtGuardianNumber, txtEta, txtDriver, txtPickup;
        Button btnArrived;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtPassenger = itemView.findViewById(R.id.txtPassenger);
            txtPhoneNumber = itemView.findViewById(R.id.txtPhoneNumber);
            txtGuardianNumber = itemView.findViewById(R.id.txtGuardianNumber);
            txtEta = itemView.findViewById(R.id.txtEta);
            txtDriver = itemView.findViewById(R.id.txtDriver);
            txtPickup = itemView.findViewById(R.id.txtPickup);
            btnArrived = itemView.findViewById(R.id.btnArrived);
        }
    }

    private String safe(String v) {
        return v != null ? v : "";
    }
}
