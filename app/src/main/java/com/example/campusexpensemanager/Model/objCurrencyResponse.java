package com.example.campusexpensemanager.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class objCurrencyResponse {
    @SerializedName("results")
    private List<objCurrency> results;

    public List<objCurrency> getResults() {
        return results;
    }
}