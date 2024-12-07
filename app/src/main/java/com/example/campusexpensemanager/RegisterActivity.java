package com.example.campusexpensemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import javax.crypto.SecretKey;

public class RegisterActivity extends AppCompatActivity {

    Button btnRegister;
    TextView tvUserName, tvPassword, tvRePw, tvLogin, tvPolicy;
    EditText editTextTextEmailAddress, editTextPhone;
    CheckBox cbAgree;
    private SecretKey existingKey;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvPassword = (TextView) findViewById(R.id.tvPassword);
        tvRePw = (TextView) findViewById(R.id.tvRePw);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        cbAgree = (CheckBox) findViewById(R.id.cbAgree);
        tvPolicy = (TextView) findViewById(R.id.tvPolicy);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        editTextTextEmailAddress = (EditText) findViewById(R.id.editTextTextEmailAddress);

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
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tvUserName.getText().toString().trim();
                String password = tvPassword.getText().toString().trim();
                String rePassword = tvRePw.getText().toString().trim();
                String email = editTextTextEmailAddress.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();

                try {

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    } else {
                        String encryptedPassword = EncryptionUtils.encrypt(password, existingKey);
                        boolean success = dbHelper.registerUser(username, encryptedPassword, email, phone);

                        if (success) {
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        tvPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String privacyPolicy =
                        "1. Introduction\n" +
                                "Our expense management app (\"HeoDat\") is committed to protecting user privacy. This policy explains how we collect, use, store, and protect personal information while users interact with the app on their devices. As this is an offline app, all personal data will be stored and processed directly on the user’s device.\n\n" +
                                "2. Personal Data Collected\n" +
                                "The app may request the following information from users:\n" +
                                "- Personal Information: Such as name and email address (if provided).\n" +
                                "- Financial Information: Including balance, expenses, income, and spending details.\n" +
                                "- Login Data: Username and password if a password protection feature is used.\n\n" +
                                "3. Purpose of Using Personal Data\n" +
                                "Personal data is used solely for the following purposes:\n" +
                                "- Personal Account Management: Financial data is used to provide users with personal finance management features.\n" +
                                "- Account Protection: Username and password (if applicable) are stored on the user’s device to prevent unauthorized access.\n\n" +
                                "4. Data Storage and Security\n" +
                                "- Local Storage: All personal and financial data will be stored on the user’s device and will not be shared with any third parties.\n" +
                                "- Security: We use standard security measures to protect stored data, including encryption and password authentication. However, we are not responsible if the user’s device is lost or compromised.\n\n" +
                                "5. User Rights\n" +
                                "Users have the following rights regarding their personal data:\n" +
                                "- Access and Edit: Users may access and modify their personal information within the app.\n" +
                                "- Data Deletion: Users can delete data within the app. Deleted data is permanently removed and cannot be recovered.\n\n" +
                                "6. Policy Changes\n" +
                                "This privacy policy may be updated from time to time. Any changes will be communicated to users through the app.\n";

                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Data Privacy Policy")
                        .setMessage(privacyPolicy)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });    }
}
