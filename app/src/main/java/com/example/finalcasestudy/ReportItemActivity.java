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

        setContentView(R.layout.activity_report_item); // adjust to your XML filename

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
                // ignore the initial automatic selection when the spinner first loads
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }

                String selected = parent.getItemAtPosition(position).toString();

                switch (selected) {
                    case "Home":
                        openIfNotCurrent(ReportItemActivity.class);
                        break;
                    case "Lost Item":
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // Start target Activity only if it's not the current one
    private void openIfNotCurrent(Class<?> targetActivity) {
        if (getClass() != targetActivity) {
            Intent intent = new Intent(this, targetActivity);
            // Option: avoid stacking duplicate activities — bring existing instance to front
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            // optional: call finish() if you want to remove the current activity from backstack
        }
    }
}