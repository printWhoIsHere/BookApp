package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfViewBinding;
import com.example.bookapp.utilities.Constants;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    private ActivityPdfViewBinding binding;
    private String bookId;

    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: " + bookId);

        loadBookDetails();

        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get PDF URL from DB");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: PDF URL: " + pdfUrl);
                        loadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    binding.pdfView.fromBytes(bytes)
                            .swipeHorizontal(true)
                            .onPageChange((page, pageCount) -> {
                                int currentPage = (page + 1);
                                binding.pages.setText(currentPage + "/" + pageCount);
                                Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);
                            })
                            .onError(t -> {
                                Log.d(TAG, "onError: " + t.getMessage());
                                Toast.makeText(PdfViewActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .onPageError((page, t) -> {
                                Log.d(TAG, "onPageError: " + t.getMessage());
                                Toast.makeText(PdfViewActivity.this, "Error on page " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .load();
                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                    binding.progressBar.setVisibility(View.GONE);
                });
    }
}