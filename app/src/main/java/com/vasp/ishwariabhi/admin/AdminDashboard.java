package com.vasp.ishwariabhi.admin;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vasp.ishwariabhi.AppUtils;
import com.vasp.ishwariabhi.BluetoothDataService;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.DeviceListActivity;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;
import com.vasp.ishwariabhi.session.UserSession;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminDashboard extends AppCompatActivity {
    UserSession userSession;
    CardView cvHotel, cvVendor, cvVegetable, cvManageOrders, cvViewQuantity, cvSetPrices, cvDailyReceipts,
            cvEditReceipts, cvMonthlyReceipts, cvManagePayments;
    RelativeLayout lvProfile, lvPrinter;
    ImageView imgProfile, imgPrinter;
    LinearLayout lvViewMore, lvSuperAdmin, lvProfit;
    String User_Id, version;
    String strCalenderYear, strCalenderMonth, strCalenderDay, strDayMiliseconds, strMonthMiliseconds, strYearMiliseconds, strPrinterAddress;
    long yearMiliseconds, monthMiliseconds, dayMiliseconds;
    TextView tvTodaysProfit, tvMonthlyProfit, tvYearlyProfit;
    private static final int REQ_CODE_VERSION_UPDATE = 530;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    FrameLayout lvMain;
    public static DecimalFormat money = new DecimalFormat("0.00");
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(AdminDashboard.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AdminDashboard.this, new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN, ACCESS_FINE_LOCATION, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 105);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,
                                CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,
                            CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

        userSession = new UserSession(getApplicationContext());
        User_Id = userSession.getUserId();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter("intentKey"));

        tvTodaysProfit = findViewById(R.id.tvTodaysProfit);
        tvMonthlyProfit = findViewById(R.id.tvMonthlyProfit);
        tvYearlyProfit = findViewById(R.id.tvYearlyProfit);
        cvManageOrders = findViewById(R.id.cvManageOrders);
        cvViewQuantity = findViewById(R.id.cvViewQuantity);
        lvViewMore = findViewById(R.id.lvViewMore);
        cvSetPrices = findViewById(R.id.cvSetPrices);
        cvDailyReceipts = findViewById(R.id.cvDailyReceipts);
        cvEditReceipts = findViewById(R.id.cvEditReceipts);
        cvMonthlyReceipts = findViewById(R.id.cvMonthlyReceipts);
        lvPrinter = findViewById(R.id.lvPrinter);
        imgPrinter = findViewById(R.id.imgPrinter);
        cvManagePayments = findViewById(R.id.cvManagePayments);
        lvProfit = findViewById(R.id.lvProfit);
        lvSuperAdmin = findViewById(R.id.lvSuperAdmin);

        if (!userSession.getPrinterAddress().equals("NA")) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                Log.e("TAG", "onCreate: PrinterAddress " + userSession.getPrinterAddress());
                Intent intent = new Intent(AdminDashboard.this, BluetoothDataService.class);
                intent.putExtra("MAC_ADDRESS", userSession.getPrinterAddress());
                intent.putExtra("BILL", "");
                startService(intent);
            }
        }

        if (userSession.getUserType().equals("SuperAdmin")) {
            lvSuperAdmin.setVisibility(View.VISIBLE);
            lvProfit.setVisibility(View.VISIBLE);
        } else {
            lvSuperAdmin.setVisibility(View.GONE);
            lvProfit.setVisibility(View.GONE);
        }

        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = info.versionName;

        Calendar calenderYear = Calendar.getInstance();   // this takes current date
        calenderYear.set(Calendar.DAY_OF_YEAR, 1);
        System.out.println(calenderYear.getTime());
        Log.e("TAG", "onCreate: TIMEEE" + calenderYear.getTime());
        strCalenderYear = AppUtils.formatDateForDisplay(calenderYear.getTime(), "yyyy-MM-dd");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdf.parse(strCalenderYear);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Year in milli :: " + timeInMilliseconds);
            yearMiliseconds = timeInMilliseconds;
            strYearMiliseconds = String.valueOf(yearMiliseconds);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Calendar calenderMonth = Calendar.getInstance();   // this takes current date
        calenderMonth.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(calenderMonth.getTime());
        Log.e("TAG", "onCreate: TIMEEE" + calenderMonth.getTime());
        strCalenderMonth = AppUtils.formatDateForDisplay(calenderMonth.getTime(), "yyyy-MM-dd");

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdfYear.parse(strCalenderMonth);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Month in milli :: " + timeInMilliseconds);
            monthMiliseconds = timeInMilliseconds;
            strMonthMiliseconds = String.valueOf(monthMiliseconds);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        strCalenderDay = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

        SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdfToday.parse(strCalenderDay);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Day in milli :: " + timeInMilliseconds);
            dayMiliseconds = timeInMilliseconds;
            strDayMiliseconds = String.valueOf(dayMiliseconds);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        cvHotel = findViewById(R.id.cvHotel);
        cvVendor = findViewById(R.id.cvVendor);
        cvVegetable = findViewById(R.id.cvVegetable);
        lvProfile = findViewById(R.id.lvProfile);
        imgProfile = findViewById(R.id.imgProfile);

        cvManageOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(AdminDashboard.this, ManageOrdersActivity.class), 9282);
            }
        });

        lvPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanBluetoothDevices();

            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, AdminProfileActivity.class));
            }
        });
        cvEditReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, EditBillsActivity.class));
            }
        });

        cvMonthlyReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), BillCategoryActivity.class);
                i.putExtra("Title", "Bill Category");
                startActivity(i);
            }
        });

        cvDailyReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, DailyReceiptsActivity.class));
            }
        });

        cvSetPrices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, PriceTypeActivity.class));
            }
        });

        cvManagePayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), BillCategoryActivity.class);
                i.putExtra("Title", "Payment Category");
                startActivity(i);
            }
        });

        cvViewQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, ViewQuantityAdminActivity.class));
            }
        });

        cvHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboard.this, ManageHotelActivity.class));
            }
        });
        cvVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboard.this, ManageVendorsActivity.class));
            }
        });
        cvVegetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboard.this, AllVegetablesActivity.class));
            }
        });
        lvViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, ProfitHistoryActivity.class));
            }
        });

        if (!isNetworkConnectionAvailable()) {
            checkNetworkConnection();
        } else {
            displayFirebaseRegId();
        }


    }

    private void displayFirebaseRegId() {

        FirebaseApp.initializeApp(AdminDashboard.this);
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(AdminDashboard.this, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(@NonNull String s) {
                Log.e("newToken ::::::::::: ", s);
                getUserStatus(s);
            }
        });


    }

    public void checkNetworkConnection() {
        SweetAlertDialog builder = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        builder.setTitle("No internet Connection");
        builder.setContentText("Please turn on internet connection to continue");
        builder.setCancelable(false);
        builder.setConfirmButton("Retry", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                builder.dismiss();
                isNetworkConnectionAvailable();
            }
        });
        builder.show();
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
            Log.d("Network", "Connected");
            displayFirebaseRegId();
            return true;
        } else {
            checkNetworkConnection();
            Log.d("Network", "Not Connected");
            return false;
        }
    }

    private void getUserStatus(String token) {

//        final ProgressDialog progressDialog = ProgressDialog.show(AdminDashboard.this, "", "Please Wait...", true, false);


        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.APP_SERVER_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AllApi mApiService = retrofit.create(AllApi.class);
        Call<ReturnedResponsePojo> mService = mApiService.CheckUserStatus(User_Id, version, token);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

//                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {
                        getAllProfits();
                        checkForAppUpdate();
                        checkNewAppVersionState();
                    } else {
                        new AlertDialog.Builder(AdminDashboard.this)
                                .setTitle("User Inactive!")
                                .setCancelable(false)
                                .setMessage("Sorry you no longer have access to this app.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {
                                        finish();
                                    }
                                }).create().show();

                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminDashboard.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminDashboard.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminDashboard.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
