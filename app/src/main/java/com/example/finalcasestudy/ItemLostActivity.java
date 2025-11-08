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

public class ItemLostActivity extends AppCompatActivity {

    // Declare UI elements
    private FloatingActionButton fabAddLost;
    private RecyclerView recyclerView;
    private Spinner spinner;
    private EditText searchBar;
    private ImageButton buttonSearch;
    private TabLayout tabLayout;

    private boolean spinnerInitialized;
    private FirebaseFirestore db;
    private List<ItemLostData> itemList;
    private ItemLostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_lost);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize all UI elements
        fabAddLost = findViewById(R.id.floatingActionButton2);
        recyclerView = findViewById(R.id.recyclerView);
        spinner = findViewById(R.id.spinner);
        searchBar = findViewById(R.id.editTextText);
        buttonSearch = findViewById(R.id.buttonSearch);
        tabLayout = findViewById(R.id.tabLayout);

        // Display lost items in 2-column grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize Firestore and data list
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new ItemLostAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // Load all lost items from Firestore when activity starts
        loadLostItems();

        // Floating button to report a new lost item
        fabAddLost.setOnClickListener(v -> {
            startActivity(new Intent(this, LostReportActivity.class));
        });

        // Search button action
        buttonSearch.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                // Pass the search query to MatchingResultLost activity
                Intent intent = new Intent(ItemLostActivity.this, MatchingResultLost.class);
                intent.putExtra("searchQuery", query);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter something to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup navigation spinner
        setupSpinner();

        // Tab Filtering
        setupTabs();
    }

    private void setupTabs() {
        // Listen for tab selection and filter by category
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                // Show all items if “All” tab is selected, otherwise filter by category
                if (selectedCategory.equals("All")) {
                    loadLostItems();
                } else {
                    filterByCategory(selectedCategory);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Load all lost items from Firestore and display in RecyclerView
    private void loadLostItems() {
        db.collection("lost_items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ItemLostActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    itemList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String documentId = doc.getId();
                            String name = doc.getString("itemName");
                            String date = doc.getString("dateLost");
                            String imageUrl = doc.getString("imageUrl");

                            // Add each Firestore document as an ItemLostData object
                            itemList.add(new ItemLostData(documentId, name, date, imageUrl));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Filters Firestore data by the selected category tab
    private void filterByCategory(String category) {
        db.collection("lost_items")
                .whereEqualTo("category", category)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ItemLostActivity.this, "Error filtering data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    itemList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String documentId = doc.getId();
                            String name = doc.getString("itemName");
                            String date = doc.getString("dateLost");
                            String imageUrl = doc.getString("imageUrl");

                            itemList.add(new ItemLostData(documentId, name, date, imageUrl));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Navigation spinner setup
    private void setupSpinner() {

        String current = "Lost Items";
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
                        Intent logoutIntent = new Intent(ItemLostActivity.this, MainActivity.class);
                        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }

                // Reset selection to current after navigation
                spinner.post(() -> {
                    int currentIndex = ((ArrayAdapter<CharSequence>) spinner.getAdapter()).getPosition(current);
                    spinner.setSelection(currentIndex);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Opens a new activity only if it's not already the current one
    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}
