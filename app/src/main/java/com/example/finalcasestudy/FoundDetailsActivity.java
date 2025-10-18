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
import android.widget.TableLayout;
import android.widget.TableRow;
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

import java.util.HashMap;
import java.util.Map;

public class FoundDetailsActivity extends AppCompatActivity {

    private Spinner spinner;
    private boolean spinnerInitialized;

    private ImageView ivItemImage;
    private TextView tvItemName, tvDescription, tvCategory, tvOwner, tvContact, tvDateLoss, tvLocationLoss;
    private FirebaseFirestore db;

    // 🔹 Editable claim fields
    private EditText etClaimant, etStatus, etSubmissionDate;
    private RadioGroup radioGroup;
    private RadioButton radioClaimed, radioUnclaimed;
    private Button btnSaveClaim;

    private String documentId; // store document ID for updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_found_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Firebase
        db = FirebaseFirestore.getInstance();

        // 🔹 Spinner setup
        spinner = findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition("Home"));

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
                        finish();
                        break;
                }

                spinner.post(() -> spinner.setSelection(adapter.getPosition("Home")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🔹 Initialize UI fields
        ivItemImage = findViewById(R.id.ivItemImage);
        tvItemName = findViewById(R.id.tvItemName);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvOwner = findViewById(R.id.tvOwner);
        tvContact = findViewById(R.id.tvContact);
        tvDateLoss = findViewById(R.id.tvDateLoss);
        tvLocationLoss = findViewById(R.id.tvLocationLoss);

        etClaimant = findViewById(R.id.etClaimant);
        etStatus = findViewById(R.id.etStatus);
        etSubmissionDate = findViewById(R.id.etSubmissionDate);
        radioGroup = findViewById(R.id.radioGroup);
        radioClaimed = findViewById(R.id.radioClaimed);
        radioUnclaimed = findViewById(R.id.radioUnclaimed);
        btnSaveClaim = findViewById(R.id.button12);

        // 🔹 Get Document ID from Intent
        documentId = getIntent().getStringExtra("documentId");
        if (documentId != null && !documentId.isEmpty()) {
            loadItemDetails(documentId);
        } else {
            Toast.makeText(this, "No document ID received.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 🔹 Save Claim button logic
        btnSaveClaim.setOnClickListener(v -> saveClaimToFirestore());
    }

    private void loadItemDetails(String documentId) {
        db.collection("reported_items").document(documentId)
                .get()
                .addOnSuccessListener(this::displayItemDetails)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading item: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void displayItemDetails(DocumentSnapshot doc) {
        if (doc.exists()) {
            tvItemName.setText(doc.getString("itemName"));
            tvDescription.setText(doc.getString("description"));
            tvCategory.setText(doc.getString("category"));
            tvOwner.setText(doc.getString("finder"));
            tvContact.setText(doc.getString("contactNumber"));
            tvDateLoss.setText(doc.getString("dateFound"));
            tvLocationLoss.setText(doc.getString("location"));

            String imageUrl = doc.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(ivItemImage);
            }

            // Load existing claim data if present
            etClaimant.setText(doc.getString("claimant"));
            etStatus.setText(doc.getString("claimStatus"));
            etSubmissionDate.setText(doc.getString("submissionDate"));
        }
    }

    // 🟢 Save Claim to Firestore
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
                        Toast.makeText(this, "Claim information saved successfully!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving claim: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}
