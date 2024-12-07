package com.example.campusexpensemanager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;


import androidx.core.app.NotificationCompat;
import com.example.campusexpensemanager.Adapters.ExpencesAdapter;
import com.example.campusexpensemanager.Model.RecurringExpense;
import com.example.campusexpensemanager.Model.objExpences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Name and Version
    private static final String DATABASE_NAME = "HeoDatt.db";
    private static final int DATABASE_VERSION = 15;
    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    // Expense Table
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY = "category";
    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_EMAIL + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL);"
                ;
        db.execSQL(createUserTable);
        // Create Expenses Table
        String createExpenseTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_CATEGORY + " TEXT);";
        db.execSQL(createExpenseTable);
        db.execSQL("CREATE TABLE recurring_expenses (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    description TEXT NOT NULL," +
                "    amount REAL NOT NULL," +
                "    category TEXT NOT NULL," +
                "    start_date TEXT NOT NULL," +
                "    end_date TEXT" +
                ");");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS recurring_expenses;");
        onCreate(db);
    }
    public boolean registerUser(String username, String password, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);// Ideally, hash the password here
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1; // Returns true if insert is successful
    }
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists; // Returns true if user exists with the provided username and password
    }
    public void insertUser(String userName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, userName);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }
    public String getUserName() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String userName = cursor.getString(0);
            cursor.close();
            return userName;
        }
        return "Guest"; // Giá trị mặc định nếu không có dữ liệu
    }
    public boolean addExpense(String date, int amount, String description, String category, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        long result = db.insert(TABLE_EXPENSES, null, values);
        // Check the condition after adding the transaction
        if (result != -1 && "Spending".equals(category)) {
            int totalChi = getMonthlyExpenseSum();
            if (totalChi > 10000) {
                showNotification(context, "Your spending this month exceeds 10000 VND!", "You need to manage your spending properly.!");
            }
        }
        return result != -1;
    }
    public boolean updateExpense(objExpences obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, obj.getDate());
        values.put(COLUMN_AMOUNT, obj.getAmount());
        values.put(COLUMN_DESCRIPTION, obj.getDescription());
        values.put(COLUMN_CATEGORY, obj.getCategory());
        int result = db.update(TABLE_EXPENSES, values,
                COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(obj.getId())});
        return result > 0;
    }
    public boolean deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa tất cả dữ liệu trong bảng user
        int result = db.delete(TABLE_USERS, null, null);
        db.close();
        // Nếu result > 0 thì xóa thành công, trả về true
        return result > 0;
    }
    public boolean deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EXPENSES,
                COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }
    public int getMonthlyExpenseSum() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Get the current month and year
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is zero-based
        // Format the date as yyyy-MM for the query
        String monthPattern = String.format("%04d-%02d", year, month);
        // Query to calculate the sum of "Expense" for the current month
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_CATEGORY + " = 'Spending' AND " + COLUMN_DATE + " LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{monthPattern + "%"});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0); // Get the sum
        }
        cursor.close();
        return total;
    }
    private void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "expense_channel";
        String channelName = "Expense Notifications";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        // Add the image resource
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.hoe); // Replace 'expense_warning' with your image name in res/drawable
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                .bigPicture(largeIcon); // The main image
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(bigPictureStyle) // Add the style to the notification
                .setLargeIcon(largeIcon);  // Set a small version of the image for collapsed state
        notificationManager.notify(1, builder.build());
    }
    public ArrayList<objExpences> getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<objExpences> arr = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_EXPENSES,
                    null, null, null, null, null,
                    COLUMN_EXPENSE_ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) { // Ensure cursor is not null and has data
                do {
                    objExpences obj = new objExpences();
                    obj.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)));
                    obj.setAmount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    obj.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    obj.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    obj.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    arr.add(obj);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor to avoid memory leaks
            }
            db.close(); // Close database connection
        }
        return arr;
    }
    public ArrayList<objExpences> getBy_like(String desc) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<objExpences> arr = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selection = "desc LIKE ?";
            String[] selectionArgs = new String[]{"%" + desc + "%"};
            cursor = db.query(TABLE_EXPENSES,
                    null, selection, selectionArgs, null, null,
                    COLUMN_EXPENSE_ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) { // Ensure cursor is not null and has data
                do {
                    objExpences obj = new objExpences();
                    obj.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)));
                    obj.setAmount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    obj.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    obj.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    obj.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    arr.add(obj);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor to avoid memory leaks
            }
            db.close(); // Close database connection
        }
        return arr;
    }
    public ArrayList<objExpences> getBy_Date(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<objExpences> arr = new ArrayList<>();
        Cursor cursor = null;

        try {
            String selection = "date LIKE ?";
            String[] selectionArgs = new String[]{"" + date + ""};
            cursor = db.query(TABLE_EXPENSES,
                    null, selection, selectionArgs, null, null,
                    COLUMN_EXPENSE_ID + " DESC");

            if (cursor != null && cursor.moveToFirst()) { // Ensure cursor is not null and has data
                do {
                    objExpences obj = new objExpences();
                    obj.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)));
                    obj.setAmount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    obj.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    obj.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    obj.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    arr.add(obj);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor to avoid memory leaks
            }
            db.close(); // Close database connection
        }

        return arr;
    }
    public String getTotalExpensesAsJson() {
        SQLiteDatabase db = this.getReadableDatabase();
        JSONObject jsonResult = new JSONObject();
        Cursor cursor = null;
        String sql = "SELECT " +
                "(SELECT SUM(amount) FROM recurring_expenses  " +
                "WHERE (start_date <= '2024-12-01' AND (end_date IS NULL OR end_date >= '2024-12-30'))) AS recurring, " +
                "(SELECT SUM(amount) FROM expenses WHERE category = 'Icome') AS totalIncome, " +
                "(SELECT SUM(amount) FROM expenses WHERE category = 'Expense') AS totalExpense";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                int totalIncome = cursor.getInt(cursor.getColumnIndexOrThrow("totalIncome"));
                int totalExpense = cursor.getInt(cursor.getColumnIndexOrThrow("totalExpense"));
                int recurring = cursor.getInt(cursor.getColumnIndexOrThrow("recurring"));
                // Add data to JSON object
                jsonResult.put("totalIncome", totalIncome);
                jsonResult.put("totalExpense", totalExpense);
                jsonResult.put("recurring", recurring);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                jsonResult.put("error", e.getMessage());
            } catch (Exception jsonException) {
                jsonException.printStackTrace();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return jsonResult.toString();
    }
    public JSONArray getMonthlyTotalsAsJson() {
        SQLiteDatabase db = this.getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        String sql = "SELECT " +
                "strftime('%m', date) AS month, " +
                "SUM(CASE WHEN category = 'Icome' THEN amount ELSE 0 END) AS totalThu, " +
                "SUM(CASE WHEN category = 'Expense' THEN amount ELSE 0 END) AS totalChi," +
                "SUM(amount) AS total " +
                "FROM expenses " +
                "GROUP BY month " +
                "ORDER BY month";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Create a JSON object for each row
                    JSONObject jsonObject = new JSONObject();
                    String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
                    double totalThu = cursor.getDouble(cursor.getColumnIndexOrThrow("totalThu"));
                    double totalChi = cursor.getDouble(cursor.getColumnIndexOrThrow("totalChi"));
                    double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                    jsonObject.put("month", month);
                    jsonObject.put("totalThu", totalThu);
                    jsonObject.put("totalChi", totalChi);
                    jsonObject.put("total", total);
                    // Add the JSON object to the JSON array
                    jsonArray.put(jsonObject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return jsonArray;
    }
    public JSONArray getTop3Chi_byMonth(String yyyymm) {
        SQLiteDatabase db = this.getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        String selection = "strftime('%Y-%m',date)=? and category=?";
        String[] selectionArgs = new String[]{yyyymm,"Expense"};
        try {
            cursor = db.query("expenses", new String[]{"strftime('%Y-%m', date) AS month", "description", "SUM(amount) AS total"},
                    selection,selectionArgs,
                    "description,strftime('%Y-%m', date)",
                    null,"total desc","3");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Create a JSON object for each row
                    JSONObject jsonObject = new JSONObject();
                    String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                    jsonObject.put("month", month);
                    jsonObject.put("description", description);
                    jsonObject.put("total", total);
                    // Add the JSON object to the JSON array
                    jsonArray.put(jsonObject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return jsonArray;
    }
    public void createDATA() {
        String sql = "-- Insert 50 sample records\n" +
                "INSERT INTO expenses (date, amount, description, category)\n" +
                "VALUES\n" +
                "    -- January\n" +
                "    ('2024-01-03', 150.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-01-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-01-20', 500.00, 'Electricity Bill', 'Expense'),\n" +
                "    ('2024-01-25', 700.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-01-28', 300.00, 'Transport', 'Expense'),\n" +
                "    ('2024-01-02', 300.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-01-10', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-01-15', 700.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-01-22', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-01-30', 400.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- February\n" +
                "    ('2024-02-05', 180.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-02-12', 1000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-02-18', 250.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-02-23', 450.00, 'Gift from Friend', 'Icome'),\n" +
                "    ('2024-02-28', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-02-03', 150.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-02-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-02-20', 500.00, 'Electricity Bill', 'Expense'),\n" +
                "    ('2024-02-25', 700.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-02-28', 300.00, 'Transport', 'Expense'),\n" +
                "    ('2024-02-02', 300.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-02-10', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-02-15', 700.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-02-22', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-02-05', 180.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-02-12', 1000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-02-18', 250.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-02-23', 450.00, 'Gift from Friend', 'Icome'),\n" +
                "    ('2024-02-28', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-02-03', 150.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-02-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-02-20', 500.00, 'Electricity Bill', 'Expense'),\n" +
                "    ('2024-02-25', 700.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-02-28', 300.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- March\n" +
                "    ('2024-03-02', 300.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-03-10', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-03-15', 700.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-03-22', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-03-30', 400.00, 'Transport', 'Expense'),\n" +
                "    ('2024-03-05', 180.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-03-12', 1000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-03-18', 250.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-03-23', 450.00, 'Gift from Friend', 'Icome'),\n" +
                "    ('2024-03-28', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-03-03', 150.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-03-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-03-20', 500.00, 'Electricity Bill', 'Expense'),\n" +
                "    ('2024-03-25', 700.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-03-28', 300.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- April\n" +
                "    ('2024-04-01', 200.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-04-12', 1000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-04-18', 180.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-04-24', 500.00, 'Gift', 'Icome'),\n" +
                "    ('2024-04-29', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-04-03', 250.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-04-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-04-20', 300.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-04-27', 500.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-04-30', 200.00, 'Transport', 'Expense'),\n" +
                "    ('2024-04-02', 200.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-04-10', 3000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-04-15', 400.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-04-20', 800.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-04-27', 250.00, 'Coffee', 'Expense'),\n" +
                "\n" +
                "    -- May\n" +
                "    ('2024-05-06', 400.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-05-14', 3000.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-05-19', 500.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-05-25', 750.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-05-31', 180.00, 'Transport', 'Expense'),\n" +
                "    ('2024-05-01', 200.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-05-12', 1000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-05-18', 180.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-05-24', 500.00, 'Gift', 'Icome'),\n" +
                "    ('2024-05-29', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-05-03', 250.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-05-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-05-20', 300.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-05-27', 500.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-05-30', 200.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- June\n" +
                "    ('2024-06-03', 250.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-06-15', 2000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-06-20', 300.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-06-27', 500.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-06-30', 200.00, 'Transport', 'Expense'),\n" +
                "    ('2024-06-06', 400.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-06-14', 3000.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-06-19', 500.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-06-25', 750.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-06-31', 180.00, 'Transport', 'Expense'),\n" +
                "    ('2024-06-01', 200.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-06-12', 1000.00, 'Freelance Payment', 'Icome'),\n" +
                "\n" +
                "    -- July\n" +
                "    ('2024-07-04', 300.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-07-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-07-18', 500.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-07-26', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-07-30', 400.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-07-01', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-07-12', 2500.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-07-18', 200.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-07-25', 750.00, 'Gift', 'Icome'),\n" +
                "    ('2024-07-30', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-07-04', 180.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-07-14', 3000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-07-21', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-07-28', 500.00, 'Gift', 'Icome'),\n" +
                "    ('2024-07-31', 300.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- August\n" +
                "    ('2024-08-02', 200.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-08-10', 3000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-08-15', 400.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-08-20', 800.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-08-27', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-08-04', 300.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-08-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-08-18', 500.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-08-26', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-08-30', 400.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-08-01', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-08-12', 2500.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-08-18', 200.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-08-25', 750.00, 'Gift', 'Icome'),\n" +
                "    ('2024-08-30', 150.00, 'Phone Bill', 'Expense'),\n" +
                "\n" +
                "    -- September\n" +
                "    ('2024-09-01', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-09-12', 2500.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-09-18', 200.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-09-25', 750.00, 'Gift', 'Icome'),\n" +
                "    ('2024-09-30', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-09-06', 400.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-09-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-09-18', 300.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-09-24', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-09-30', 200.00, 'Transport', 'Expense'),\n" +
                "    ('2024-09-02', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-09-15', 3000.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-09-21', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-09-27', 800.00, 'Freelance Payment', 'Icome'),\n" +
                "\n" +
                "    -- October\n" +
                "    ('2024-10-04', 180.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-10-14', 3000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-10-21', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-10-28', 500.00, 'Gift', 'Icome'),\n" +
                "    ('2024-10-31', 300.00, 'Transport', 'Expense'),\n" +
                "    ('2024-10-01', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-10-12', 2500.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-10-18', 200.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-10-25', 750.00, 'Gift', 'Icome'),\n" +
                "    ('2024-10-30', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-10-06', 400.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-10-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-10-18', 300.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-10-24', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-10-30', 200.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- November\n" +
                "    ('2024-11-06', 400.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-11-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-11-18', 300.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-11-24', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-11-30', 200.00, 'Transport', 'Expense'),\n" +
                "    ('2024-11-04', 300.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-11-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-11-18', 500.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-11-26', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-11-30', 400.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-11-01', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-11-12', 2500.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-11-18', 200.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-11-25', 750.00, 'Gift', 'Icome'),\n" +
                "    ('2024-11-30', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-11-04', 180.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-11-14', 3000.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-11-21', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-11-28', 500.00, 'Gift', 'Icome'),\n" +
                "    ('2024-11-30', 300.00, 'Transport', 'Expense'),\n" +
                "\n" +
                "    -- December\n" +
                "    ('2024-12-02', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-12-15', 3000.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-12-21', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-12-27', 800.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-12-10', 3000.00, 'Salary', 'Icome'),\n" +
                "    ('2024-12-15', 400.00, 'Dining Out', 'Expense'),\n" +
                "    ('2024-12-20', 800.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-12-27', 250.00, 'Coffee', 'Expense'),\n" +
                "    ('2024-12-04', 300.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-12-12', 2500.00, 'Salary', 'Icome'),\n" +
                "    ('2024-12-18', 500.00, 'Car Maintenance', 'Expense'),\n" +
                "    ('2024-12-26', 600.00, 'Freelance Payment', 'Icome'),\n" +
                "    ('2024-12-30', 400.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-12-01', 350.00, 'Groceries', 'Expense'),\n" +
                "    ('2024-12-12', 2500.00, 'Bonus', 'Icome'),\n" +
                "    ('2024-12-18', 200.00, 'Shopping', 'Expense'),\n" +
                "    ('2024-12-25', 750.00, 'Gift', 'Icome'),\n" +
                "    ('2024-12-30', 150.00, 'Phone Bill', 'Expense'),\n" +
                "    ('2024-12-30', 400.00, 'Phone Bill', 'Expense');";
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(sql);
    }
    public boolean updatePassword(String username, String encryptedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", encryptedPassword);

        int rowsAffected = db.update("Users", values, "username = ?", new String[]{username});
        return rowsAffected > 0;
    }

    public boolean addRecurringExpense(RecurringExpense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", expense.getDescription());
        values.put("amount", expense.getAmount());
        values.put("category", expense.getCategory());
        values.put("start_date", expense.getStartDate());
        values.put("end_date", expense.getEndDate());
        long result = db.insert("recurring_expenses", null, values);
        db.close();
        return result != -1;
    }

    public boolean updateRecurringExpense(RecurringExpense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", expense.getDescription());
        values.put("amount", expense.getAmount());
        values.put("start_date", expense.getStartDate());
        values.put("end_date", expense.getEndDate());
        int result = db.update("recurring_expenses", values,
                "description = ? AND start_date = ?",
                new String[]{expense.getDescription(), expense.getStartDate()});
        db.close();
        return result > 0;
    }
    public boolean deleteRecurringExpense(RecurringExpense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("recurring_expenses", "description= ? AND start_date = ?",
                new String[]{expense.getDescription(), expense.getStartDate()});
        db.close();
        return result > 0;
    }











}

