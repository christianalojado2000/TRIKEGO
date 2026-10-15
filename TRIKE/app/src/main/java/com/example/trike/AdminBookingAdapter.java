package com.example.trike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;

    public AdminBookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.txtLocation.setText("Location: " + booking.getLocation());
        holder.txtStatus.setText("Status: " + booking.getStatus());
        holder.txtUserId.setText("Passenger: " + booking.getPassengerName());
        holder.txtPhoneNumber.setText("Phone: " + booking.getPhoneNumber());
        holder.txtGuardianNumber.setText("Guardian: " + booking.getGuardianNumber());
        holder.txtEta.setText("ETA: " + booking.getEta());
        holder.txtDriver.setText("Driver: " +
                (booking.getDriverName() != null && !booking.getDriverName().isEmpty()
                        ? booking.getDriverName() : "None"));
        holder.txtPickup.setText("Pickup: " + booking.getPickupAddress());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String location = booking.getLocation();
        String terminal = location.contains("-")
                ? location.split("-")[0].trim().toLowerCase()
                : location.trim().toLowerCase();
        String collectionName = "bookings_" + terminal;


        if (booking.getDriverName() != null && !booking.getDriverName().isEmpty()) {
            holder.btnAccept.setEnabled(false);
            holder.btnAccept.setAlpha(0.5f);
        } else {
            holder.btnAccept.setEnabled(true);
            holder.btnAccept.setAlpha(1f);
        }


        holder.btnAccept.setOnClickListener(v -> {
            db.collection(collectionName)
                    .document(booking.getBookingId())
                    .update("status", "Accepted")
                    .addOnSuccessListener(aVoid -> {
                        booking.setStatus("Accepted");
                        notifyItemChanged(holder.getAdapterPosition());
                        Toast.makeText(context, "Booking accepted!", Toast.LENGTH_SHORT).show();

                        holder.btnAccept.setEnabled(false);
                        holder.btnAccept.setAlpha(0.5f);

                        // ⭐ SEND SMS to passenger & guardian
                        sendBookingSMS(context, booking);

                        Intent intent = new Intent(context, DriverListActivity.class);
                        intent.putExtra("bookingId", booking.getBookingId());
                        intent.putExtra("terminal", terminal);
                        context.startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to accept: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });


        if (!"Arrived".equalsIgnoreCase(booking.getStatus())) {
            holder.btnArrived.setEnabled(false);
            holder.btnArrived.setAlpha(0.5f);
        } else {
            holder.btnArrived.setEnabled(true);
            holder.btnArrived.setAlpha(1f);
        }

        holder.btnArrived.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Arrival")
                    .setMessage("Are you sure the passenger has arrived?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection(collectionName)
                                .document(booking.getBookingId())
                                .update("status", "Completed")
                                .addOnSuccessListener(aVoid -> {
                                    booking.setStatus("Completed");
                                    notifyItemChanged(holder.getAdapterPosition());
                                    Toast.makeText(context, "Trip Completed!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Booking")
                    .setMessage("Are you sure you want to delete this booking?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection(collectionName)
                                .document(booking.getBookingId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    int pos = holder.getAdapterPosition();
                                    if (pos >= 0 && pos < bookingList.size()) {
                                        bookingList.remove(pos);
                                        notifyItemRemoved(pos);
                                    } else {
                                        bookingList.removeIf(b -> b.getBookingId().equals(booking.getBookingId()));
                                        notifyDataSetChanged();
                                    }
                                    Toast.makeText(context, "Booking deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // ⭐⭐⭐ SEND SMS FUNCTION ⭐⭐⭐
    private void sendBookingSMS(Context ctx, Booking booking) {

        String passengerPhone = booking.getPhoneNumber();
        String guardianPhone = booking.getGuardianNumber();

        String message = "Your driver is on the way. Expect arrival in 10 minutes.";

        try {
            SmsManager smsManager = SmsManager.getDefault();

            if (passengerPhone != null && !passengerPhone.isEmpty()) {
                smsManager.sendTextMessage(passengerPhone, null, message, null, null);
            }

            if (guardianPhone != null && !guardianPhone.isEmpty()) {
                smsManager.sendTextMessage(guardianPhone, null, message, null, null);
            }

            Toast.makeText(ctx, "SMS notification sent!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(ctx, "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtLocation, txtStatus, txtUserId, txtPhoneNumber, txtGuardianNumber,
                txtEta, txtDriver, txtPickup;
        Button btnArrived, btnDelete, btnAccept;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtUserId = itemView.findViewById(R.id.txtUserId);
            txtPhoneNumber = itemView.findViewById(R.id.txtPhoneNumber);
            txtGuardianNumber = itemView.findViewById(R.id.txtGuardianNumber);
            txtEta = itemView.findViewById(R.id.txtEta);
            txtDriver = itemView.findViewById(R.id.txtDriver);
            txtPickup = itemView.findViewById(R.id.txtPickup);

            btnArrived = itemView.findViewById(R.id.btnArrived);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}
