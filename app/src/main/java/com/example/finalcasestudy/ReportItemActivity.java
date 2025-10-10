package com.example.finalcasestudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class ReportItemActivity extends AppCompatActivity {

    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_item); // adjust to your XML filename

        spinner = findViewById(R.id.spinner);


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
                String selected = parent.getItemAtPosition(position).toString();

                switch (selected) {
                    case "Home":
                        startActivity(new Intent(ReportItemActivity.this, ReportItemActivity.class));
                        break;
                    case "Lost Items":
                        startActivity(new Intent(ReportItemActivity.this, ItemLostActivity.class));
                        break;
                    case "Found Items":
                        startActivity(new Intent(ReportItemActivity.this, ItemFoundActivity.class));
                        break;
                    case "Summary":
                        startActivity(new Intent(ReportItemActivity.this, SummariesActivity.class));
                        break;
                    case "Logout":
                        finish();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
