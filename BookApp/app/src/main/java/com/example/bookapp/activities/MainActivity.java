package com.example.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //view binding
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //handle btnSignIn click
        binding.btnSignIn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignInActivity.class)));

        //handle btnSkip click
        binding.btnSkip.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DashboardUserActivity.class)));
    }
}