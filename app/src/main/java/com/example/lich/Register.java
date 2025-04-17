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

public class Register extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText nameEditText;
    private EditText phoneEditText;
    private Button registerButton;
    private TaoDatabase taoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        taoDatabase = new TaoDatabase(this);

        emailEditText = findViewById(R.id.registerUsername);
        passwordEditText = findViewById(R.id.registerPassword);
        confirmPasswordEditText = findViewById(R.id.registerConfirmPassword);
        nameEditText = findViewById(R.id.registerName);
        phoneEditText = findViewById(R.id.registerPhone);
        registerButton = findViewById(R.id.registerSubmitButton);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(Register.this, "Vui Lòng Điền Đầy Đủ Thông Tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(Register.this, "Mật Khẩu Không Khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (taoDatabase.kiemTraEmailTonTai(email)) {
                Toast.makeText(Register.this, "Email Đã Được Đăng Ký", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thêm tài khoản vào SQLite
            long result = taoDatabase.themTaiKhoan(name, password, email, phone);

            if (result != -1) {
                Toast.makeText(Register.this, "Đăng Ký Thành Công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Register.this, "Đăng Ký Thất Bại", Toast.LENGTH_SHORT).show();
            }
        });



        TextView loginTextView = findViewById(R.id.lg);
        if (loginTextView != null) {
            loginTextView.setOnClickListener(v -> {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            });
        }

        ImageView imageView3 = findViewById(R.id.imageView3);
        imageView3.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}