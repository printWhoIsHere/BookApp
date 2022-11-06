package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfUserBinding;
import com.example.bookapp.filters.FilterPdfUser;
import com.example.bookapp.models.ModelPdf;
import com.example.bookapp.utilities.MyApplication;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfUser filter;

    private RowPdfUserBinding binding;

    private static final String TAG = "ADAPTER_PDF_USER_TAG";

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        //get data
        ModelPdf model = pdfArrayList.get(position);
        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();

        //convert time
        String date = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleView.setText(title);
        holder.descriptionView.setText(description);
        holder.dateView.setText(date);

        MyApplication.loadPdfFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.pdfView,
                holder.progressBar
        );
        MyApplication.loadCategory(
                "" + categoryId,
                holder.categoryView
        );
        MyApplication.loadPdfFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.pdfView,
                holder.progressBar
        );
        MyApplication.loadPdfSize(
                "" + pdfUrl,
                "" + title,
                holder.sizeView
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder {

        TextView titleView, descriptionView, sizeView, dateView, categoryView;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleView = binding.titleView;
            descriptionView = binding.descriptionView;
            sizeView = binding.sizeView;
            dateView = binding.dateView;
            categoryView = binding.categoryView;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
