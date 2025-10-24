package com.example.finalcasestudy;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Declare UI components for user input and actions
    private EditText emailField, passwordField;
    private Button signInBtn;
    private TextView resendEmailText, forgotPasswordText;

    // Firebase authentication instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link XML components with their respective IDs
        emailField = findViewById(R.id.editTextText5);
        passwordField = findViewById(R.id.editTextText6);
        signInBtn = findViewById(R.id.button4);
        resendEmailText = findViewById(R.id.textViewResend);
        forgotPasswordText = findViewById(R.id.textView9);

        // Set onClick listeners for buttons and text options
        signInBtn.setOnClickListener(v -> loginUser());                    // Log in the user
        resendEmailText.setOnClickListener(v -> resendVerificationEmail()); // Resend email verification
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog()); // Show password reset dialog

        // If you clicked the forgotPasswordText you will be directed to ForgotPasswordActivity
        forgotPasswordText.setOnClickListener(view ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    /**
     * Handles user login using Firebase Authentication.
     * Validates email and password, checks email domain and verification status.
     */
    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Check if fields are empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Restrict login to @bpsu.edu.ph emails
        if (!email.endsWith("@bpsu.edu.ph")) {
            Toast.makeText(this, "Please use your BPSU email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in the user with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Check if the user has verified their email
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to the next activity after successful login
                            Intent intent = new Intent(this, ReportItemActivity.class);
                            startActivity(intent);
                            finish(); // Close the login screen
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Display error if authentication fails
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Resends a verification email to the currently logged-in user.
     */
    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();

        // Ensure user exists and has not yet verified their email
        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email sent again.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Please log in first to resend verification email.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a dialog box allowing the user to reset their password.
     * Sends a password reset email if a valid BPSU email is entered.
     */
    private void showForgotPasswordDialog() {
        // Create an input field for email entry
        final EditText resetMail = new EditText(this);
        resetMail.setHint("Enter your BPSU email");

        // Build and show an AlertDialog for password reset
        new AlertDialog.Builder(this)
                .setTitle("Reset Password?")
                .setMessage("Enter your @bpsu.edu.ph email to receive a reset link.")
                .setView(resetMail)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = resetMail.getText().toString().trim();

                    // Validate email format and emptiness
                    if (!email.endsWith("@bpsu.edu.ph")) {
                        Toast.makeText(this, "Only @bpsu.edu.ph emails allowed.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (email.isEmpty()) {
                        Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Send password reset email using Firebase
                    mAuth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Close dialog if canceled
                .create().show();
    }
}
