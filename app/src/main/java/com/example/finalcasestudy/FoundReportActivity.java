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
import androidx.annotation.NonNull;
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
    private String uploadedImageUrl = null;

    private Button uploadImageBtn, reportFoundBtn;
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

        // UI Initialization
        uploadImageBtn = findViewById(R.id.button10);
        reportFoundBtn = findViewById(R.id.button11);
        imageView = findViewById(R.id.ivItemImage);
        itemNameInput = findViewById(R.id.editTextTextMultiLine);
        descInput = findViewById(R.id.editTextTextMultiLine8);
        finderInput = findViewById(R.id.editTextTextMultiLine9);
        numberInput = findViewById(R.id.editTextTextMultiLine10);
        dateFoundInput = findViewById(R.id.editTextTextMultiLine13);
        locationFoundInput = findViewById(R.id.editTextTextMultiLine12);

        initConfig(); // Initialize Cloudinary
        db = FirebaseFirestore.getInstance();

        // ImageView click → request permission or open gallery
        imageView.setOnClickListener(v -> requestPermissions());

        // Upload image to Cloudinary
        uploadImageBtn.setOnClickListener(v -> {
            if (imagePath == null) {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageToCloudinary();
            }
        });

        // Save item details + go to MatchingResultFound
        reportFoundBtn.setOnClickListener(v -> {
            if (uploadedImageUrl == null) {
                Toast.makeText(this, "Please upload the image first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String itemName = itemNameInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String finder = finderInput.getText().toString().trim();
            String number = numberInput.getText().toString().trim();
            String dateFound = dateFoundInput.getText().toString().trim();
            String location = locationFoundInput.getText().toString().trim();

            if (itemName.isEmpty() || description.isEmpty() || finder.isEmpty()
                    || number.isEmpty() || dateFound.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveItemDetails(itemName, description, finder, number, dateFound, location, uploadedImageUrl);
        });
    }

    // Initialize Cloudinary
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

    // Upload Image to Cloudinary
    private void uploadImageToCloudinary() {
        Toast.makeText(this, "Uploading image to Cloudinary...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imagePath)
                .option("folder", "Found Items Report")
                .option("public_id", "image_" + System.currentTimeMillis())
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        uploadedImageUrl = resultData.get("secure_url").toString();
                        Toast.makeText(FoundReportActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
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

    // Save Full Item Details to Firestore
    private void saveItemDetails(String itemName, String description, String finder, String number,
                                 String dateFound, String location, String imageUrl) {

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemName", itemName);
        itemData.put("description", description);
        itemData.put("finder", finder);
        itemData.put("contactNumber", number);
        itemData.put("dateFound", dateFound);
        itemData.put("location", location);
        itemData.put("imageUrl", imageUrl);
        itemData.put("timestamp", System.currentTimeMillis());

        db.collection("reported_items")
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FoundReportActivity.this, "Item reported successfully!", Toast.LENGTH_SHORT).show();

                    // Clear form
                    itemNameInput.setText("");
                    descInput.setText("");
                    finderInput.setText("");
                    numberInput.setText("");
                    dateFoundInput.setText("");
                    locationFoundInput.setText("");
                    imageView.setImageResource(0);
                    uploadedImageUrl = null;

                    // Go to MatchingResultFound
                    Intent intent = new Intent(FoundReportActivity.this, MatchingResultFound.class);
                    intent.putExtra("location", location);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(FoundReportActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Request Permission to Pick Image
    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, IMAGE_REQ);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQ);
            }
        }
    }

    // ✅ Handle Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission denied to access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ Open Gallery to Pick Image
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
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
