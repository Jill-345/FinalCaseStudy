package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MatchingResultLost extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinner;
    private boolean spinnerInitialized = false;
    private FirebaseFirestore db;
    private MatchingResultLostAdapter adapter;
    private List<MatchingResultLostData> itemList;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_matching_result_lost);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        spinner = findViewById(R.id.spinner);
        tabLayout = findViewById(R.id.tabLayout);

        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new MatchingResultLostAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // Get search query from previous activity
        String searchQuery = getIntent().getStringExtra("searchQuery");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            loadMatchingResults(searchQuery);
        } else {
            Toast.makeText(this, "No search term provided.", Toast.LENGTH_SHORT).show();
        }

        // Spinner setup
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        String current = "Lost Items";
        int index = spinnerAdapter.getPosition(current);
        spinner.setSelection(index);

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
                        startActivity(new Intent(MatchingResultLost.this, ReportItemActivity.class));
                        finish();
                        break;
                    case "Lost Items":
                        startActivity(new Intent(MatchingResultLost.this, ItemLostActivity.class));
                        finish();
                        break;
                    case "Found Items":
                        startActivity(new Intent(MatchingResultLost.this, ItemFoundActivity.class));
                        finish();
                        break;
                    case "Summary":
                        startActivity(new Intent(MatchingResultLost.this, SummariesActivity.class));
                        finish();
                        break;
                    case "Logout":
                        finish();
                        break;
                }

                spinner.post(() -> spinner.setSelection(spinnerAdapter.getPosition(current)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupTabs();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                String searchQuery = getIntent().getStringExtra("searchQuery");

                if (searchQuery == null || searchQuery.isEmpty()) {
                    Toast.makeText(MatchingResultLost.this, "No search term provided.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedCategory.equals("All")) {
                    loadMatchingResults(searchQuery);
                } else {
                    filterByCategoryAndSearch(selectedCategory, searchQuery);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Load Firestore data that matches search query
    private void loadMatchingResults(String query) {
        db.collection("lost_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        String owner = doc.getString("owner");
                        String category = doc.getString("category");
                        String date = doc.getString("dateLost");
                        String imageUrl = doc.getString("imageUrl");

                        // Match multiple fields for more accurate results
                        if (matchesQuery(query, itemName, description, location, owner, category, date)) {
                            itemList.add(new MatchingResultLostData(
                                    doc.getId(),
                                    itemName,
                                    date,
                                    imageUrl
                            ));
                        }
                    }

                    if (itemList.isEmpty()) {
                        Toast.makeText(this, "No matching results found.", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Filter by both category and search term
    private void filterByCategoryAndSearch(String category, String query) {
        db.collection("lost_items")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        String owner = doc.getString("owner");
                        String date = doc.getString("dateLost");
                        String imageUrl = doc.getString("imageUrl");

                        if (matchesQuery(query, itemName, description, location, owner, date)) {
                            itemList.add(new MatchingResultLostData(
                                    doc.getId(),
                                    itemName,
                                    date,
                                    imageUrl
                            ));
                        }
                    }

                    if (itemList.isEmpty()) {
                        Toast.makeText(this, "No items found in this category for your search.", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error filtering data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean matchesQuery(String query, String... fields) {
        String lowerQuery = query.trim().toLowerCase();
        for (String field : fields) {
            if (field != null && !field.equalsIgnoreCase("null")) {
                if (field.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        return false;
    }
}
