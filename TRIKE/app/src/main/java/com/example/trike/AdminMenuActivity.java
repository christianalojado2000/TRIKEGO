package com.example.trike;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminMenuActivity extends AppCompatActivity {

    ImageButton trikeButton, fareButton, bookinghistoryButton, feedbacksButton, logBtn;
    TextView terminalLabel;
    String terminal, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        terminal = getIntent().getStringExtra("terminal");
        email = getIntent().getStringExtra("email");


        trikeButton = findViewById(R.id.trikeb);
        fareButton = findViewById(R.id.fare);
        bookinghistoryButton = findViewById(R.id.bookinghistory);
        feedbacksButton = findViewById(R.id.feedbacks);
        logBtn = findViewById(R.id.logBtn);
        terminalLabel = findViewById(R.id.terminal_name);


        if (terminalLabel != null && terminal != null) {
            terminalLabel.setText("Logged in as: " + terminal);
        }


        trikeButton.setOnClickListener(v -> {
            Intent i = new Intent(this, DriverActivity.class);
            i.putExtra("terminal", terminal);
            startActivity(i);
        });


        fareButton.setOnClickListener(v ->
                Toast.makeText(this, "Open Fare Management", Toast.LENGTH_SHORT).show()
        );


        bookinghistoryButton.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminBookList.class);
            i.putExtra("terminal", terminal);
            startActivity(i);
        });


        feedbacksButton.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminFeedbackActivity.class);
            i.putExtra("terminal", terminal);
            startActivity(i);
        });


        logBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, AdminLogin.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}
