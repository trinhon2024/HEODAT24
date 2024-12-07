package com.example.campusexpensemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class function {


    public static String formatCurrency(int amount) {
        if (amount >= 1_000_000) {
            // Định dạng cho đơn vị triệu
            return (amount / 1_000_000) + "tr" + ((amount % 1_000_000)/1_000) + "k";

        } else if (amount >= 1_000) {
            // Định dạng cho đơn vị nghìn
            return (amount / 1_000) + "k";
        } else {
            // Định dạng cho đơn vị đồng
            return amount + "đ";
        }
    }

    public static String formatCurrencyVND(int amount) {
        // Use the Vietnamese locale
        Locale locale = new Locale("vi", "VN");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        return currencyFormat.format(amount);
    }

    private static final String PREFS_NAME = "CampusExpense";
    private static final String KEY_SECRET_KEY = "SecretKey";
    private static final String AES = "AES";

    // Lưu SecretKey vào SharedPreferences nếu chưa tồn tại
    public static void saveSecretKeyIfNotExists(Context context, SecretKey secretKey) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Kiểm tra xem KEY_SECRET_KEY đã tồn tại chưa
        if (!sharedPreferences.contains(KEY_SECRET_KEY)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Chuyển SecretKey thành chuỗi Base64
            String keyString = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);

            // Lưu vào SharedPreferences
            editor.putString(KEY_SECRET_KEY, keyString);
            editor.apply();
        }
    }

    // Lấy SecretKey từ SharedPreferences
    public static SecretKey getSecretKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String keyString = sharedPreferences.getString(KEY_SECRET_KEY, null);
        if (keyString != null) {
            byte[] decodedKey = Base64.decode(keyString, Base64.DEFAULT);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, AES);
        }

        return null;
    }

    public static boolean isValidEmail(String email) {
        // Define the regex for email validation
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        // Compile the regex into a pattern
        Pattern pattern = Pattern.compile(regex);

        // Match the input email against the pattern
        Matcher matcher = pattern.matcher(email);

        // Return whether the email matches the regex
        return matcher.matches();
    }
}
