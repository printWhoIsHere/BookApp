package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.bookapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        splashScreen();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void splashScreen() {
        int splash_display_length = 2000;
        new Handler().postDelayed(() -> {
            checkUser();
        }, splash_display_length);
    }

    private void checkUser() {
        //get current user, of logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        } else {
            //user logged in check user type
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            //get user type
                            String userType = "" + snapshot.child("userType").getValue();
                            //check user type
                            if (userType.equals("user")) {
                                //open user MainActivity
                                startActivity(new Intent(SplashScreen.this, DashboardUserActivity.class));
                                finish();
                            } else if (userType.equals("admin")) {
                                //open admin MainActivity
                                startActivity(new Intent(SplashScreen.this, DashboardAdminActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}