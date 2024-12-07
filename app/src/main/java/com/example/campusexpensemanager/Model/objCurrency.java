package com.example.campusexpensemanager.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class objCurrency {
    String currency;
    double buy;
    double sell;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBuy() {
        return buy;
    }

    public void setBuy(double buy) {
        this.buy = buy;
    }

    public double getSell() {
        return sell;
    }

    public void setSell(double sell) {
        this.sell = sell;
    }
}

