package com.example.campusexpensemanager.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campusexpensemanager.Model.RecurringExpense;
import com.example.campusexpensemanager.R;

import java.util.List;

public class RecurringExpenseAdapter extends ArrayAdapter<RecurringExpense> {
    public RecurringExpenseAdapter(Context context, List<RecurringExpense> expenses) {
        super(context, 0, expenses);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_recurring_expense, parent, false);
        }

        RecurringExpense expense = getItem(position);

        TextView tvDescription = convertView.findViewById(R.id.tvDescription);
        TextView tvAmount = convertView.findViewById(R.id.tvAmount);
        TextView tvDates = convertView.findViewById(R.id.tvDates);

        tvDescription.setText(expense.getDescription());
        tvAmount.setText("Amount: " + expense.getAmount());
        tvDates.setText("From: " + expense.getStartDate() + "      To: " + expense.getEndDate());

        return convertView;
    }
}
