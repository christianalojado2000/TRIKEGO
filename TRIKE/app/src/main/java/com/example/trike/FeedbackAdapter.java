package com.example.trike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private final Context context;
    private final List<Feedback> feedbackList;

    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        holder.txtPassenger.setText("Passenger: " + safe(feedback.getPassengerName()));
        holder.txtDriver.setText("Driver: " + safe(feedback.getDriverName()));
        holder.txtComment.setText("Feedback: " + safe(feedback.getFeedback()));

        float ratingValue = feedback.getRating();
        holder.ratingBar.setRating(ratingValue > 0 ? ratingValue : 0);
        holder.ratingBar.setIsIndicator(true); // read-only display
    }

    @Override
    public int getItemCount() {
        return feedbackList != null ? feedbackList.size() : 0;
    }

    private String safe(String text) {
        return (text == null || text.trim().isEmpty()) ? "N/A" : text;
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView txtPassenger, txtDriver, txtComment;
        RatingBar ratingBar;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPassenger = itemView.findViewById(R.id.txtPassenger);
            txtDriver = itemView.findViewById(R.id.txtDriver);
            txtComment = itemView.findViewById(R.id.txtComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
