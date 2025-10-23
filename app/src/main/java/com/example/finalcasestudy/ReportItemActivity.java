package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class ReportItemActivity extends AppCompatActivity {

    private Button reportLostBtn, reportFoundBtn;
    private Spinner spinner;

    boolean spinnerInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_item);

        spinner = findViewById(R.id.spinner);
        reportLostBtn = findViewById(R.id.button5);
        reportFoundBtn = findViewById(R.id.button6);

        reportLostBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, LostReportActivity.class);
            startActivity(intent);
        });

        reportFoundBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, FoundReportActivity.class);
            startActivity(intent);
        });

        setupSpinner();
    }

    private void setupSpinner() {

        String current = "Home";
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

    // Start target Activity only if it's not the current one
    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            // These flags prevent duplicate screens from stacking
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}