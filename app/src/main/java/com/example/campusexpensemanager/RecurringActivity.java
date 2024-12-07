package com.example.campusexpensemanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.Adapters.RecurringExpenseAdapter;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.Model.RecurringExpense;
import com.example.campusexpensemanager.R;

import java.util.ArrayList;
import java.util.List;

public class RecurringActivity extends AppCompatActivity {
    private List<RecurringExpense> recurringExpenses = new ArrayList<>();
    private RecurringExpenseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring);

        EditText editDesc = findViewById(R.id.etDescription);
        EditText editAmount = findViewById(R.id.etAmount);
        EditText editStart = findViewById(R.id.editStaartDate);
        EditText editEnd = findViewById(R.id.editEndDate);
        Button btnAddRecurringExpense = findViewById(R.id.btnAddRecurringExpense);
        Button buttonBack = findViewById(R.id.buttonBack);
        ListView listRecurringExpenses = findViewById(R.id.listRecurringExpenses);
        adapter = new RecurringExpenseAdapter(this, recurringExpenses);
        listRecurringExpenses.setAdapter(adapter);
        registerForContextMenu(listRecurringExpenses);
        loadRecurringExpensesFromDatabase();
        btnAddRecurringExpense.setOnClickListener(v -> {
            try {
                String description = editDesc.getText().toString().trim();
                String startDate = editStart.getText().toString().trim();
                String endDate = editEnd.getText().toString().trim();
                double amount = Double.parseDouble(editAmount.getText().toString().trim());

                if (description.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                RecurringExpense expense = new RecurringExpense();
                expense.setDescription(description);
                expense.setAmount(amount);
                expense.setStartDate(startDate);
                expense.setEndDate(endDate);
                expense.setCategory("");

                DatabaseHelper db = new DatabaseHelper(this);
                if (db.addRecurringExpense(expense)) {
                    recurringExpenses.add(expense); // Add to the list
                    adapter.notifyDataSetChanged(); // Refresh ListView
                    Toast.makeText(this, "Recurring Expense Added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error Adding Expense", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Unexpected Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        buttonBack.setOnClickListener(v -> {
            View customView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
            new AlertDialog.Builder(this)
                    .setView(customView)
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> finish())
                    .setNegativeButton("No", null)
                    .create()
                    .show();
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listRecurringExpenses) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.recurring_context_menu, menu);
            menu.setHeaderTitle("Choose an action");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        RecurringExpense selectedExpense = recurringExpenses.get(position);

        // Using if-else instead of switch
        if (item.getItemId() == R.id.action_edit_recurring) {
            showEditRecurringDialog(selectedExpense, position);
            return true;
        } else if (item.getItemId() == R.id.action_delete_recurring) {
            deleteRecurringExpense(selectedExpense, position);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }


    private void showEditRecurringDialog(RecurringExpense expense, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_recurring, null);
        builder.setView(dialogView);

        EditText editDesc = dialogView.findViewById(R.id.editDescription);
        EditText editAmount = dialogView.findViewById(R.id.editAmount);
        EditText editStart = dialogView.findViewById(R.id.editStartDate);
        EditText editEnd = dialogView.findViewById(R.id.editEndDate);

        editDesc.setText(expense.getDescription());
        editAmount.setText(String.valueOf(expense.getAmount()));
        editStart.setText(expense.getStartDate());
        editEnd.setText(expense.getEndDate());

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                expense.setDescription(editDesc.getText().toString().trim());
                expense.setAmount(Double.parseDouble(editAmount.getText().toString().trim()));
                expense.setStartDate(editStart.getText().toString().trim());
                expense.setEndDate(editEnd.getText().toString().trim());

                DatabaseHelper db = new DatabaseHelper(this);
                if (db.updateRecurringExpense(expense)) {
                    recurringExpenses.set(position, expense);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error updating expense", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
    private void deleteRecurringExpense(RecurringExpense expense, int position) {
        DatabaseHelper db = new DatabaseHelper(this);

        // Create a custom layout for the AlertDialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        // Set the image in the dialog
        ImageView imageView = dialogView.findViewById(R.id.dialog_image);
        imageView.setImageResource(R.drawable.hoe); // Replace with your image resource

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this recurring expense?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (db.deleteRecurringExpense(expense)) {
                        recurringExpenses.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error deleting expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void loadRecurringExpensesFromDatabase() {
        recurringExpenses.clear();
        DatabaseHelper db = new DatabaseHelper(this);
        SQLiteDatabase readableDb = db.getReadableDatabase();
        Cursor cursor = readableDb.rawQuery("SELECT * FROM recurring_expenses", null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    RecurringExpense expense = new RecurringExpense();
                    expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                    expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                    expense.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                    expense.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));

                    recurringExpenses.add(expense);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            readableDb.close();
        }
        adapter.notifyDataSetChanged();
    }
}
