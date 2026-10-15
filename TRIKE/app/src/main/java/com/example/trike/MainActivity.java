package com.example.trike;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ImageButton userfare, userchecklist, logBtn;
    Button bookn;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();


        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        userfare = findViewById(R.id.ufare);
        userchecklist = findViewById(R.id.userhistory);
        bookn = findViewById(R.id.booknow);
        logBtn = findViewById(R.id.logBtn);


        logBtn.setOnClickListener(v -> showLogoutDialog());


        userfare.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        });


        userchecklist.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), UserHistoryActivity.class);
            startActivity(i);
        });


        bookn.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), BooknowActivity.class);
            startActivity(i);
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
