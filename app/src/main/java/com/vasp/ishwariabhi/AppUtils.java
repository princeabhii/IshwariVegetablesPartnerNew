package com.vasp.ishwariabhi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUtils {

  public static Locale locale = Locale.US;

  public static boolean isConnectedToNetwork(final Context context) {
    final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
    assert connectivityManager != null;
    return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
  }



  public static String formatDateForDisplay(Date d, String format) {
    if (d == null) {
      return "";
    }
    return new SimpleDateFormat(format, getAppLocale()).format(d);
  }

  public static Locale getAppLocale() {
    if (locale == null) {
      locale = new Locale("en", "UK");
      //locale = Locale.getDefault();
    }
    return locale;
  }


  public static String convertDateyyyymmddToddmmyyyy(String yyyymmdd) {
    String ddmmyyyy;
    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd",getAppLocale());
    DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy",getAppLocale());
    Date date = null;
    try {
      date = inputFormat.parse(yyyymmdd);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    ddmmyyyy = outputFormat.format(date);
    return ddmmyyyy;
  }

  public static String convertDateddmmyyyyTOyyyymmdd(String ddmmyyyy) {
    String yyyymmdd;
    DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", getAppLocale());
    DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", getAppLocale());
    Date date = null;
    try {
      date = inputFormat.parse(ddmmyyyy);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    yyyymmdd = outputFormat.format(date);
    return yyyymmdd;
  }

  public static void showSnackBar(View view,String message){
    Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    snackbar.show();
  }
}
