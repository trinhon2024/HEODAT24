package com.example.campusexpensemanager.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.campusexpensemanager.Model.objExpences;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.function;

import java.util.ArrayList;
import java.util.List;

public class ExpencesAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private ArrayList<objExpences> expenses;
    private ArrayList<objExpences> expensesOld;
    public ExpencesAdapter(Context context, ArrayList<objExpences> expenses) {
        this.context = context;
        this.expenses = expenses;
        this.expensesOld = expenses;
    }
    @Override
    public int getCount() {
        return expenses.size();
    }
    @Override
    public Object getItem(int position) {
        return expenses.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        }

        // Lấy đối tượng hiện tại
        objExpences expense = expenses.get(position);

        // Gán dữ liệu cho các TextView
        TextView textDate = (TextView) convertView.findViewById(R.id.textDate);
        TextView textAmount = (TextView) convertView.findViewById(R.id.textAmount);
        TextView textDescription = (TextView) convertView.findViewById(R.id.textDescription);


        textDate.setText(expense.getDate());
        textAmount.setText(function.formatCurrency(expense.getAmount()));
        textDescription.setText(expense.getDescription());
        if (expense.getCategory().equals("Income")) {
            textAmount.setTextColor(Color.GREEN);
        } else {
            textAmount.setTextColor(Color.RED);
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchKey = constraint.toString();
                if (searchKey.isEmpty()) {
                    expenses = expensesOld;
                } else {
                    ArrayList<objExpences> lst = new ArrayList<>();
                    for (objExpences obj : expensesOld) {
                        if (obj.getDescription().toLowerCase().contains(searchKey.toLowerCase())) {
                            lst.add(obj);
                        }
                    }
                    expenses = lst;
                }
                FilterResults flre = new FilterResults();
                flre.values = expenses;
                return flre;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                expenses = (ArrayList<objExpences>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
