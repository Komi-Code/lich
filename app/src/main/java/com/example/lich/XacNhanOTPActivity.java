package com.example.lich;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lich.Database.TaoDatabase;

public class XacNhanOTPActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText otpEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button confirmButton;
    private TaoDatabase taoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xacnhanotp);

        taoDatabase = new TaoDatabase(this);

        emailEditText = findViewById(R.id.etEmail);
        otpEditText = findViewById(R.id.etOTP);
        newPasswordEditText = findViewById(R.id.etNewPassword);
        confirmPasswordEditText = findViewById(R.id.etConfirmNewPassword);
        confirmButton = findViewById(R.id.btnConfirm);

        String email = getIntent().getStringExtra("email");
        String originalOTP = getIntent().getStringExtra("otp");

        // Điền email
        emailEditText.setText(email);
        emailEditText.setEnabled(false);

        confirmButton.setOnClickListener(v -> {
            String enteredOTP = otpEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (enteredOTP.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui Lòng Điền Đầy Đủ Thông Tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật Khẩu Không Khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra OTP
            if (enteredOTP.equals(originalOTP)) {
                // Cập nhật mật khẩu
                taoDatabase.capNhatMatKhau(email, newPassword);

                Toast.makeText(this, "Đặt Lại Mật khẩu Thành Công", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Mã OTP Không Chính Xác", Toast.LENGTH_SHORT).show();
            }
        });
    }
}