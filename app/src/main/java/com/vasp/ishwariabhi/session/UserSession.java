package com.vasp.ishwariabhi.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.vasp.ishwariabhi.LoginActivity;

public class UserSession {
    // Sharedpref file name
    private static final String PREF_NAME = "MyPrefs";
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Constructor
    public UserSession(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void setUserName(String UserName) {
        pref.edit().putString("UserName", UserName).commit();
    }

    public String getUserName() {
        String UserName = pref.getString("UserName","");
        return UserName;
    }
//    public String getUser_Id() {
//        String User_Id = pref.getString("User_Id","");
//        return User_Id;
//    }

    public void setLanguage(int Language) {
        pref.edit().putInt("Language", Language).commit();
    }

    public int getLanguage() {
        int Language = pref.getInt("Language",0);
        return Language;
    }

    public void setLanguageName(String LanguageName) {
        pref.edit().putString("LanguageName", LanguageName).commit();
    }

    public String getLanguageName() {
        String LanguageName = pref.getString("LanguageName","");
        return LanguageName;
    }

    public void setSell_Percentage(int Sell_Percentage) {
        pref.edit().putInt("Sell_Percentage", Sell_Percentage).commit();
    }

    public int getSell_Percentage() {
        int Sell_Percentage = pref.getInt("Sell_Percentage",0);
        return Sell_Percentage;
    }

    public void setSell_PercentageStatus(boolean Sell_PercentageStatus) {
        pref.edit().putBoolean("Sell_PercentageStatus", Sell_PercentageStatus).commit();
    }

    public boolean getSell_PercentageStatus() {
        boolean Sell_PercentageStatus = pref.getBoolean("Sell_PercentageStatus",false);
        return Sell_PercentageStatus;
    }

    public void setMobile_No(String Mobile_No) {
        pref.edit().putString("Mobile_No", Mobile_No).commit();
    }

    public String getMobile_No() {
        String Mobile_No = pref.getString("Mobile_No","");
        return Mobile_No;
    }

    public void setUserType(String UserType) {
        pref.edit().putString("UserType", UserType).commit();
    }

    public String getUserType() {
        String UserType = pref.getString("UserType","");
        return UserType;
    }

    public void setPrinterAddress(String PrinterAddress) {
        pref.edit().putString("PrinterAddress", PrinterAddress).commit();
    }

    public String getPrinterAddress() {
        String PrinterAddress = pref.getString("PrinterAddress","");
        return PrinterAddress;

    }

    public void setEmail_Id(String Email_Id) {
        pref.edit().putString("Email_Id", Email_Id).commit();
    }

    public String getEmail_Id() {
        String Email_Id = pref.getString("Email_Id","");
        return Email_Id;
    }

    public void setUserId(String UserId) {
        pref.edit().putString("UserId", UserId).commit();
    }

    public String getUserId() {
        String UserId = pref.getString("UserId","");
        return UserId;
    }

    public void setPassword(String Password) {
        pref.edit().putString("Password", Password).commit();
    }

    public String getPassword() {
        String Password = pref.getString("Password","");
        return Password;
    }

    public void setBusiness_Id(String Business_Id) {
        pref.edit().putString("Business_Id", Business_Id).commit();
    }

    public String getBusiness_Id() {
        String Business_Id = pref.getString("Business_Id","");
        return Business_Id;
    }

    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        _context.startActivity(i);

    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
