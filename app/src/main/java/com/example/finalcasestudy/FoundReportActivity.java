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

        // UI initialization
        uploadImageBtn = findViewById(R.id.button10); // Upload to Cloudinary
        reportFoundBtn = findViewById(R.id.button11); // Save details + go to MatchingResultFound
        imageView = findViewById(R.id.ivItemImage);
        itemNameInput = findViewById(R.id.editTextTextMultiLine);
        descInput = findViewById(R.id.editTextTextMultiLine8);
        finderInput = findViewById(R.id.editTextTextMultiLine9);
        numberInput = findViewById(R.id.editTextTextMultiLine10);
        dateFoundInput = findViewById(R.id.editTextTextMultiLine13);
        locationFoundInput = findViewById(R.id.editTextTextMultiLine12);

        // Initialize Cloudinary
        initConfig();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // ImageView click → select image
        imageView.setOnClickListener(v -> requestPermissions());

        // Button 10 → Upload to Cloudinary only
        uploadImageBtn.setOnClickListener(v -> {
            if (imagePath == null) {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageToCloudinary();
            }
        });

        // Button 11 → Save to Firestore (only if image uploaded + all details complete)
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

            // Save details
            saveItemDetails(itemName, description, finder, number, dateFound, location, uploadedImageUrl);
        });
    }

    // ===============================
    // Initialize Cloudinary
    // ===============================
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

    // ===============================
    // Upload Image to Cloudinary
    // ===============================
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
                        saveImageUrlToFirebase(uploadedImageUrl); // optional
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

    // Optional: Save image URL only
    private void saveImageUrlToFirebase(String imageUrl) {
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", imageUrl);
        imageData.put("timestamp", System.currentTimeMillis());

        db.collection("uploaded_images")
                .add(imageData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(FoundReportActivity.this, "Image URL saved to Firestore.", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(FoundReportActivity.this, "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ===============================
    // Save full item details
    // ===============================
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

                    // Clear inputs
                    itemNameInput.setText("");
                    descInput.setText("");
                    finderInput.setText("");
                    numberInput.setText("");
                    dateFoundInput.setText("");
                    locationFoundInput.setText("");
                    imageView.setImageResource(0);
                    uploadedImageUrl = null;

                    // Move to MatchingResultFound activity
                    Intent intent = new Intent(FoundReportActivity.this, MatchingResultFound.class);
                    intent.putExtra("location", location);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(FoundReportActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // ===============================
    // Image Selection Permissions
    // ===============================
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
