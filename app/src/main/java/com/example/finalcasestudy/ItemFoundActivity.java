package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ItemFoundActivity extends AppCompatActivity {

    // Declare UI elements
    private FloatingActionButton fabAddFound;
    private RecyclerView recyclerView;
    private Spinner spinner;
    private EditText searchBar;
    private ImageButton buttonSearch;
    private boolean spinnerInitialized;
    private ItemFoundAdapter adapter;
    private List<ItemFoundData> itemList;
    private FirebaseFirestore db;
    private TabLayout tabLayout;

    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_found);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize all UI elements
        fabAddFound = findViewById(R.id.floatingActionButton2);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        spinner = findViewById(R.id.spinner);
        searchBar = findViewById(R.id.editTextText);
        buttonSearch = findViewById(R.id.buttonSearch);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new ItemFoundAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // Load all found items from Firestore when activity starts
        loadFoundItems();

        // Floating action button to add a new found report
        fabAddFound.setOnClickListener(v -> {
            startActivity(new Intent(this, FoundReportActivity.class));
        });

        // Search button logic
        buttonSearch.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                // Send the search query to MatchingResultFound activity
                Intent intent = new Intent(ItemFoundActivity.this, MatchingResultFound.class);
                intent.putExtra("searchQuery", query);
                startActivity(intent);
            } else {
                Toast.makeText(ItemFoundActivity.this, "Please enter something to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handles category filtering via tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                filterByCategory(currentCategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                filterByCategory(currentCategory);
            }
        });

        // Setup navigation spinner
        setupSpinner();
    }

    // Navigation spinner setup
    private void setupSpinner() {
        String current = "Found Items"; // current screen label
        int index = ((ArrayAdapter<CharSequence>) spinner.getAdapter()).getPosition(current);
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
                        Intent logoutIntent = new Intent(ItemFoundActivity.this, MainActivity.class);
                        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }

                // Reset spinner selection back to current page after navigating
                spinner.post(() -> {
                    int currentIndex = ((ArrayAdapter<CharSequence>) spinner.getAdapter()).getPosition(current);
                    spinner.setSelection(currentIndex);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Opens a new activity only if it's not already the current one
    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0); // no animation
        }
    }

    // Load all found items from Firestore and display in RecyclerView
    private void loadFoundItems() {
        db.collection("reported_items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ItemFoundActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    itemList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String documentId = doc.getId();
                            String name = doc.getString("itemName");
                            String date = doc.getString("dateFound");
                            String imageUrl = doc.getString("imageUrl");

                            // Add item data to list
                            itemList.add(new ItemFoundData(documentId, name, date, imageUrl));
                        }
                        // Refresh RecyclerView
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Filtering displayed items by category using the selected tab
    private void filterByCategory(String category) {
        // If "All" is selected, it will show everything
        if (category.equals("All")) {
            loadFoundItems();
            return;
        }

        // Otherwise, filter items based on the selected category
        db.collection("reported_items")
                .whereEqualTo("category", category)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ItemFoundActivity.this, "Error loading category data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    itemList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String documentId = doc.getId();
                            String name = doc.getString("itemName");
                            String date = doc.getString("dateFound");
                            String imageUrl = doc.getString("imageUrl");

                            // Add filtered item data to list
                            itemList.add(new ItemFoundData(documentId, name, date, imageUrl));
                        }
                        // Refresh RecyclerView with filtered results
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
