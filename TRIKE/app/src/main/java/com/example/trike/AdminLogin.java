package com.example.trike;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLogin extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        db = FirebaseFirestore.getInstance();

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLogin.this, "Please enter email & password", Toast.LENGTH_SHORT).show();
            } else {
                checkAdminLogin(email, password);
            }
        });
    }

    private void checkAdminLogin(String email, String password) {
        db.collection("admins").document(email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();

                        if (doc.exists()) {
                            String dbPass = doc.getString("password");
                            String terminal = doc.getString("terminal");

                            if (dbPass != null && dbPass.equals(password)) {

                                Toast.makeText(AdminLogin.this,
                                        "Login Successful!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(AdminLogin.this,
                                        AdminMenuActivity.class);
                                intent.putExtra("terminal", terminal);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(AdminLogin.this,
                                        "Incorrect password", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(AdminLogin.this,
                                    "Admin not found", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(AdminLogin.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
