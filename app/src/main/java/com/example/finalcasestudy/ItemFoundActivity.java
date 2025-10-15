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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_found);

        // ✅ Fix for full-screen layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabAddFound = findViewById(R.id.floatingActionButton2);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        spinner = findViewById(R.id.spinner);
        searchBar = findViewById(R.id.editTextText);
        buttonSearch = findViewById(R.id.buttonSearch);

        // ✅ Initialize Firestore
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new ItemFoundAdapter(itemList);
        recyclerView.setAdapter(adapter);

        // 🔥 Load data from Firestore
        loadFoundItems();

        // ➕ FAB to add found item
        fabAddFound.setOnClickListener(v -> {
            startActivity(new Intent(this, FoundReportActivity.class));
        });

        // 🔍 Search button click
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

        // 🧭 Spinner setup
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        String current = "Found Items";
        int index = adapterSpinner.getPosition(current);
        spinner.setSelection(index);

        // ✅ Prevent spinner from blocking search bar clicks
        spinner.setOnTouchListener((v, event) -> {
            spinnerInitialized = true;
            return false;
        });

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

                // Keep spinner highlighting on current
                spinner.post(() -> spinner.setSelection(adapterSpinner.getPosition(current)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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

    private void loadFoundItems() {
        db.collection("reported_items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ItemFoundActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        itemList.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                String name = doc.getString("itemName");
                                String date = doc.getString("dateFound");
                                String imageUrl = doc.getString("imageUrl");

                                itemList.add(new ItemFoundData(name, date, imageUrl));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
