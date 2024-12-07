package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class SettingsActivity extends AppCompatActivity {
    Button btnLogout, btnChangePassword, btnFeedback;
    BottomNavigationView bottom_navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        btnFeedback = (Button) findViewById(R.id. btnFeedback);
        bottom_navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottom_navigation.setSelectedItemId(R.id.nav_home);

        bottom_navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_settings) {
                    return true;
                } else if (id == R.id.nav_statistics) {
                    startActivity(new Intent(SettingsActivity.this, OverviewActivity.class));
                    return true;
                } else if (id == R.id.nav_add) {
                    startActivity(new Intent(SettingsActivity.this, AddExpenseActivity.class));
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                    return true;
                }
                return false;
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (SettingsActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Heodat@gmail.com.vn"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Campus Expense Manager");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please share your feedback here...");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingsActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi hàm đăng xuất
                logoutUser();
            }
            private void logoutUser() {
                // Khởi tạo DBHelper và xóa thông tin người dùng
                DatabaseHelper dbHelper = new DatabaseHelper(SettingsActivity.this);
                dbHelper.deleteUser();  // Xóa dữ liệu người dùng khỏi cơ sở dữ liệu

                // Chuyển hướng người dùng về màn hình đăng nhập
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Kết thúc Activity hiện tại
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottom_navigation.setSelectedItemId(R.id.nav_settings);
    }
}