//                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminDashboard.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminDashboard.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void getAllProfits() {

        final ProgressDialog progressDialog = ProgressDialog.show(AdminDashboard.this, "", "Please Wait...", true, false);


        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.APP_SERVER_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AllApi mApiService = retrofit.create(AllApi.class);
        Call<ReturnedResponsePojo> mService = mApiService.GetDayProfit(strDayMiliseconds, strMonthMiliseconds, strYearMiliseconds, strDayMiliseconds);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        double yearProfit = Double.parseDouble(jsonResponse.Year_Total_Profit);
                        double monthProfit = Double.parseDouble(jsonResponse.Month_Total_Profit);
                        double dayProfit = Double.parseDouble(jsonResponse.Day_Total_Profit);

                        tvYearlyProfit.setText("Rs. " + money.format(yearProfit));
                        tvMonthlyProfit.setText("Rs. " + money.format(monthProfit));
                        tvTodaysProfit.setText("Rs. " + money.format(dayProfit));
                    } else {
                        Toast.makeText(AdminDashboard.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminDashboard.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminDashboard.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminDashboard.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminDashboard.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminDashboard.this, "Error " + t, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
//        loadFragment(new HomeFragment());
//        checkNewAppVersionState();
        //Refresh your stuff here
    }

    @Override
    protected void onDestroy() {
        unregisterInstallStateUpdListener();
        super.onDestroy();
    }

    private void checkForAppUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(AdminDashboard.this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Create a listener to track request state updates.
        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState installState) {
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED)
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister();
            }
        };

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Log.e("", "checkForAppUpdate: playstore yes");
                // Request the update.
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Start an update.
                    startAppUpdateImmediate(appUpdateInfo);
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                    // Before starting an update, register a listener for updates.
                    appUpdateManager.registerListener(installStateUpdatedListener);
                    // Start an update.
                    startAppUpdateFlexible(appUpdateInfo);
                }
            } else {
                Log.e("", "checkForAppUpdate: playstore no");
            }
        });
    }

    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {

            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private void popupSnackbarForCompleteUpdateAndUnregister() {
        Snackbar snackbar = Snackbar.make(lvMain, "UPDATE", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Restart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.show();

        unregisterInstallStateUpdListener();
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            //FLEXIBLE:
                            // If the update is downloaded but not installed,
                            // notify the user to complete the update.
                            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                popupSnackbarForCompleteUpdateAndUnregister();
                            }

                            //IMMEDIATE:
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                startAppUpdateImmediate(appUpdateInfo);
                            }
                        });

    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

