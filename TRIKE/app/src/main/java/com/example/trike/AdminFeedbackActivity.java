package com.example.trike;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminFeedbackActivity extends AppCompatActivity {

    private RecyclerView recyclerFeedbacks;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_feedback);

        recyclerFeedbacks = findViewById(R.id.recyclerViewFeedback);
        recyclerFeedbacks.setLayoutManager(new LinearLayoutManager(this));

        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(this, feedbackList);
        recyclerFeedbacks.setAdapter(feedbackAdapter);

        db = FirebaseFirestore.getInstance();

        loadFeedbacks();
    }

    private void loadFeedbacks() {
        db.collection("adminfeedback")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    feedbackList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Feedback feedback = document.toObject(Feedback.class);
                        if (feedback != null) {
                            feedbackList.add(feedback);
                        }
                    }

                    feedbackAdapter.notifyDataSetChanged();

                    if (feedbackList.isEmpty()) {
                        Toast.makeText(this, "No feedbacks found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
