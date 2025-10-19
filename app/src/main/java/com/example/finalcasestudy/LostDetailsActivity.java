package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class LostDetailsActivity extends AppCompatActivity {

    private Spinner spinner;
    private boolean spinnerInitialized;

    private ImageView ivItemImage;
    private TextView tvItemName, tvDescription, tvCategory, tvOwner, tvContact, tvDateLoss, tvLocationLoss;
    private RadioGroup radioGroup;
    private RadioButton radioFound, radioNotFound;

    private FirebaseFirestore db;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lost_details);

        // ✅ Handle window insets (fixed)
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // ✅ Spinner setup
        spinner = findViewById(R.id.spinner8);
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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // ✅ Initialize UI elements
        ivItemImage = findViewById(R.id.ivItemImage);
        tvItemName = findViewById(R.id.textViewItemName);
        tvDescription = findViewById(R.id.textViewDescription);
        tvCategory = findViewById(R.id.textViewCategory);
        tvOwner = findViewById(R.id.textViewOwner);
        tvContact = findViewById(R.id.textViewContact);
        tvDateLoss = findViewById(R.id.textViewDateLoss);
        tvLocationLoss = findViewById(R.id.textViewLocationLoss);
        radioGroup = findViewById(R.id.radioGroup);
        radioFound = findViewById(R.id.radioFound);
        radioNotFound = findViewById(R.id.radioNotFound);

        // ✅ Get document ID from intent
        documentId = getIntent().getStringExtra("documentId");
        if (documentId != null && !documentId.isEmpty()) {
            loadItemDetails(documentId);
        } else {
            Toast.makeText(this, "No document ID received.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // ✅ Update Firestore when radio button changes (fixed lambda issue)
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final String newStatus; // must be final for inner usage
                if (checkedId == R.id.radioFound) {
                    newStatus = "Found";
                } else if (checkedId == R.id.radioNotFound) {
                    newStatus = "Not Found";
                } else {
                    newStatus = "";
                }

                if (!newStatus.isEmpty() && documentId != null) {
                    db.collection("lost_items").document(documentId)
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(LostDetailsActivity.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(LostDetailsActivity.this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 🔹 Load Lost Item Details from Firestore
    private void loadItemDetails(String documentId) {
        db.collection("lost_items").document(documentId)
                .get()
                .addOnSuccessListener(this::displayItemDetails)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading item: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 🔹 Display data to screen
    private void displayItemDetails(DocumentSnapshot doc) {
        if (doc.exists()) {
            tvItemName.setText(doc.getString("itemName"));
            tvDescription.setText(doc.getString("description"));
            tvCategory.setText(doc.getString("category"));
            tvOwner.setText(doc.getString("owner"));
            tvContact.setText(doc.getString("contactNumber"));
            tvDateLoss.setText(doc.getString("dateLost"));
            tvLocationLoss.setText(doc.getString("locationLost"));

            String imageUrl = doc.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(ivItemImage);
            }

            String status = doc.getString("status");
            if ("Found".equalsIgnoreCase(status)) {
                radioFound.setChecked(true);
            } else {
                radioNotFound.setChecked(true);
            }
        }
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
