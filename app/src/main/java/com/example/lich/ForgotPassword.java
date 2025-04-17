package com.example.lich;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lich.Database.TaoDatabase;

import java.util.Random;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPassword extends AppCompatActivity {
    private EditText emailEditText;
    private Button resetPasswordButton;
    private TaoDatabase taoDatabase;
    private String generatedOTP;
    private CountDownTimer otpTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quenmatkhau);

        taoDatabase = new TaoDatabase(this);

        emailEditText = findViewById(R.id.etEmail);
        resetPasswordButton = findViewById(R.id.btnResetPassword);

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // Kiểm tra email có tồn tại không
            if (taoDatabase.kiemTraEmailTonTai(email)) {
                generatedOTP = generateOTP();
                sendOTPEmail(email, generatedOTP);
                // Bắt đầu đếm ngược 10 phút
                startOTPTimer();

                // Chuyển sang màn hình xác nhận OTP
                Intent intent = new Intent(ForgotPassword.this, XacNhanOTPActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("otp", generatedOTP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ForgotPassword.this, "Email Chưa Được Đăng Ký", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView imageView4 = findViewById(R.id.imageView4);
        imageView4.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassword.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    // Tạo mã OTP ngẫu nhiên
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Đếm ngược thời gian OTP
    private void startOTPTimer() {
        otpTimer = new CountDownTimer(10 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                String timeLeft = String.format("%02d:%02d", minutes, seconds);
                runOnUiThread(() -> {
                    resetPasswordButton.setText("Gửi Lại Mã OTP Sau " + timeLeft);
                    resetPasswordButton.setEnabled(false);
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(() -> {
                    generatedOTP = null; // Vô hiệu hóa OTP
                    resetPasswordButton.setText("Gửi Mã OTP");
                    resetPasswordButton.setEnabled(true);
                });
            }
        }.start();
    }

    private void sendOTPEmail(String toEmail, String otp) {
        new Thread(() -> {
            final String username = "2224801030043@student.tdmu.edu.vn";
            final String password = "gblk kzcc fpld kpmf";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(toEmail));
                message.setSubject("Mã OTP Đặt Lại Mật Khẩu");
                message.setText("Mã OTP Của Bạn Là: " + otp +
                        "\nMã Này Có Hiệu Lực Trong 10 Phút.");

                Transport.send(message);

                runOnUiThread(() -> {
                    Toast.makeText(ForgotPassword.this, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show();
                });
            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPassword.this, "Gửi mã OTP thất bại", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) {
            otpTimer.cancel();
        }
    }
}