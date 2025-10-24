package com.example.finalcasestudy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Declare UI elements and Firebase authentication instance
    private EditText emailField;
    private Button forgotBtn, backLoginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables edge-to-edge layout for better design
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link UI components with XML layout IDs
        emailField = findViewById(R.id.editTextText7);
        forgotBtn = findViewById(R.id.button7);
        backLoginBtn = findViewById(R.id.button8);

        // Set click listener for "Forgot Password" button
        forgotBtn.setOnClickListener(v -> sendResetLink());

        // Set click listener for "Back to Login" button
        // Finishes the activity and returns to the previous screen (Login)
        backLoginBtn.setOnClickListener(v -> finish());
    }

    /**
     * Sends a password reset link to the entered email if valid.
     */
    private void sendResetLink() {
        String email = emailField.getText().toString().trim(); // Get email input and remove extra spaces

        // Validate that the email field is not empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate that the email uses a BPSU domain
        if (!email.endsWith("@bpsu.edu.ph")) {
            Toast.makeText(this, "Only @bpsu.edu.ph emails are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send password reset link using Firebase Authentication
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
