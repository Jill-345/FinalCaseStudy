package com.example.finalcasestudy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoundReportActivity extends AppCompatActivity {

    private static final int IMAGE_REQ = 1;
    private static final int CAMERA_REQ = 2;

    private Uri imagePath;
    private String uploadedImageUrl = null;

    private Button uploadImageBtn, reportFoundBtn;
    private ImageView imageView;
    private EditText itemNameInput, descInput, finderInput, numberInput, dateFoundInput, locationFoundInput;

    private Calendar calendar;

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
        numberInput = findViewById(R.id.editTextContactNumber);
        dateFoundInput = findViewById(R.id.editTextDateFound);
        locationFoundInput = findViewById(R.id.editTextTextMultiLine12);

        setupDatePicker();

        initConfig(); // Initialize Cloudinary
        db = FirebaseFirestore.getInstance();

        // ImageView click → choose photo source
        imageView.setOnClickListener(v -> showImageSourceDialog());

        // Upload image to Cloudinary
        uploadImageBtn.setOnClickListener(v -> {
            if (imagePath == null) {
                Toast.makeText(this, "Please select or take an image first.", Toast.LENGTH_SHORT).show();
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



    private void setupDatePicker() {
        calendar = Calendar.getInstance();

        // Allow typing AND clicking
        dateFoundInput.setFocusable(true);
        dateFoundInput.setFocusableInTouchMode(true);
        dateFoundInput.setClickable(true);

        // When clicked, open the date picker
        dateFoundInput.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FoundReportActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year1);
                        calendar.set(Calendar.MONTH, month1);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }


    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        dateFoundInput.setText(sdf.format(calendar.getTime()));
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

                    // Go to ItemFoundActivity
                    Intent intent = new Intent(FoundReportActivity.this, ItemFoundActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(FoundReportActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Dialog to choose camera or gallery
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        requestCameraPermission();
                    } else {
                        requestGalleryPermission();
                    }
                })
                .show();
    }

    // Request gallery permission
    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, IMAGE_REQ);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQ);
            }
        }
    }

    // Request camera permission
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQ);
        }
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied to access gallery.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission denied to use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open gallery
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQ);
    }

    // Open camera
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQ);
        } else {
            Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQ && data != null && data.getData() != null) {
                imagePath = data.getData();
                Picasso.get().load(imagePath).into(imageView);
            } else if (requestCode == CAMERA_REQ && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // Convert bitmap to URI
                Uri tempUri = getImageUri(imageBitmap);
                imagePath = tempUri;

                imageView.setImageBitmap(imageBitmap);
            }
        }
    }

    // Convert bitmap to Uri
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                getContentResolver(), bitmap, "CapturedImage_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }
}
