package com.example.campusexpensemanager.Model;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class RecurringExpense {
    private int id;
    private String description;
    private double amount;
    private String category;
    private String startDate;
    private String endDate;

    public  RecurringExpense(){}
    public RecurringExpense(String description, double amount,
                            String category, String startDate, String endDate) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }




}