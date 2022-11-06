package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;
    private String booId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryIdArrayList, categoryTitleArrayList;
    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        booId = getIntent().getStringExtra("booId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.categoryView.setOnClickListener(v -> categoryDialog());
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnSubmit.setOnClickListener(v -> validateData());
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(booId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String title = "" + snapshot.child("title").getValue();
                        //set to views
                        binding.titleEdit.setText(title);
                        binding.descriptionEdit.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = "" + snapshot.child("category").getValue();
                                        //set tp category text view
                                        binding.categoryView.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private String title = "", description = "";

    private void validateData() {
        //ged data
        title = binding.titleEdit.getText().toString().trim();
        description = binding.descriptionEdit.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show();
        } else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating PDF info to db");
        progressDialog.setMessage("Updating book info");
        progressDialog.show();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);

        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(booId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Book updated");
                    progressDialog.dismiss();
                    Toast.makeText(PdfEditActivity.this, "Book info updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: Failed to update due to" + e.getMessage());
                    Toast.makeText(PdfEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String selectedCategoryId = "", selectedCategoryTitle = "";

    private void categoryDialog() {
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    selectedCategoryId = categoryIdArrayList.get(which);
                    selectedCategoryTitle = categoryTitleArrayList.get(which);

                    binding.categoryView.setText(selectedCategoryTitle);
                })
                .show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories");
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String category = "" + ds.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: " + id);
                    Log.d(TAG, "onDataChange: Category: " + category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}