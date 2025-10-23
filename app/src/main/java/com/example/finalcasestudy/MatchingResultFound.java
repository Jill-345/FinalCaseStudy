package com.example.finalcasestudy;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MatchingResultFound extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinner;
    private boolean spinnerInitialized = false;
    private FirebaseFirestore db;
    private MatchingResultFoundAdapter adapter;
    private List<MatchingResultFoundData> itemList;
    private TabLayout tabLayout;
    private String selectedCampus = "All Campuses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_matching_result_found);

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
        adapter = new MatchingResultFoundAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // Get search query from previous activity
        String searchQuery = getIntent().getStringExtra("searchQuery");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            loadMatchingResults(searchQuery);
        } else {
            Toast.makeText(this, "No search term provided.", Toast.LENGTH_SHORT).show();
        }

        // ✅ Campus Spinner setup (no intents, just filter)
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.campus, // your XML already has android:entries="@array/campus"
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }

                selectedCampus = parent.getItemAtPosition(position).toString();
                String searchQuery = getIntent().getStringExtra("searchQuery");

                if (searchQuery != null && !searchQuery.isEmpty()) {
                    if (selectedCampus.equalsIgnoreCase("All Campuses")) {
                        loadMatchingResults(searchQuery);
                    } else {
                        filterByCampusAndSearch(selectedCampus, searchQuery);
                    }
                }

                Toast.makeText(MatchingResultFound.this, "Selected: " + selectedCampus, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupTabs();
    }

    // ✅ Tab setup (still filters by category)
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                String searchQuery = getIntent().getStringExtra("searchQuery");

                if (searchQuery == null || searchQuery.isEmpty()) {
                    Toast.makeText(MatchingResultFound.this, "No search term provided.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedCategory.equals("All")) {
                    if (selectedCampus.equals("All Campuses")) {
                        loadMatchingResults(searchQuery);
                    } else {
                        filterByCampusAndSearch(selectedCampus, searchQuery);
                    }
                } else {
                    if (selectedCampus.equals("All Campuses")) {
                        filterByCategoryAndSearch(selectedCategory, searchQuery);
                    } else {
                        filterByCategoryCampusAndSearch(selectedCategory, selectedCampus, searchQuery);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // ✅ Load Firestore data
    private void loadMatchingResults(String query) {
        db.collection("reported_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        String finder = doc.getString("finder");
                        String category = doc.getString("category");
                        String campus = doc.getString("campus");
                        String date = doc.getString("dateFound");
                        String imageUrl = doc.getString("imageUrl");

                        if (matchesQuery(query, itemName, description, location, finder, category, campus, date)) {
                            itemList.add(new MatchingResultFoundData(
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

    // ✅ Filter by Category + Search
    private void filterByCategoryAndSearch(String category, String query) {
        db.collection("reported_items")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        String finder = doc.getString("finder");
                        String campus = doc.getString("campus");
                        String date = doc.getString("dateFound");
                        String imageUrl = doc.getString("imageUrl");

                        if (matchesQuery(query, itemName, description, location, finder, campus, date)) {
                            itemList.add(new MatchingResultFoundData(
                                    doc.getId(),
                                    itemName,
                                    date,
                                    imageUrl
                            ));
                        }
                    }

                    if (itemList.isEmpty()) {
                        Toast.makeText(this, "No items found in this category.", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error filtering data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ✅ Filter by Campus + Search
    private void filterByCampusAndSearch(String campus, String query) {
        db.collection("reported_items")
                .whereEqualTo("campus", campus)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        String finder = doc.getString("finder");
                        String category = doc.getString("category");
                        String date = doc.getString("dateFound");
                        String imageUrl = doc.getString("imageUrl");

                        if (matchesQuery(query, itemName, description, location, finder, category, date)) {
                            itemList.add(new MatchingResultFoundData(
                                    doc.getId(),
                                    itemName,
                                    date,
                                    imageUrl
                            ));
                        }
                    }

                    if (itemList.isEmpty()) {
                        Toast.makeText(this, "No results found for " + campus, Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error filtering by campus: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ✅ Filter by Category + Campus + Search
    private void filterByCategoryCampusAndSearch(String category, String campus, String query) {
        db.collection("reported_items")
                .whereEqualTo("category", category)
                .whereEqualTo("campus", campus)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");
                        String description = doc.getString("description");
                        String location = doc.getString("location");
                        String finder = doc.getString("finder");
                        String date = doc.getString("dateFound");
                        String imageUrl = doc.getString("imageUrl");

                        if (matchesQuery(query, itemName, description, location, finder, date)) {
                            itemList.add(new MatchingResultFoundData(
                                    doc.getId(),
                                    itemName,
                                    date,
                                    imageUrl
                            ));
                        }
                    }

                    if (itemList.isEmpty()) {
                        Toast.makeText(this, "No results found for this category and campus.", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error filtering data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ✅ Text matching helper
    private boolean matchesQuery(String query, String... fields) {
        String lowerQuery = query.trim().toLowerCase();
        for (String field : fields) {
            if (field != null) {
                String cleanField = field.trim().toLowerCase();
                if (!cleanField.equals("null") && cleanField.contains(lowerQuery)) {
                    return true;
                }
            }
        }
        return false;
    }
}
