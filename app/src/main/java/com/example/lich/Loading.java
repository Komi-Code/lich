package com.example.lich;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class Loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        // Chuyển sang MainActivity sau 2 giây
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Loading.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}