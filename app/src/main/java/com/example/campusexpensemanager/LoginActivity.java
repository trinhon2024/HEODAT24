package com.example.campusexpensemanager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import javax.crypto.SecretKey;
public class LoginActivity extends AppCompatActivity {
    private Button btnLogIn2;
    private TextView tvUser2, tvPassword3, tvSignup;
    private DatabaseHelper dbHelper;
    private SecretKey existingKey;
    String storedUsername = "yourUsername";
    String storedPassword = "yourPassword";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper = new DatabaseHelper(this);
        btnLogIn2 = (Button) findViewById(R.id.btnLogIn2);
        tvUser2 = (TextView) findViewById(R.id.tvUser2);
        tvPassword3 = (TextView) findViewById(R.id.tvPassword3);
        tvSignup = (TextView) findViewById(R.id.tvSignup);
        try {
            // Use the same key from registration (load securely in production)
            // Lấy SecretKey từ SharedPreferences (nếu có)
            existingKey = function.getSecretKey(this);

            if (existingKey == null) {
                // Nếu chưa có SecretKey, tạo mới
                SecretKey newKey = EncryptionUtils.generateKey();
                // Lưu SecretKey vào SharedPreferences
                function.saveSecretKeyIfNotExists(this, newKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        btnLogIn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tvUser2.getText().toString().trim();
                String password = tvPassword3.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String encryptedPassword = EncryptionUtils.encrypt(password, existingKey); // Mã hóa mật khẩu
                        boolean isValidUser = dbHelper.checkUser(username, encryptedPassword);

                        if (isValidUser) {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UserName", username);
                            editor.apply();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error during login", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

}
