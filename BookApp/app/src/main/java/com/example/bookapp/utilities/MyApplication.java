package com.example.bookapp.utilities;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //created a static method to convert timestamp to proper date format
    public static final String formatTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        //format timestamp to dd/MM/yyyy
        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting" + bookTitle);
        progressDialog.show();

        Log.d(TAG, "deleteBook:  Deleting from storage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Deleted from storage");
                    Log.d(TAG, "onSuccess: Now deleting info from db");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                    reference.child(bookId)
                            .removeValue()
                            .addOnSuccessListener(unused1 -> {
                                Log.d(TAG, "onSuccess: Deleted from db too");
                                progressDialog.dismiss();
                                Toast.makeText(context, "Book Deleted Successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "onFailure: Failed to delete from db due to" + e.getMessage());
                                progressDialog.dismiss();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeView) {
        String TAG = "PDF_SIZE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(storageMetadata -> {
                    double bytes = storageMetadata.getSizeBytes();
                    Log.d(TAG, "onSuccess: " + pdfTitle + " " + bytes);

                    //convert
                    double kb = bytes / 1024;
                    double mb = kb / 1024;

                    if (mb >= 1) {
                        sizeView.setText(String.format("%.2f", mb) + " MB");
                    } else if (kb >= 1) {
                        sizeView.setText(String.format("%.2f", kb) + " KB");
                    } else {
                        sizeView.setText(String.format("%.2f", bytes) + " bytes");
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getMessage()));
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        String TAG = "PDF_LOAD_SINGLE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    Log.d(TAG, "onSuccess: " + pdfTitle + " successfully got the file");

                    //set to PDFView
                    pdfView.fromBytes(bytes)
                            .pages(0) //show only 1st page
                            .spacing(0)
                            .swipeHorizontal(false)
                            .enableSwipe(false)
                            .onError(t -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onError: " + t.getMessage());
                            })
                            .onPageError((page, t) -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onPageError: " + t.getMessage());
                            })
                            .onLoad(nbPages -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "loadComplete: pdf loaded");
                            })
                            .load();

                })
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: failed getting file from url due to " + e.getMessage()));
    }

    public static void loadCategory(String categoryId, TextView categoryView) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = "" + snapshot.child("category").getValue();
                        categoryView.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCount(String bookId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get view count
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }
                        long newViewsCount = Long.parseLong(viewsCount) + 1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    /*public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {
        Log.d(TAG_DOWNLOAD, "downloadBook: download book");
        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading " + nameWithExtension);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(bookUrl);
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Download");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving book");
                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG_DOWNLOAD, "onSuccess: Failed to download due to " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(context, "Failed to download due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdir();

            String filePath = downloadFolder.getPath() + "/" + nameWithExtension;
            FileOutputStream outputStream = new FileOutputStream(filePath);
            outputStream.write(bytes);
            outputStream.close();

            Toast.makeText(context, "Saved to download folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to download folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);
        } catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to download folder due to " + e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: incrementing book download count");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: downloads count " + downloadsCount);

                        if (downloadsCount.equals("") || downloadsCount.equals("null")) {
                            downloadsCount = "0";
                        }

                        //convert to long and inc
                        long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: new download count " + newDownloadsCount);

                        //setup data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(unused -> Log.d(TAG_DOWNLOAD, "onSuccess: downloads count updated"))
                                .addOnFailureListener(e -> Log.d(TAG_DOWNLOAD, "onFailure: failed to update downloads count due to " + e.getMessage()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
*/
}
