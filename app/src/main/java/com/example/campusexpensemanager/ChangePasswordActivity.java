package com.example.campusexpensemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import javax.crypto.SecretKey;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword;
    private Button btnChangePasswordConfirm;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnChangePasswordConfirm = findViewById(R.id.btnChangePasswordConfirm);

        dbHelper = new DatabaseHelper(this);

        btnChangePasswordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();

                if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(ChangePasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String username = sharedPreferences.getString("UserName", null);

                    if (username != null) {
                        try {
                            // Lấy SecretKey để mã hóa mật khẩu
                            SecretKey secretKey = function.getSecretKey(ChangePasswordActivity.this);
                            if (secretKey == null) {
                                throw new Exception("Secret key not found.");
                            }

                            // Mã hóa mật khẩu hiện tại để kiểm tra
                            String encryptedCurrentPassword = EncryptionUtils.encrypt(currentPassword, secretKey);

                            boolean isPasswordValid = dbHelper.checkUser(username, encryptedCurrentPassword);
                            if (isPasswordValid) {
                                // Mã hóa mật khẩu mới
                                String encryptedNewPassword = EncryptionUtils.encrypt(newPassword, secretKey);

                                // Cập nhật mật khẩu trong cơ sở dữ liệu
                                boolean isUpdated = dbHelper.updatePassword(username, encryptedNewPassword);

                                if (isUpdated) {
                                    Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "Error updating password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Incorrect current password", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(ChangePasswordActivity.this, "Error during password change", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Error: User not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}