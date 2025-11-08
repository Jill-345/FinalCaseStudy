package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SummariesActivity extends AppCompatActivity {

    // Declare necessary components the UI, and Firebase
    private Spinner spinner;
    private boolean spinnerInitialized;
    private FirebaseFirestore db;
    private TextView tvLostReports, tvFoundFromLost, tvUnfoundItems;
    private TextView tvFoundReports, tvClaimedItems, tvUnclaimedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summaries);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinner = findViewById(R.id.spinner4);

        //  Initialize  and UI
        db = FirebaseFirestore.getInstance();

        tvLostReports = findViewById(R.id.textView26);
        tvFoundFromLost = findViewById(R.id.textView28);
        tvUnfoundItems = findViewById(R.id.textView30);

        tvFoundReports = findViewById(R.id.textView33);
        tvClaimedItems = findViewById(R.id.textView35);
        tvUnclaimedItems = findViewById(R.id.textView37);

        //  Start real-time listeners
        startLostItemListener();
        startFoundItemListener();

        // Spinner navigation setup
        setupSpinner();
    }

    // Handles navigation spinner
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String current = "Summary";
        int index = adapter.getPosition(current);
        spinner.setSelection(index);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }

                // Handle spinner menu selection
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
                        Intent logoutIntent = new Intent(SummariesActivity.this, MainActivity.class);
                        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }

                // Reset selection to current after navigation
                spinner.post(() -> {
                    int currentIndex = adapter.getPosition(current);
                    spinner.setSelection(currentIndex);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Opens target activity only if it’s not the current one
    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    // Monitors the the changes collection and updates the summary
    private void startLostItemListener() {
        db.collection("lost_items").addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Toast.makeText(SummariesActivity.this, "Error loading lost items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (querySnapshot != null) {
                int totalLost = querySnapshot.size();
                int foundCount = 0;
                int unfoundCount = 0;

                // Count how many items are claimed vs unclaimed
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String status = doc.getString("status");
                    if ("Found".equalsIgnoreCase(status)) {
                        foundCount++;
                    } else {
                        unfoundCount++;
                    }
                }

                // Update TextViews with the latest counts
                tvLostReports.setText(String.valueOf(totalLost));
                tvFoundFromLost.setText(String.valueOf(foundCount));
                tvUnfoundItems.setText(String.valueOf(unfoundCount));
            }
        });
    }

    // Real-time listener for Found Items
    private void startFoundItemListener() {
        db.collection("reported_items").addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Toast.makeText(SummariesActivity.this, "Error loading found items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (querySnapshot != null) {
                int totalFound = querySnapshot.size();
                int claimed = 0;
                int unclaimed = 0;

                // Count how many items are claimed vs unclaimed
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String claimStatus = doc.getString("claimStatus");
                    if ("Claimed".equalsIgnoreCase(claimStatus)) {
                        claimed++;
                    } else {
                        unclaimed++;
                    }
                }

                // Update TextViews with the latest counts
                tvFoundReports.setText(String.valueOf(totalFound));
                tvClaimedItems.setText(String.valueOf(claimed));
                tvUnclaimedItems.setText(String.valueOf(unclaimed));
            }
        });
    }
}