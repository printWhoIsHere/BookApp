package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.BooksUserFragment;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    private ActivityDashboardUserBinding binding;
    private FirebaseAuth firebaseAuth;

    //to show in tabs
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //handle click, profile
        binding.btnProfile.setOnClickListener(view -> startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class)));

        //handle click, logout
        binding.btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });
        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        categoryArrayList = new ArrayList<>();

        //load categories from DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //clear before adding to list
                categoryArrayList.clear();

                //load categories
                ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
                //ModelCategory modelMostDownloaded = new ModelCategory("02", "Most Downloaded", "", 1);
                //add models to list
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                //categoryArrayList.add(modelMostDownloaded);
                //add data to view pager adapter
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelAll.getId(),
                        "" + modelAll.getCategory(),
                        "" + modelAll.getUid()
                ), modelAll.getCategory());
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelMostViewed.getId(),
                        "" + modelMostViewed.getCategory(),
                        "" + modelMostViewed.getUid()
                ), modelMostViewed.getCategory());
                /*viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelMostDownloaded.getId(),
                        "" + modelMostDownloaded.getCategory(),
                        "" + modelMostDownloaded.getUid()
                ), modelMostDownloaded.getCategory());*/

                //refresh list
                viewPagerAdapter.notifyDataSetChanged();
                //now load from DB
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add data to list
                    categoryArrayList.add(model);
                    //add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                                    "" + model.getId(),
                                    "" + model.getCategory(),
                                    "" + model.getUid()),
                            model.getCategory());
                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //set adapter to ViewPager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<BooksUserFragment> fragmentsList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }

        private void addFragment(BooksUserFragment fragment, String title) {
            //add fragment passed as parameter in fragmentsList/fragmentTitleList
            fragmentsList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
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