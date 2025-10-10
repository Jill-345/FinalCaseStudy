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
            boolean firstSelection = true; // To ignore the initial trigger

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Prevent the first auto-trigger when the spinner loads
                if (firstSelection) {
                    firstSelection = false;
                    return;
                }

                String selected = parent.getItemAtPosition(position).toString();

                switch (selected) {
                    case "Home":
                        startActivity(new Intent(ReportItemActivity.this, HomeActivity.class));
                        break;

                    case "Report Item":
                        // Do nothing, we’re already here
                        break;

                    case "Found Items":
                        startActivity(new Intent(ReportItemActivity.this, FoundItemsActivity.class));
                        break;

                    case "Profile":
                        startActivity(new Intent(ReportItemActivity.this, ProfileActivity.class));
                        break;

                    case "Logout":
                        finish(); // or handle logout logic
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing
            }
        });