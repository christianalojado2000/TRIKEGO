package com.example.trike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class UserdriverfareActivity extends AppCompatActivity {

    ImageButton userfare, userchecklist;
    Button bookn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userdriverfare);

        userfare = findViewById(R.id.ufare);
        userchecklist = findViewById(R.id.userhistory);
        bookn = findViewById(R.id.booknow);

        userfare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V){

            }
        });

        userchecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                Intent i = new Intent(getApplicationContext(), UserHistoryActivity.class);
                startActivity(i);
            }
        });

        bookn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                Intent i = new Intent(getApplicationContext(), BooknowActivity.class);
                startActivity(i);
            }
        });
    }
}
