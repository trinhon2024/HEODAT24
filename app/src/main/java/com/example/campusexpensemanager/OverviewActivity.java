package com.example.campusexpensemanager;

import static com.example.campusexpensemanager.function.formatCurrencyVND;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.Model.objCurrency;
import com.example.campusexpensemanager.Model.objExpences;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {
    TextView tvTitle, tvCurrency, tvDescTop3;
    DatabaseHelper dbHelper;
    BottomNavigationView bottom_navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        dbHelper = new DatabaseHelper(this);
        tvCurrency = (TextView) findViewById(R.id.tvCurrency);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDescTop3 = (TextView) findViewById(R.id.tvDescTop3);
        bottom_navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_statistics) {
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(OverviewActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_add) {
                    startActivity(new Intent(OverviewActivity.this, AddExpenseActivity.class));
                    return true;
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(OverviewActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            }
        });

        String result = dbHelper.getTotalExpensesAsJson();
//       {
//            "totalIncome": 5000000,
//             "totalExpense": 3000000
//        }
        ExchangeCurrency.fetchExchangeRates(new ExchangeCurrency.FetchCallback() {
            @Override
            public void onSuccess(List<objCurrency> rates) {
                for (objCurrency obj : rates) {
                    if (obj.getCurrency().toLowerCase().equals("usd")) {
                        tvCurrency.setText("USD/VNĐ: " + formatCurrencyVND((int) obj.getSell()));
                        Log.i("OverView", obj.getCurrency() + " " + obj.getSell() + " " + obj.getBuy());
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "Call API Error !", Toast.LENGTH_LONG).show();
            }
        });
        try {
            JSONArray top3Desc = dbHelper.getTop3Chi_byMonth("2024-11");
            String topeDESCrition = "Top 3 Expences: ";
            for (int i = 0; i < top3Desc.length(); i++) {
                JSONObject jobj = top3Desc.getJSONObject(i); // Lấy từng JSONObject
                topeDESCrition += jobj.optString("description") +", ";
            }
            tvDescTop3.setText(topeDESCrition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        float incomePercentage = 0;
        float expensePercentage = 0;
        try {
            JSONObject jsonObject = new JSONObject(result);
            int totalIncome = jsonObject.optInt("totalIncome", 0);
            int totalExpense = jsonObject.optInt("totalExpense", 0);
            int recurring = jsonObject.optInt("recurring", 0);
            incomePercentage = (float) ((totalIncome * 100f) / (totalIncome + totalExpense));
            expensePercentage = (float) ((totalExpense * 100f) / (totalIncome + totalExpense));
            tvTitle.setText(formatCurrencyVND(totalIncome - totalExpense - recurring));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Initialize the PieChart
        PieChart pieChart = findViewById(R.id.pieChart);


        // Data for the PieChart
        ArrayList<PieEntry> entriesPie = new ArrayList<>();
        entriesPie.add(new PieEntry(incomePercentage, "Income")); // Replace 60f with actual data
        entriesPie.add(new PieEntry(expensePercentage, "Outcome")); // Replace 40f with actual data

        // Create a PieDataSet
        PieDataSet dataSet = new PieDataSet(entriesPie, "Income/Outcome");
        dataSet.setColors(Color.GREEN, Color.RED); // Example colors for "Thu" and "Chi"
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(16f);

        // Create PieData
        PieData pieData = new PieData(dataSet);

        // Configure the PieChart
        pieChart.setData(pieData);
        pieChart.setCenterText("Income vs Outcome");
        pieChart.setCenterTextSize(18f);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false); // Disable the description

        // Refresh the chart
        pieChart.invalidate();


        // Data for the chart (12 months)
//        ArrayList<BarEntry> entries = new ArrayList<>();
//        entries.add(new BarEntry(1, 5000)); // January
//        entries.add(new BarEntry(2, 4000)); // February
//        entries.add(new BarEntry(3, 4500)); // March
//        entries.add(new BarEntry(4, 3000)); // April
//        entries.add(new BarEntry(5, 5500)); // May
//        entries.add(new BarEntry(6, 2000)); // June
//        entries.add(new BarEntry(7, 3500)); // July
//        entries.add(new BarEntry(8, 6000)); // August
//        entries.add(new BarEntry(9, 7000)); // September
//        entries.add(new BarEntry(10, 3000)); // October
//        entries.add(new BarEntry(11, 8000)); // November
//        entries.add(new BarEntry(12, 4000)); // December
//
//        // Create BarDataSet
//        BarDataSet barDataSet = new BarDataSet(entries, "Monthly Expenses");
//        barDataSet.setColors(Color.BLUE); // Set bar color
//        barDataSet.setValueTextColor(Color.BLACK);
//        barDataSet.setValueTextSize(12f);
//
//        // Create BarData
//        BarData barData = new BarData(barDataSet);
//        barChart.setData(barData);
//
//        // Configure X-Axis
//        XAxis xAxis = barChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawGridLines(false);
//        xAxis.setGranularity(1f);
//        xAxis.setLabelCount(12);
//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
//                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
//                return months[(int) value - 1]; // Adjust for index starting at 0
//            }
//        });
//
//        // Configure chart appearance
//        barChart.getDescription().setEnabled(false); // Disable description
//        barChart.setFitBars(true); // Ensure bars fit within the chart
//        barChart.animateY(1000); // Add animation
//
//        // Refresh the chart
//        barChart.invalidate();

        // Initialize the BarChart
        BarChart barChart = findViewById(R.id.barChart);
        JSONArray jsonArray = dbHelper.getMonthlyTotalsAsJson();
        // Prepare data for the BarChart
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> months = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String month = jsonObject.getString("month");
                double totalThu = jsonObject.getDouble("totalThu");
                double totalChi = jsonObject.getDouble("totalChi");

                // Add data for a stacked bar (Thu and Chi in one bar)
                barEntries.add(new BarEntry(i, new float[]{(float) totalThu, (float) totalChi}));
                months.add(month); // Add month label
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("OverView", months.toString());
        Log.i("OverView", barEntries.toString());
        // Create a BarDataSet for the stacked bars
        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Totals (Income/Outcome)");
        barDataSet.setColors(Color.GREEN, Color.RED); // Set colors for Thu and Chi
        barDataSet.setStackLabels(new String[]{"Income", "Outcome"}); // Set labels for the stacked values
        // Create BarData
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.7f); // Set bar width

        // Configure the BarChart
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false); // Disable chart description
        barChart.animateY(1000); // Animate Y-axis

        // Configure the X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months)); // Set month labels
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // Ensure intervals match entries
        xAxis.setGranularityEnabled(true);

        // Refresh the chart
        barChart.invalidate();

    }
    @Override
    protected void onResume() {
        super.onResume();
        bottom_navigation.setSelectedItemId(R.id.nav_statistics);
    }

}
