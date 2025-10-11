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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.menu_items,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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

                // 👇 reset the spinner to prevent being "stuck"
                spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //cafjimenez24@bpsu.edu.ph

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