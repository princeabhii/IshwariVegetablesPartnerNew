package com.vasp.ishwariabhi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vasp.ishwariabhi.admin.AdminDashboard;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;
import com.vasp.ishwariabhi.session.UserSession;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    EditText edEmail, edPassword;
    LinearLayout lvLoginButton;
    String strEmail, strPassword, version, UserId, UserName;
    UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userSession = new UserSession(getApplicationContext());
        UserId = userSession.getUserId();
        UserName = userSession.getUserName();

        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = info.versionName;

        lvLoginButton = findViewById(R.id.lvLoginButton);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);

        lvLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strEmail = edEmail.getText().toString().trim();
                strPassword = edPassword.getText().toString().trim();

                if (edEmail.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Your Email", Toast.LENGTH_LONG).show();
                } else if (edPassword.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Your Password", Toast.LENGTH_LONG).show();
                } else {
                    displayFirebaseRegId();
                }
            }
        });

    }

    private void displayFirebaseRegId() {

        FirebaseApp.initializeApp(LoginActivity.this);
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(@NonNull String s) {
                Log.e("newToken ::::::::::: ", s);
                LoginDetails(strEmail, strPassword, version, s);
            }
        });


    }

    private void LoginDetails(String email, String password, String version, String regId) {
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, "", "Login..", false, false);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.APP_SERVER_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        AllApi mApiService = retrofit.create(AllApi.class);
        Call<ReturnedResponsePojo> mService = mApiService.AdminLogin(email, password, version, regId);
        mService.enqueue(new Callback<ReturnedResponsePojo>() {

            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {
                loading.dismiss();
                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        String UserId = jsonResponse.User_Id;
                        String UserName = jsonResponse.Mgmt_Name;
                        String userMobile = jsonResponse.Mobile_No;
                        String userPassword = jsonResponse.Password;
                        String userType = jsonResponse.User_Type;
                        String userEmail = jsonResponse.Email_Id;
                        userSession.setUserId(UserId);
                        userSession.setPassword(userPassword);
                        userSession.setUserName(UserName);
                        userSession.setMobile_No(userMobile);
                        userSession.setEmail_Id(userEmail);
                        userSession.setUserType(userType);
                        userSession.setPrinterAddress("NA");
                        userSession.setLanguage(0);
                        userSession.setLanguageName("english");

//                        if (userType.equals("Admin")) {

                            Intent i = new Intent(getApplicationContext(), AdminDashboard.class);
                            startActivity(i);
                            finish();
//                        }
                    } else if (success.equals("2")) {
                        Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                    } else if (success.equals("0")) {
                        Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(getApplicationContext(), "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                loading.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(getApplicationContext(), "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);

                    }
                }).create().show();

    }

}