package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoundDetailsActivity extends AppCompatActivity {

    private Spinner spinner;
    private boolean spinnerInitialized;

    private ImageView ivItemImage;
    private TextView tvItemName, tvDescription, tvCategory, tvOwner, tvContact, tvDateFound, tvLocation, tvCampus;
    private FirebaseFirestore db;

    private EditText etClaimant, etStatus, etSubmissionDate;
    private RadioGroup radioGroup;
    private RadioButton radioClaimed, radioUnclaimed;
    private Button btnSaveClaim;

    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_found_details);

        // ✅ Handle window insets
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // ✅ Spinner for Menu
        spinner = findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapterMenu = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );
        adapterMenu.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterMenu);
        spinner.setSelection(adapterMenu.getPosition("Select page"));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }

                String selected = parent.getItemAtPosition(position).toString();
                switch (selected) {
                    case "Home":
                        openIfNotCurrent(ReportItemActivity.class);
                        break;
                    case "Lost Items":
                        openIfNotCurrent(ItemLostActivity.class);
                        break;
                    case "Found Items":
                        openIfNotCurrent(ItemFoundActivity.class);
                        break;
                    case "Summary":
                        openIfNotCurrent(SummariesActivity.class);
                        break;
                    case "Logout":
                        Intent logoutIntent = new Intent(FoundDetailsActivity.this, MainActivity.class);
                        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }

                spinner.post(() -> spinner.setSelection(adapterMenu.getPosition("Home")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ✅ Initialize UI elements
        ivItemImage = findViewById(R.id.ivItemImage);
        tvItemName = findViewById(R.id.tvItemName);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvOwner = findViewById(R.id.tvOwner);
        tvContact = findViewById(R.id.tvContact);
        tvDateFound = findViewById(R.id.tvDateLoss);
        tvLocation = findViewById(R.id.tvLocationLoss);
        tvCampus = findViewById(R.id.tvCampus);

        etClaimant = findViewById(R.id.etClaimant);
        etStatus = findViewById(R.id.etStatus);
        etSubmissionDate = findViewById(R.id.etSubmissionDate);
        radioGroup = findViewById(R.id.radioGroup);
        radioClaimed = findViewById(R.id.radioClaimed);
        radioUnclaimed = findViewById(R.id.radioUnclaimed);
        btnSaveClaim = findViewById(R.id.button12);

        // ✅ Get document ID from intent
        documentId = getIntent().getStringExtra("documentId");
        if (documentId != null && !documentId.isEmpty()) {
            loadItemDetails(documentId);
        } else {
            Toast.makeText(this, "No document ID received.", Toast.LENGTH_SHORT).show();
            finish();
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newStatus;

            if (checkedId == R.id.radioClaimed) {
                newStatus = "Claimed";
                // Automatically set submission date to today
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                etSubmissionDate.setText(todayDate);
            } else if (checkedId == R.id.radioUnclaimed) {
                newStatus = "Unclaimed";
                etClaimant.setText(""); // clear claimant field
                etSubmissionDate.setText(""); // clear date
            } else {
                newStatus = "";
            }

            etStatus.setText(newStatus);


        // ✅ Auto-update Firestore claim status & submission date
            if (!newStatus.isEmpty() && documentId != null) {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("claimStatus", newStatus);
                updateData.put("submissionDate", etSubmissionDate.getText().toString());

                db.collection("reported_items").document(documentId)
                        .update(updateData)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(FoundDetailsActivity.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(FoundDetailsActivity.this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // ✅ Save claim info manually
        btnSaveClaim.setOnClickListener(v -> saveClaimToFirestore());
    }

    // 🔹 Load data from Firestore
    private void loadItemDetails(String documentId) {
        db.collection("reported_items").document(documentId)
                .get()
                .addOnSuccessListener(this::displayItemDetails)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading item: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 🔹 Display item details
    private void displayItemDetails(DocumentSnapshot doc) {
        if (doc.exists()) {
            tvItemName.setText(doc.getString("itemName"));
            tvDescription.setText(doc.getString("description"));
            tvCategory.setText(doc.getString("category"));
            tvOwner.setText(doc.getString("finder"));
            tvContact.setText(doc.getString("contactNumber"));
            tvDateFound.setText(doc.getString("dateFound"));
            tvLocation.setText(doc.getString("location"));
            tvCampus.setText(doc.getString("campus"));

            String imageUrl = doc.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(ivItemImage);
            }

            // Load claim details
            etClaimant.setText(doc.getString("claimant"));
            etStatus.setText(doc.getString("claimStatus"));
            etSubmissionDate.setText(doc.getString("submissionDate"));

            String claimStatus = doc.getString("claimStatus");
            if (claimStatus != null) {
                if (claimStatus.equalsIgnoreCase("Claimed")) {
                    radioClaimed.setChecked(true);
                } else if (claimStatus.equalsIgnoreCase("Unclaimed")) {
                    radioUnclaimed.setChecked(true);
                } else {
                    radioGroup.clearCheck();
                }
            }
        }
    }

    // 🔹 Save claim info to Firestore
    private void saveClaimToFirestore() {
        if (documentId == null) return;

        String claimantName = etClaimant.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String submissionDate = etSubmissionDate.getText().toString().trim();

        if (claimantName.isEmpty() || status.isEmpty() || submissionDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("claimant", claimantName);
        claimData.put("claimStatus", status);
        claimData.put("submissionDate", submissionDate);

        db.collection("reported_items").document(documentId)
                .update(claimData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Claim information saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving claim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 🔹 Spinner Navigation Helper
    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}
