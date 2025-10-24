package com.example.finalcasestudy;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    // Declare input fields and button
    private EditText nameField, emailField, passwordField, confirmField;
    private Button createAccountBtn;

    // Firebase authentication and database references
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication and Database
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Link XML components (EditTexts and Button)
        nameField = findViewById(R.id.editTextText);
        emailField = findViewById(R.id.editTextText2);
        passwordField = findViewById(R.id.editTextText3);
        confirmField = findViewById(R.id.editTextText4);
        createAccountBtn = findViewById(R.id.button3);

        // Set listener for the "Create Account" button
        createAccountBtn.setOnClickListener(v -> registerUser());
    }

    // Handles user registration logic including validation and Firebase Authentication.
    private void registerUser() {
        // Retrieve user input and remove extra spaces
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirm = confirmField.getText().toString().trim();

        // Check for empty fields
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Restrict signup to school emails only (@bpsu.edu.ph)
        if (!email.endsWith("@bpsu.edu.ph")) {
            Toast.makeText(this, "Please use your @bpsu.edu.ph email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password and confirmation match
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to create a new user account in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Account successfully created
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Save additional user information in the Realtime Database
                            usersRef.child(user.getUid()).setValue(new User(name, email, "student"));

                            // Send verification email to the user
                            user.sendEmailVerification();

                            Toast.makeText(this, "Registered! Verify your email.", Toast.LENGTH_LONG).show();

                            // Redirect to LoginActivity after successful registration
                            startActivity(new Intent(this, LoginActivity.class));
                            finish(); // Close SignupActivity
                        }
                    } else {
                        // If registration fails, show the error message
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Helper model class to represent a User in the Firebase Database.
     * It includes basic user information: name, email, and role.
     */
    public static class User {
        public String name, email, role;

        // Default constructor required for Firebase
        public User() { }

        // Parameterized constructor for creating a new user entry
        public User(String name, String email, String role) {
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }
}