//    @SuppressLint("NewApi")
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 9282) {
//            getAllProfits();
//        }
//    }

    private void scanBluetoothDevices() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(AdminDashboard.this, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
//                ListPairedDevices();
//                discover();
                Intent connectIntent = new Intent(AdminDashboard.this,
                        DeviceListActivity.class);
                startActivityForResult(connectIntent,
                        REQUEST_CONNECT_DEVICE);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v("TAG", "Coming incoming address " + mDeviceAddress);
                    strPrinterAddress = mDeviceAddress;
                    if (strPrinterAddress != null) {
                        userSession.setPrinterAddress(strPrinterAddress);
                        Intent intent = new Intent(AdminDashboard.this, BluetoothDataService.class);
                        intent.putExtra("MAC_ADDRESS", mDeviceAddress);
                        intent.putExtra("BILL", "");
                        startService(intent);
                    }
//                    startPrinterConnection(mDeviceAddress);
                }
                break;

            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
//                    ListPairedDevices();
                    Intent connectIntent = new Intent(AdminDashboard.this,
                            DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(AdminDashboard.this, "Bluetooth Permission Required enable", Toast.LENGTH_SHORT).show();
                }
                break;

            case 9282:
                getAllProfits();
                break;
        }
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("key");
            if (message.equals("Yes")) {
                imgPrinter.setColorFilter(context.getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            }
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };
}