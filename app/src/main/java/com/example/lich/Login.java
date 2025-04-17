package com.example.lich;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lich.Database.TaoDatabase;

public class Login extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;
    private TaoDatabase taoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangnhap);

        taoDatabase = new TaoDatabase(this);

        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        forgotPasswordTextView = findViewById(R.id.tvForgotPassword);
        registerTextView = findViewById(R.id.tvRegister);

        // Xử lý đăng nhập
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (taoDatabase.kiemTraDangNhap(email, password)) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("isLoggedIn", true);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Login.this, "Email Hoặc Mật Khẩu Không Đúng", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgotPassword.class);
            startActivity(intent);
        });

        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        ImageView imageView3 = findViewById(R.id.imageView3);
        imageView3.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}