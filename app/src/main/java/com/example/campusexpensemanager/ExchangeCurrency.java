package com.example.campusexpensemanager;

import android.util.Log;

import com.example.campusexpensemanager.Model.objCurrency;
import com.example.campusexpensemanager.Model.objCurrencyResponse;

import java.util.List;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class ExchangeCurrency {
    //https://vapi.vnappmob.com/api/v2/exchange_rate/sbv{
    //{
    //    "results": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MzMzOTk0NjAsImlhdCI6MTczMjEwMzQ2MCwic2NvcGUiOiJleGNoYW5nZV9yYXRlIiwicGVybWlzc2lvbiI6MH0.NToZ_LAMtd7Xm0em-CPwd9pmgohk3j6cBuzt4VEAw8U"
    //}
    private static final String BASE_URL = "https://vapi.vnappmob.com/";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MzMzOTk0NjAsImlhdCI6MTczMjEwMzQ2MCwic2NvcGUiOiJleGNoYW5nZV9yYXRlIiwicGVybWlzc2lvbiI6MH0.NToZ_LAMtd7Xm0em-CPwd9pmgohk3j6cBuzt4VEAw8U";
    public interface ExchangeRateApi {
        @GET("api/v2/exchange_rate/sbv")
        Call<objCurrencyResponse> getExchangeRates(
                @Header("Authorization") String token
        );
    }
    public interface FetchCallback {
        void onSuccess(List<objCurrency> rates);

        void onError(Throwable t);
    }
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static ExchangeRateApi api = retrofit.create(ExchangeRateApi.class);
    public static void fetchExchangeRates(FetchCallback callback) {
        Call<objCurrencyResponse> call = api.getExchangeRates(BEARER_TOKEN);
        call.enqueue(new Callback<objCurrencyResponse>() {
            @Override
            public void onResponse(Call<objCurrencyResponse> call, Response<objCurrencyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy danh sách objCurrency từ lớp bọc
                    callback.onSuccess(response.body().getResults());
                    Log.i("API SUCCESS", response.body().getResults().toString());
                } else {
                    callback.onError(new Exception("Response not successful: " + response.message()));
                    Log.i("API ERROR", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<objCurrencyResponse> call, Throwable t) {
                callback.onError(t);
                Log.i("API ERROR", "Request failed: " + t.getMessage());
            }
        });
    }

}



