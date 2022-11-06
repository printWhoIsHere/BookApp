package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding;
    private FirebaseAuth firebaseAuth;

    //ArrayList to store category
    private ArrayList<ModelCategory> categoryArrayList;
    //adapter
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();

        //EditText change listener, search
        binding.categorySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type each letter
                try {
                    adapterCategory.getFilter().filter(s);
                } catch (Exception e) {}
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //handle click, profile
        binding.btnProfile.setOnClickListener(view -> startActivity(new Intent(DashboardAdminActivity.this, ProfileActivity.class)));

        //handle click, logout
        binding.btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });

        //handle click, start category add screen
        binding.btnAddCategory.setOnClickListener(v -> startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class)));

        //handle click, start pdf add screen
        binding.addPDF.setOnClickListener(v -> startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class)));

    }

    private void loadCategories() {
        //init ArrayList
        categoryArrayList = new ArrayList<>();
        //get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear ArrayList before adding data into it
                categoryArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //get data
                    ModelCategory model = dataSnapshot.getValue(ModelCategory.class);

                    //add to ArrayList
                    categoryArrayList.add(model);
                }
                //setup adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                //set adapter to recyclerview
                binding.categoriesList.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //not logged in, goto main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            //logged out
            String email = firebaseUser.getEmail();
            //set in TextView of layout_header
            binding.subTitleTv.setText(email);
        }
    }
}