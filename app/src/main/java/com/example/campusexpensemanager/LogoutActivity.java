package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting); // Layout của bạn

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi hàm đăng xuất
                logoutUser();
            }
        });
    }

    // Hàm xử lý đăng xuất
    private void logoutUser() {
        // Khởi tạo DBHelper và xóa thông tin người dùng
        DatabaseHelper dbHelper = new DatabaseHelper(LogoutActivity.this);
        dbHelper.deleteUser();  // Xóa dữ liệu người dùng khỏi cơ sở dữ liệu

        // Chuyển hướng người dùng về màn hình đăng nhập
        Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Kết thúc Activity hiện tại
    }
}
