package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri pdfUri = null;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private ProgressDialog progressDialog;
    private static final int PDF_PICK_CODE = 1000;
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnAttach.setOnClickListener(v -> pdfPickIntent());
        binding.categoryView.setOnClickListener(v -> categoryPickDialog());
        binding.btnSubmit.setOnClickListener(v -> validateData());

    }

    private String title = "", description = "";

    private void validateData() {
        Log.d(TAG, "validateData: validating data");
        title = binding.titleEdit.getText().toString().trim();
        description = binding.descriptionEdit.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick PDF", Toast.LENGTH_SHORT).show();
        } else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage");
        progressDialog.setMessage("Uploading PDF");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        //path of PDF in firebase storage
        String filePathAndName = "Books/" + timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "onSuccess: PDFuploaded to storage");
                    Log.d(TAG, "onSuccess: getting PDF url");

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadedPdfUrl = "" + uriTask.getResult();

                    uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onFailure: PDF upload failed due to " + e.getMessage());
                    Toast.makeText(PdfAddActivity.this, "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading PDF info to firebase DB");
        progressDialog.setMessage("Uploading PDF info");

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        //DB ref: DB > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: Successfully uploaded");
                    Toast.makeText(PdfAddActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onFailure: Failed to upload to DB due to " + e.getMessage());
                    Toast.makeText(PdfAddActivity.this, "Failed to upload to DB due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        // DB reference to load categories... DB > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String categoryId = "" + ds.child("id").getValue();
                    String categoryTitle = "" + ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    selectedCategoryTitle = categoryTitleArrayList.get(which);
                    selectedCategoryId = categoryIdArrayList.get(which);

                    binding.categoryView.setText(selectedCategoryTitle);
                    Log.d(TAG, "onClick: Selected Category " + selectedCategoryId + " " + selectedCategoryTitle);
                }).show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF Picked");
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult: URI: " + pdfUri);
            }
        } else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}