package com.example.appfirst;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TestMLKit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mlkit);
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, TestMLKit.class);
        return intent;
    }
}