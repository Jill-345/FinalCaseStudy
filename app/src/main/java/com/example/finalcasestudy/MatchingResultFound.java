package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MatchingResultFound extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinner;
    private boolean spinnerInitialized;
    private FirebaseFirestore db;
    private ItemFoundAdapter adapter;
    private List<ItemFoundData> itemList;

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

        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new ItemFoundAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // 🔎 Get the search keyword from the intent
        String searchQuery = getIntent().getStringExtra("searchQuery");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            loadMatchingResults(searchQuery);
        }

        // 📋 Spinner setup
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        String current = "Home";
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
                        startActivity(new Intent(MatchingResultFound.this, ItemFoundActivity.class));
                        finish();
                        break;
                }
                spinner.post(() -> spinner.setSelection(spinnerAdapter.getPosition(current)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // 🔥 Firestore query to load results based on search, latest first
    private void loadMatchingResults(String query) {
        db.collection("reported_items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("itemName");

                        // 🔍 Match by name (case-insensitive)
                        if (itemName != null && itemName.toLowerCase().contains(query.toLowerCase())) {
                            String date = doc.getString("date");
                            String imageUrl = doc.getString("imageUrl"); // optional if you have images

                            // Create your model object
                            ItemFoundData item = new ItemFoundData(itemName, date, imageUrl);
                            itemList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // You can show a Toast if loading fails
                    // Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
    }
}
