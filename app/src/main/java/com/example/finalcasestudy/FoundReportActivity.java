package com.example.finalcasestudy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FoundReportActivity extends AppCompatActivity {

    private static final int IMAGE_REQ = 1;
    private Uri imagePath;
    private Button reportFoundBtn;
    private ImageView imageView;
    private EditText itemNameInput, descInput, finderInput, numberInput, dateFoundInput, locationFoundInput;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_found_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reportFoundBtn = findViewById(R.id.button11);
        imageView = findViewById(R.id.ivItemImage);
        itemNameInput = findViewById(R.id.editTextTextMultiLine);
        descInput = findViewById(R.id.editTextTextMultiLine8);
        finderInput = findViewById(R.id.editTextTextMultiLine9);
        numberInput = findViewById(R.id.editTextTextMultiLine10);
        dateFoundInput = findViewById(R.id.editTextTextMultiLine13);
        locationFoundInput = findViewById(R.id.editTextTextMultiLine12);

        // Initialize Cloudinary
        initConfig();

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        imageView.setOnClickListener(v -> requestPermissions());

        reportFoundBtn.setOnClickListener(v -> {
            if (imagePath != null) {
                uploadImage();
            } else {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dylvri8g8");
        config.put("api_key", "317749255636612");
        config.put("api_secret", "QuFDsxxrABtIs8WT3TpEWp5fOUU");
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    private void uploadImage() {
        MediaManager.get().upload(imagePath)
                .option("folder", "cloudinarysample") // 👈 Folder name in Cloudinary
                .option("public_id", "image_" + System.currentTimeMillis())
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(FoundReportActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String uploadedUrl = resultData.get("secure_url").toString();
                        Toast.makeText(FoundReportActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                        saveImageToFirebase(uploadedUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(FoundReportActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch();
    }

    private void saveImageToFirebase(String imageUrl) {
        String itemName = itemNameInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String location = locationFoundInput.getText().toString().trim();

        if (itemName.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemName", itemName);
        itemData.put("description", description);
        itemData.put("location", location);
        itemData.put("imageUrl", imageUrl);
        itemData.put("timestamp", System.currentTimeMillis());

        db.collection("reported_items")
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FoundReportActivity.this, "Item saved to Firestore!", Toast.LENGTH_SHORT).show();
                    itemNameInput.setText("");
                    descInput.setText("");
                    locationFoundInput.setText("");
                    imageView.setImageResource(0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FoundReportActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        IMAGE_REQ
                );
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        IMAGE_REQ
                );
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQ && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imagePath = data.getData();
            Picasso.get().load(imagePath).into(imageView);
        }
    }
}
