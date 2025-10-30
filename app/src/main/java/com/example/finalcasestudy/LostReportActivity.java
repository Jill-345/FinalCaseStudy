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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class LostReportActivity extends AppCompatActivity {

    private static final int IMAGE_REQ = 1;
    private static final int CAMERA_REQ = 2;

    private Uri imagePath;
    private String uploadedImageUrl = null;

    private Button uploadImageBtn, reportLostBtn;
    private ImageView imageView;
    private EditText itemNameInput2, descInput2, ownerInput2, numberInput2, dateLostInput, locationLostInput;

    private Spinner spinner, categorySpinner, campusSpinner;
    private boolean spinnerInitialized;

    private Calendar calendar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lost_report);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Initialize UI
        spinner = findViewById(R.id.spinner2);
        categorySpinner = findViewById(R.id.spinner5);
        campusSpinner = findViewById(R.id.spinner9);
        uploadImageBtn = findViewById(R.id.button13);
        reportLostBtn = findViewById(R.id.button9);
        imageView = findViewById(R.id.ivItemImage2);
        itemNameInput2 = findViewById(R.id.editTextTextMultiLine2);
        descInput2 = findViewById(R.id.editTextTextMultiLine3);
        ownerInput2 = findViewById(R.id.editTextTextMultiLine4); // owner, not finder
        numberInput2 = findViewById(R.id.editTextContactNumber);
        dateLostInput = findViewById(R.id.editTextDateFound);
        locationLostInput = findViewById(R.id.editTextTextMultiLine7);

        setupDatePicker();

        initConfig();
        db = FirebaseFirestore.getInstance();

        // 🔹 Image actions
        imageView.setOnClickListener(v -> showImageSourceDialog());
        uploadImageBtn.setOnClickListener(v -> {
            if (imagePath == null) {
                Toast.makeText(this, "Please select or take an image first.", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageToCloudinary();
            }
        });

        // 🔹 Report lost item
        reportLostBtn.setOnClickListener(v -> {
            if (uploadedImageUrl == null) {
                Toast.makeText(this, "Please upload the image first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String itemName = itemNameInput2.getText().toString().trim();
            String description = descInput2.getText().toString().trim();
            String category = (categorySpinner.getSelectedItem() != null)
                    ? categorySpinner.getSelectedItem().toString().trim()
                    : "";
            String owner = ownerInput2.getText().toString().trim();
            String number = numberInput2.getText().toString().trim();
            String dateLost = dateLostInput.getText().toString().trim();
            String location = locationLostInput.getText().toString().trim();
            String campus = (campusSpinner.getSelectedItem() != null)
                    ? campusSpinner.getSelectedItem().toString().trim()
                    : "";

            if (itemName.isEmpty() || description.isEmpty() || owner.isEmpty()
                    || number.isEmpty() || dateLost.isEmpty() || location.isEmpty() || campus.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveItemDetails(itemName, description, category, owner, number, dateLost, location, campus, uploadedImageUrl);
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // optional: do something when category changes
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.category_items,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(adapter);

        // 🔹 Spinner navigation
        setupSpinner();

    }
    private void setupSpinner() {

        String current = "Select page";
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void openIfNotCurrent(Class<?> targetActivity) {
        if (!getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    // 🔹 Cloudinary config
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

    // 🔹 Upload to Cloudinary (Lost Items Report folder)
    private void uploadImageToCloudinary() {
        Toast.makeText(this, "Uploading image to Cloudinary...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imagePath)
                .option("folder", "Lost Items Report")
                .option("public_id", "lost_image_" + System.currentTimeMillis())
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        uploadedImageUrl = resultData.get("secure_url").toString();
                        Toast.makeText(LostReportActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(LostReportActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    // 🔹 Date picker
    private void setupDatePicker() {
        calendar = Calendar.getInstance();
        dateLostInput.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    LostReportActivity.this,
                    (view, y, m, d) -> {
                        calendar.set(y, m, d);
                        updateDateField();
                    }, year, month, day);
            dialog.show();
        });
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        dateLostInput.setText(sdf.format(calendar.getTime()));
    }

    // 🔹 Save to Firestore
    private void saveItemDetails(String itemName, String description, String category, String owner, String number,
                                 String dateLost, String location, String campus, String imageUrl) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemName", itemName);
        itemData.put("description", description);
        itemData.put("category", category);
        itemData.put("owner", owner);
        itemData.put("contactNumber", number);
        itemData.put("dateLost", dateLost);
        itemData.put("location", location);
        itemData.put("campus", campus); // ✅ Added campus field
        itemData.put("imageUrl", imageUrl);
        itemData.put("timestamp", System.currentTimeMillis());

        db.collection("lost_items")
                .add(itemData)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Lost item reported successfully!", Toast.LENGTH_SHORT).show();

                    // Reset form
                    itemNameInput2.setText("");
                    descInput2.setText("");
                    ownerInput2.setText("");
                    numberInput2.setText("");
                    dateLostInput.setText("");
                    locationLostInput.setText("");
                    imageView.setImageResource(0);
                    uploadedImageUrl = null;

                    // Redirect
                    startActivity(new Intent(this, ItemLostActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 🔹 Image picker / camera
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) requestCameraPermission();
                    else requestGalleryPermission();
                }).show();
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) selectImageFromGallery();
            else ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, IMAGE_REQ);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) selectImageFromGallery();
            else ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_REQ);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) openCamera();
        else ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_REQ);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_REQ && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromGallery();
        } else if (requestCode == CAMERA_REQ && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImageFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, IMAGE_REQ);
    }

    private void openCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) startActivityForResult(i, CAMERA_REQ);
        else Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQ && data != null && data.getData() != null) {
                imagePath = data.getData();
                Picasso.get().load(imagePath).into(imageView);
            } else if (requestCode == CAMERA_REQ && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imagePath = getImageUri(bitmap);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                getContentResolver(), bitmap, "Captured_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }
}
