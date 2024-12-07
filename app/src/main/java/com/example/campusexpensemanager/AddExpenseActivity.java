package com.example.campusexpensemanager;

import android.content.Intent;
import android.content.DialogInterface;
// or
import android.app.AlertDialog; // For the default AlertDialog
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.Model.objExpences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText editTextDate, editTextAmount, editTextDescription;
    private Spinner spinnerCategory;
    private Button buttonSaveExpense,buttonGoback;
    private DatabaseHelper dbHelper;
    int idExpences;
    BottomNavigationView bottom_navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        dbHelper = new DatabaseHelper(this);

        editTextDate = findViewById(R.id.editTextDate);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense);
        buttonGoback = findViewById(R.id.buttonGoback);
        bottom_navigation = (BottomNavigationView) findViewById(R. id.bottom_navigation);
        String[] transactionTypes = {"Income", "Spending"};
        // Tạo ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // Layout hiển thị dữ liệu
                transactionTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Gắn Adapter vào Spinner
        spinnerCategory.setAdapter(adapter);
        // Retrieve data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            idExpences=intent.getIntExtra("expenses",-1);
            if(idExpences!=-1) {
                String expenseDate = intent.getStringExtra("expense_date");
                int expenseAmount = intent.getIntExtra("expense_amount", 0);
                String expenseDescription = intent.getStringExtra("expense_description");
                String expenseCategory = intent.getStringExtra("expense_category");
                // Set data to fields
                editTextDate.setText(expenseDate);
                editTextAmount.setText(String.valueOf(expenseAmount));
                editTextDescription.setText(expenseDescription);

                // Set the selected category
                if (expenseCategory != null) {
                    int spinnerPosition = adapter.getPosition(expenseCategory);
                    spinnerCategory.setSelection(spinnerPosition);
                }
                buttonSaveExpense.setText("Update Expence");
            }
        }else{
            buttonSaveExpense.setText("Save Expence");
        }

        bottom_navigation.setSelectedItemId(R.id.nav_home);

        bottom_navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_statistics) {
                    startActivity(new Intent(AddExpenseActivity.this, AddExpenseActivity.class));
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(AddExpenseActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_add) {
                    return true;
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(AddExpenseActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            }
        });
        buttonSaveExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = editTextDate.getText().toString().trim();
                int amount;
                try {
                    amount = Integer.parseInt(editTextAmount.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(AddExpenseActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                String description = editTextDescription.getText().toString().trim();
                String category = spinnerCategory.getSelectedItem().toString();

                boolean isSaved;
                if (buttonSaveExpense.getText().equals("Update Expense") && idExpences != 0) {
                    objExpences obj = new objExpences();
                    obj.setId(idExpences);
                    obj.setDate(date);
                    obj.setAmount(amount);
                    obj.setDescription(description);
                    obj.setCategory(category);
                    isSaved = dbHelper.updateExpense(obj);
                } else {
                    isSaved = dbHelper.addExpense(date, amount, description, category, AddExpenseActivity.this);
                }

                if (isSaved) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(AddExpenseActivity.this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                }
            }
            // Method to display success dialog
            private void showSuccessDialog() {
                // Inflate the custom layout
                View customView = getLayoutInflater().inflate(R.layout.dialog_success, null);

                // Create the AlertDialog and set the custom layout
                AlertDialog.Builder builder = new AlertDialog.Builder(AddExpenseActivity.this);
                builder.setView(customView)
                        .setCancelable(true); // Allow dismissal by tapping outside the dialog

                // Show the AlertDialog
                AlertDialog alert = builder.create();
                alert.show();

                // Optionally dismiss the dialog after a delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    alert.dismiss();
                    finish(); // Close the activity
                }, 3000); // Dismiss after 3 seconds
            }

        });

        buttonGoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the custom layout
                View customView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

                // Create the AlertDialog and set the custom layout
                AlertDialog.Builder builder = new AlertDialog.Builder(AddExpenseActivity.this);
                builder.setView(customView)
                        .setCancelable(false) // Prevent the dialog from being canceled by touching outside
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish(); // Close activity and go back
                            }
                        })
                        .setNegativeButton("No", null); // Close dialog if "No" is clicked

                // Show the AlertDialog
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Get amount from EditText
        String amountString = editTextAmount.getText().toString();
        int amount = 0;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottom_navigation.setSelectedItemId(R.id.nav_add);
    }

}

