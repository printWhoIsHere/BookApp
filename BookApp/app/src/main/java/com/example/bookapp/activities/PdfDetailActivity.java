package com.example.bookapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.utilities.MyApplication;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;
    String bookId, bookTitle, bookUrl;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //binding.btnDownloadBook.setVisibility(View.GONE);

        loadBookDetails();

        //increment book view count
        MyApplication.incrementBookViewCount(bookId);

        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnReadBook.setOnClickListener(v -> {
            Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
            intent1.putExtra("bookId", bookId);
            startActivity(intent1);
        });
        /*binding.btnDownloadBook.setOnClickListener(v -> {
            Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
            if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                MyApplication.downloadBook(PdfDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
            } else {
                Log.d(TAG_DOWNLOAD, "onClick: Permission wasn't granted, request permission");
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });*/
    }

    //request permission
    /*private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyApplication.downloadBook(this, "" + bookId, "" + bookTitle, "" + bookUrl);
                } else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied: ");
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }
            });*/

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();

                        //required data id loaded, show download button
                        //binding.btnDownloadBook.setVisibility(View.VISIBLE);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                "" + categoryId,
                                binding.categoryView
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.pdfView,
                                binding.progressBar
                        );
                        MyApplication.loadPdfSize(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.sizeView
                        );

                        binding.titleView.setText(bookTitle);
                        binding.descriptionView.setText(description);
                        binding.viewsView.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsView.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateView.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}