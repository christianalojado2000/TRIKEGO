package com.example.trike;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText, forgotPasswordText, watchVideo;
    private Button loginButton;
    private ImageButton admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        admin = findViewById(R.id.adminB);
        watchVideo = findViewById(R.id.watchVideoText);


        watchVideo.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, VideoPlayerActivity.class);
            startActivity(i);
        });

        // 🔹 Login button logic
        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (email.isEmpty()) {
                loginEmail.setError("Email cannot be empty");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                loginEmail.setError("Please enter a valid Email");
                return;
            }
            if (pass.isEmpty()) {
                loginPassword.setError("Password cannot be empty");
                return;
            }

            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        if (auth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Please verify your email before logging in.",
                                    Toast.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });


        forgotPasswordText.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(LoginActivity.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });


        admin.setOnClickListener(v -> openAdminLogin());
    }

    private void openAdminLogin() {
        Intent i = new Intent(LoginActivity.this, AdminLogin.class);
        startActivity(i);
    }
}
