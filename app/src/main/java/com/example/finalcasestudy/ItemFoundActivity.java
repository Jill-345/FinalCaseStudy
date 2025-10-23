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
    private String currentCategory = "All"; // Track selected category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_found);

        // ✅ Full screen fix
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔧 Initialize Views
        fabAddFound = findViewById(R.id.floatingActionButton2);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        spinner = findViewById(R.id.spinner);
        searchBar = findViewById(R.id.editTextText);
        buttonSearch = findViewById(R.id.buttonSearch);
        tabLayout = findViewById(R.id.tabLayout);

        // ✅ Firestore setup
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new ItemFoundAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // 🔥 Load all found items by default
        loadFoundItems();

        // ➕ FAB Add new found item
        fabAddFound.setOnClickListener(v -> {
            startActivity(new Intent(this, FoundReportActivity.class));
        });

        // 🔍 Search button
        buttonSearch.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                Intent intent = new Intent(ItemFoundActivity.this, MatchingResultFound.class);
                intent.putExtra("searchQuery", query);
                startActivity(intent);
            } else {
                Toast.makeText(ItemFoundActivity.this, "Please enter something to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // 🏷️ TabLayout Category Filter
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

        setupSpinner();
    }

    private void setupSpinner() {

        String current = "Found Items";
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


    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    // 🧠 Load all found items
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

                            itemList.add(new ItemFoundData(documentId, name, date, imageUrl));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // 🏷️ Filter by category from tabs
    private void filterByCategory(String category) {
        if (category.equals("All")) {
            loadFoundItems();
            return;
        }

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

                            itemList.add(new ItemFoundData(documentId, name, date, imageUrl));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
