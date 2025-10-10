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

    private EditText emailField, passwordField;
    private Button signInBtn;
    private TextView resendEmailText, signUpText, forgotPasswordText;
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

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.editTextText5);
        passwordField = findViewById(R.id.editTextText6);
        signInBtn = findViewById(R.id.button4);
        resendEmailText = findViewById(R.id.textViewResend);
        forgotPasswordText = findViewById(R.id.textView9);

        signInBtn.setOnClickListener(v -> loginUser());
        resendEmailText.setOnClickListener(v -> resendVerificationEmail());
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());

        forgotPasswordText.setOnClickListener(view ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@bpsu.edu.ph")) {
            Toast.makeText(this, "Please use your BPSU email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, ReportItemActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
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

    private void showForgotPasswordDialog() {
        final EditText resetMail = new EditText(this);
        resetMail.setHint("Enter your BPSU email");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password?")
                .setMessage("Enter your @bpsu.edu.ph email to receive a reset link.")
                .setView(resetMail)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = resetMail.getText().toString().trim();

                    if (!email.endsWith("@bpsu.edu.ph")) {
                        Toast.makeText(this, "Only @bpsu.edu.ph emails allowed.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (email.isEmpty()) {
                        Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }
}
