package com.example.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    //view binding
    private ActivitySignUpBinding binding;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle click, go back
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        //handle click, begin SignUp
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }

            private String name = "", email = "", password = "";

            private void validateData() {
                //get data
                name = binding.inputName.getText().toString().trim();
                email = binding.inputEmail.getText().toString().trim();
                password = binding.inputPassword.getText().toString().trim();
                String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();

                //validate data
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(SignUpActivity.this, "Enter name", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignUpActivity.this, "Invalid email pattern", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignUpActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Confirm password", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                } else {
                    createUserAccount();
                }
            }

            private void createUserAccount() {
                //show progress
                progressDialog.setMessage("Creating account");
                progressDialog.show();

                //create user in firebase auth
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            //account creation success, now add in firebase realtime database
                            updateUserInfo();
                        })
                        .addOnFailureListener(e -> {
                            //account creating failed
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            private void updateUserInfo() {
                progressDialog.setMessage("Saving user info");

                //timestamp
                long timestamp = System.currentTimeMillis();

                //get current user uid, since user is registered so we cat get now
                String uid = firebaseAuth.getUid();

                //setup data to add in DB
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("uid", uid);
                hashMap.put("email", email);
                hashMap.put("name", name);
                hashMap.put("profileImage", "");
                hashMap.put("userType", "user");
                hashMap.put("timestamp", "timestamp");

                //set data to DB
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(uid)
                        .setValue(hashMap)
                        .addOnSuccessListener(unused -> {
                            //data added to DB
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        //since user account is created so start MainActivity
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                        })
                        .addOnFailureListener(e -> {
                            //data failed adding to DB
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}