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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
        adapter = new MatchingResultFoundAdapter(this, itemList);
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

        String current = "Found Items";
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
                        startActivity(new Intent(MatchingResultFound.this, ReportItemActivity.class));
                        finish();
                        break;
                    case "Lost Items":
                        startActivity(new Intent(MatchingResultFound.this, ItemLostActivity.class));
                        finish();
                        break;
                    case "Found Items":
                        startActivity(new Intent(MatchingResultFound.this, ItemFoundActivity.class));
                        finish();
                        break;
                    case "Summary":
                        startActivity(new Intent(MatchingResultFound.this, SummariesActivity.class));
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
    }

    // Load Firestore data
    private void loadMatchingResults(String query) {
        db.collection("reported_items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
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

                        // Check if query matches any field
                        if (matchesQuery(query, itemName, description, location, finder, date)) {
                            MatchingResultFoundData item = new MatchingResultFoundData(
                                    doc.getId(), // important: send Firestore ID
                                    itemName,
                                    date,
                                    imageUrl
                            );
                            itemList.add(item);
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

    private boolean matchesQuery(String query, String... fields) {
        String lowerQuery = query.toLowerCase();
        for (String field : fields) {
            if (field != null && field.toLowerCase().contains(lowerQuery)) {
                return true;
            }
        }
        return false;
    }
}
