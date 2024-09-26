package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.AppUtils;
import com.vasp.ishwariabhi.BluetoothDataService;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.ViewFileActivity;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.CommonPojo;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;
import com.vasp.ishwariabhi.session.UserSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VendorMonthlyBillsActivity extends AppCompatActivity {

    RecyclerView rcvOrderDetails;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    OrderDetailsListAdapter orderDetailsListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String Vendor_Id, strFromDateSubmit, strCurrTime, strDateSubmit, strToDateSubmit, strVendorName, strPaymentStatus, Order_No, Total_Amount, Delivery_Amount, User_Total;
    long fromMiliseconds, toMiliSeconds;
    List<String> arrVendorsId = new ArrayList<String>();
    List<String> arrVendorsName = new ArrayList<String>();
    List<String> arrPaymentStatus = new ArrayList<String>();
    EditText edtSearch;
    TextView tvFromDate, tvToDate, tvNoData, tvSubTotal, tvTotal, tvDelivery;
    LinearLayout lvBack, lvOrderTotal;
    public static DecimalFormat money = new DecimalFormat("0.00");
    Spinner spnVendors, spnPaymentStatus;
    private List<CommonPojo> VendorsList = new ArrayList<>();
    File gpxfile;
    String pdfName;
    CardView cvSharePdf, cvPrintReceipt, cvMarkPaid;
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_monthly_bills);

        userSession = new UserSession(VendorMonthlyBillsActivity.this);
//        Vendor_Id = userSession.getUserId();
//        Log.e("TAG", "onCreateView: Vendor_Id" + Vendor_Id);
        rcvOrderDetails = findViewById(R.id.rcvOrderDetails);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvNoData = findViewById(R.id.tvNoData);
        lvOrderTotal = findViewById(R.id.lvOrderTotal);
        lvBack = findViewById(R.id.lvBack);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvTotal = findViewById(R.id.tvTotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        spnVendors = findViewById(R.id.spnVendors);
        spnPaymentStatus = findViewById(R.id.spnPaymentStatus);
        cvSharePdf = findViewById(R.id.cvSharePdf);
        cvPrintReceipt = findViewById(R.id.cvPrintReceipt);
        cvMarkPaid = findViewById(R.id.cvMarkPaid);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Calendar cal = Calendar.getInstance();
        strFromDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        strDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        strToDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        strCurrTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdf.parse(strFromDateSubmit);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mDate);
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            strFromDateSubmit = AppUtils.formatDateForDisplay(calendar.getTime(), "yyyy-MM-dd");
            Date newDate = calendar.getTime();
            long timeInMilliseconds = newDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            fromMiliseconds = timeInMilliseconds;
            Date newDateTo = cal.getTime();
            long timeInMillisecondsTo = newDateTo.getTime();
            System.out.println("Date in milli :: " + timeInMillisecondsTo);
            toMiliSeconds = timeInMillisecondsTo;
            tvToDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        cvSharePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneratePDF pdf = new GeneratePDF();
                pdf.execute();
            }
        });

        cvPrintReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printCustomReciept();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.equals("") || charSequence == null) {

                } else {
                    orderDetailsListAdapter.getFilter().filter(charSequence);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        spnVendors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

//                if (i > 0) {
                strVendorName = adapterView.getItemAtPosition(i).toString();
                Vendor_Id = arrVendorsId.get(i);
                getOrders();
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spnPaymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

//                if (i > 0) {
                strPaymentStatus = adapterView.getItemAtPosition(i).toString();
                getUsers();
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(VendorMonthlyBillsActivity.this, "From");
            }
        });

        tvFromDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strFromDateSubmit));

        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(VendorMonthlyBillsActivity.this, "To");
            }
        });

        populateSpinnerPayment();

    }

    private void getOrders() {

        progressDialog = ProgressDialog.show(VendorMonthlyBillsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetVendorOrdersDateWise(Vendor_Id, String.valueOf(fromMiliseconds), String.valueOf(toMiliSeconds), strPaymentStatus);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;
                    User_Total = jsonResponse.User_Total;
                    Delivery_Amount = jsonResponse.Delivery_Amount;
                    Total_Amount = jsonResponse.Total_Amount;
                    Order_No = jsonResponse.Order_Id;

                    if (success.equals("1")) {

                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getDetails()));

                        if (VegetablesList.size() > 0) {
                            double totalAmount = Double.parseDouble(Total_Amount);
                            tvSubTotal.setText("₹" + User_Total);
                            tvTotal.setText("₹" + money.format(totalAmount));
                            tvDelivery.setText("+ ₹" + Delivery_Amount);
                            lvOrderTotal.setVisibility(View.VISIBLE);

                            rcvOrderDetails.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            orderDetailsListAdapter = new OrderDetailsListAdapter(VegetablesList, VendorMonthlyBillsActivity.this);
                            rcvOrderDetails.setLayoutManager(new LinearLayoutManager(VendorMonthlyBillsActivity.this));

                            rcvOrderDetails.setAdapter(orderDetailsListAdapter);

                            if (strPaymentStatus.equals("UnPaid")) {
                                cvMarkPaid.setVisibility(View.VISIBLE);
                                cvMarkPaid.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(VendorMonthlyBillsActivity.this);
                                        builder1.setCancelable(false);
                                        builder1.setTitle("Mark Paid?");
                                        builder1.setMessage("Do you want to mark these bills as Paid?");
                                        builder1.setNegativeButton("Cancel",null);
                                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                changePaymentStatus("Paid");

                                            }
                                        });
                                        androidx.appcompat.app.AlertDialog dialog1 = builder1.create();
                                        dialog1.show();
                                    }
                                });

                            } else {
                                cvMarkPaid.setVisibility(View.GONE);
                            }

                        } else {
                            lvOrderTotal.setVisibility(View.GONE);
                            rcvOrderDetails.setVisibility(View.GONE);
                            cvMarkPaid.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);

                        }
                    } else {
                        rcvOrderDetails.setVisibility(View.GONE);
                        lvOrderTotal.setVisibility(View.GONE);
                        cvMarkPaid.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(VendorMonthlyBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(VendorMonthlyBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }


    public class OrderDetailsListAdapter extends RecyclerView.Adapter<OrderDetailsListAdapter.MyViewHolder> {

        private List<CommonPojo> OrderDetails_List;
        private List<CommonPojo> searchMeetingList;
        private Context context;


        public OrderDetailsListAdapter(List<CommonPojo> OrderDetails_List, Context context) {
            this.OrderDetails_List = OrderDetails_List;
            this.searchMeetingList = OrderDetails_List;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendor_monthly_vegetables_item, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo OrderDetailsList = searchMeetingList.get(i);

            holder.tvQuantity.setText(OrderDetailsList.getWeight() + " KG");
            holder.tvVegetableName.setText(OrderDetailsList.getVegetable_Name());
            holder.tvTotalAmount.setText("₹" + OrderDetailsList.getTotal_Amount());

            if (OrderDetailsList.getAttachment().equals("NA")) {
                Picasso.get()
                        .load(R.drawable.noimage)
                        .placeholder(R.drawable.noimage)
                        .into(holder.imgVegetable);
            } else {
                Picasso.get()
                        .load(OrderDetailsList.getAttachment()
                        )
                        .placeholder(R.drawable.noimage)
                        .into(holder.imgVegetable);

            }

            holder.cvMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(VendorMonthlyBillsActivity.this, VendorOrderDetailsActivity.class);
                    i.putExtra("From_Date", fromMiliseconds);
                    i.putExtra("To_Date", toMiliSeconds);
                    i.putExtra("Payment_Status", strPaymentStatus);
                    i.putExtra("Vendor_Id", Vendor_Id);
                    i.putExtra("Vendor_Name", strVendorName);
                    i.putExtra("Vegetable_Id", OrderDetailsList.getVegetable_Id());
                    startActivityForResult(i, 9282);
                }
            });

        }

        @Override
        public int getItemCount() {

            return searchMeetingList.size();
        }

        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString().toLowerCase();
                    Log.e("charString data", " " + charString);

                    try {
                        if (!charString.isEmpty() || !charString.equals("")) {
                            searchMeetingList = new ArrayList<>();

                            for (CommonPojo objectGallery : OrderDetails_List) {


                                if (objectGallery.getVegetable_Name().toLowerCase().contains(charString)) {

                                    searchMeetingList.add(objectGallery);
                                }

                            }
                        } else {
                            searchMeetingList = OrderDetails_List;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.e("gallerylist data", " " + OrderDetails_List.size());
                    Log.e("searchlist data", " " + searchMeetingList.size());


                    FilterResults filterResults = new FilterResults();
                    filterResults.values = searchMeetingList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                    searchMeetingList = (List<CommonPojo>) filterResults.values;
                    // refresh the list with filtered data
                    rcvOrderDetails.getRecycledViewPool().clear();
                    orderDetailsListAdapter.notifyDataSetChanged();
                }
            };
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvQuantity, tvVegetableName, tvTotalAmount;
            ImageView imgVegetable;
            CardView cvMain;


            public MyViewHolder(View view) {
                super(view);
                tvQuantity = view.findViewById(R.id.tvQuantity);
                tvVegetableName = view.findViewById(R.id.tvVegetableName);
                tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
                cvMain = view.findViewById(R.id.cvMain);
                imgVegetable = view.findViewById(R.id.imgVegetable);
            }
        }
    }

    private void changePaymentStatus(String strPaymentStatus) {

        progressDialog = ProgressDialog.show(VendorMonthlyBillsActivity.this, "", "Please Wait...", true, false);

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
        Log.e("TAG", "getUsers: DATA_PASSED" + String.valueOf(fromMiliseconds) + " " + String.valueOf(toMiliSeconds) + " " + strPaymentStatus);
        Call<ReturnedResponsePojo> mService = mApiService.UpdateHotelPaymentStatus(String.valueOf(fromMiliseconds), String.valueOf(toMiliSeconds), strPaymentStatus, Vendor_Id);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {
                        androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(VendorMonthlyBillsActivity.this);
                        builder1.setCancelable(false);
                        builder1.setTitle("Success!!");
                        builder1.setMessage("All Bills have been Marked as paid!");
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                getUsers();
                                getOrders();

                            }
                        });
                        androidx.appcompat.app.AlertDialog dialog1 = builder1.create();
                        dialog1.show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(VendorMonthlyBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(VendorMonthlyBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void getUsers() {

//        progressDialog = ProgressDialog.show(VendorMonthlyBillsActivity.this, "", "Please Wait...", true, false);

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
        Log.e("TAG", "getUsers: DATA_PASSED" + String.valueOf(fromMiliseconds) + " " + String.valueOf(toMiliSeconds) + " " + strPaymentStatus);
        Call<ReturnedResponsePojo> mService = mApiService.GetVendorsForMonthlyReceipt(String.valueOf(fromMiliseconds), String.valueOf(toMiliSeconds), strPaymentStatus);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

//                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    VendorsList = new ArrayList<>(Arrays.asList(jsonResponse.getVendors()));

                    if (success.equals("1")) {
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + VendorsList.size());
                        populateSpinner();
//                        if (VendorsList.size() > 0) {
//
//                        } else {
//                            Toast.makeText(VendorMonthlyBillsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        populateSpinner();
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + VendorsList.size());
                        Toast.makeText(VendorMonthlyBillsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(VendorMonthlyBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
//                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(VendorMonthlyBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(VendorMonthlyBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void selectDatePicker(Context context, String module) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (module.equals("From")) {
                    strFromDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date mDate = sdf.parse(strFromDateSubmit);
                        long timeInMilliseconds = mDate.getTime();
                        System.out.println("Date in milli :: " + timeInMilliseconds);
                        fromMiliseconds = timeInMilliseconds;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    tvFromDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strFromDateSubmit));
                } else {
                    strToDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date mDate = sdf.parse(strToDateSubmit);
                        long timeInMilliseconds = mDate.getTime();
                        System.out.println("Date in milli :: " + timeInMilliseconds);
                        toMiliSeconds = timeInMilliseconds;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    tvToDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
                    getUsers();
                }


            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        long now = System.currentTimeMillis() - 1000;
        dlg.getDatePicker().setMaxDate(now + (1000 * 60 * 60 * 24));
//        dlg.getDatePicker().setMinDate(new Date().getTime());
        dlg.show();
    }

    private void populateSpinner() {

        arrVendorsName = new ArrayList<String>();
        arrVendorsId = new ArrayList<String>();

        arrVendorsId.add("Select Vendor");
        arrVendorsName.add("Select Vendor");

        for (int i = 0; i < VendorsList.size(); i++) {
            arrVendorsName.add(VendorsList.get(i).getVendor_Name());
        }

        for (int i = 0; i < VendorsList.size(); i++) {
            arrVendorsId.add(VendorsList.get(i).getVendor_Id());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arrVendorsName);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnVendors.setAdapter(spinnerAdapter);

    }

    private void populateSpinnerPayment() {

        arrPaymentStatus = new ArrayList<String>();

        arrPaymentStatus.add("UnPaid");
        arrPaymentStatus.add("Paid");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arrPaymentStatus);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnPaymentStatus.setAdapter(spinnerAdapter);

    }

    @SuppressLint("StaticFieldLeak")
    private class GeneratePDF extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(VendorMonthlyBillsActivity.this,
                    getString(R.string.generating_pdf),
                    getString(R.string.please_wait));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(VendorMonthlyBillsActivity.this);
            builder1.setMessage(R.string.pdf_generated_do_you_want_to_open);
            builder1.setCancelable(false);
            builder1.setPositiveButton(getResources().getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                                try {
                                    Intent i = new Intent(VendorMonthlyBillsActivity.this, ViewFileActivity.class);
                                    i.putExtra("FilePath", gpxfile.getAbsolutePath());
                                    startActivity(i);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Intent target = new Intent(Intent.ACTION_VIEW);
                                target.setDataAndType(Uri.fromFile(gpxfile), "application/pdf");
                                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                Intent intent = Intent.createChooser(target, "Open File");
                                try {
                                    startActivityForResult(intent, 100);
                                    dialog.dismiss();
                                } catch (ActivityNotFoundException e) {
                                    // Instruct the user to install a PDF reader here,
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Pdf Reader is not found please install and try...",
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                        }
                    });

            builder1.setNegativeButton(
                    getResources().getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            generatePdfPaySlip();
            return null;
        }
    }

    private void generatePdfPaySlip() {
        try {
            //  String timestamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss", AppUtils.getAppLocale()).format(Calendar.getInstance().getTime());
            //     String Path = _strMainPath.toString() + File.separator + SConst.mPolice + File.separator + "mpolicePDF";
            File root = new File(Environment.getExternalStorageDirectory() + "/"+getString(R.string.app_name_export)+"/PDFS/");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//                String path = gpxfile.toURI();;
                root = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/"+getString(R.string.app_name_export)+"/PDFS/");
//                root = new File(ApplicationData.getInstance().mMainContext.getExternalFilesDir("") + "/Rakshak/RakshakPDF/");
                root.mkdirs();
            } else {
                root.mkdirs();
            }
            String name;

            name = "" + toMiliSeconds + Order_No;


            gpxfile = new File(root, name + ".pdf");

            pdfName = name + ".pdf";

            if (gpxfile.exists()) {
                gpxfile.delete();
            }
            gpxfile = new File(root, name + ".pdf");
            Document document = new Document(PageSize.A4, 10, 10, 120, 25);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(gpxfile));

            Rectangle rect = new Rectangle(0, 0, 0, 0);
            writer.setBoxSize("art", rect);
            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);
            document.open();
            PdfPTable table = new PdfPTable(3);

            table.setWidthPercentage(100);

            Paragraph p1 = new Paragraph();
            Paragraph p2 = new Paragraph();
            Paragraph p3 = new Paragraph();
            Font fontbold2 = FontFactory.getFont("Times-Roman", 10, Font.NORMAL);
            Font fontsmall = FontFactory.getFont("Times-Roman", 8, Font.NORMAL);
            p2.setFont(fontbold2);
            Font fontbold = FontFactory.getFont("Times-Roman", 12, Font.BOLD | Font.UNDERLINE);
            p1.setFont(fontbold);

            p3.setFont(fontbold2);
            try {
                fontbold2 = FontFactory.getFont("Times-Roman", 16, Font.NORMAL);
                p2.setFont(fontbold2);

                table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                Font bfBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
                Font fontTotalBold = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
                Font bf12 = new Font(Font.FontFamily.TIMES_ROMAN, 14);
                Font subtotalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);


                Paragraph paragraph = new Paragraph();
                PdfPTable table3 = new PdfPTable(5);
                table3.setWidthPercentage(80f);
                insertCell(table3, "Sr No.", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Vegetables", Element.ALIGN_CENTER, 2, bfBold12);
                insertCell(table3, "Weight", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Amount", Element.ALIGN_CENTER, 1, bfBold12);
//                insertCell(table3, "Total", Element.ALIGN_CENTER, 1, bfBold12);
                table3.setHeaderRows(1);

//                amountListFinal = new ArrayList<>();
//                vegetableListFinal = new ArrayList<>();
//                weightListFinal = new ArrayList<>();
//                totalAmountListFinal = new ArrayList<>();
//                for (int i = 0; i < numRows; i++) {
//                    double priceRow = rowInfo.get(i).getPrice();
//                    double weightRow = rowInfo.get(i).getNumPlates();
//                    String veggiesRow = rowInfo.get(i).getVegetables();
//                    double totalRow = rowInfo.get(i).getRowTotal();
//                    if (weightRow != 0) {
//                        amountListFinal.add(priceRow);
//                        vegetableListFinal.add(veggiesRow);
//                        weightListFinal.add(weightRow);
//                        totalAmountListFinal.add(totalRow);
//                    }
//                }


                for (int x = 0; x < VegetablesList.size(); x++) {

                    insertCell(table3, x + 1 + "", Element.ALIGN_CENTER, 1, bf12);
                    insertCell(table3, VegetablesList.get(x).getVegetable_Name(), Element.ALIGN_CENTER, 2, bf12);
                    insertCell(table3, VegetablesList.get(x).getWeight(), Element.ALIGN_CENTER, 1, bf12);
                    insertCell(table3, VegetablesList.get(x).getTotal_Amount(), Element.ALIGN_CENTER, 1, bf12);
//                    try {
//                        double vegetablePrice, vegetableWeight, totalAmount;
//                        vegetablePrice = Double.parseDouble(VegetablesList.get(x).getUser_Price().toString());
//                        vegetableWeight = Double.parseDouble(VegetablesList.get(x).getWeight().toString());
//                        totalAmount = vegetablePrice * vegetableWeight;
//                        insertCell(table3, money.format(totalAmount), Element.ALIGN_CENTER, 1, bf12);
//                    } catch (NumberFormatException e) {

//                    }

                }
                paragraph.add(table3);

                document.add(paragraph);
                document.add(p3);

//                PdfPTable tableSubTotal = new PdfPTable(6);
//                tableSubTotal.setWidthPercentage(80);
//                insertCell(tableSubTotal, "Sub Total", Element.ALIGN_CENTER, 5, subtotalFont);
//                insertCell(tableSubTotal, "" + User_Total, Element.ALIGN_CENTER, 1, subtotalFont);
//                tableSubTotal.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//                document.add(tableSubTotal);
//
//                PdfPTable tableDelivery = new PdfPTable(6);
//                tableDelivery.setWidthPercentage(80);
//                insertCell(tableDelivery, "Delivery Charges", Element.ALIGN_CENTER, 5, subtotalFont);
//                insertCell(tableDelivery, "" + Delivery_Amount, Element.ALIGN_CENTER, 1, subtotalFont);
//                tableDelivery.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//                document.add(tableDelivery);

                PdfPTable tableTotal = new PdfPTable(5);
                tableTotal.setWidthPercentage(80);
                insertCell(tableTotal, "Total", Element.ALIGN_CENTER, 4, fontTotalBold);
                insertCell(tableTotal, "Rs. " + Total_Amount, Element.ALIGN_CENTER, 1, fontTotalBold);
                tableTotal.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                document.add(tableTotal);

                PdfPTable table1212 = new PdfPTable(1);
                table1212.setWidthPercentage(80);
                table1212.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                PdfPCell cell2211 = new PdfPCell(new Phrase(""+getString(R.string.document_generated_from), fontsmall));
                cell2211.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table1212.addCell(cell2211);
                document.add(table1212);


            } catch (Exception e) {
                e.printStackTrace();
            }

            document.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class HeaderFooterPageEvent extends PdfPageEventHelper {

        public void onStartPage(PdfWriter writer, Document document) {

            Font fontInvoice = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.UNDERLINE, BaseColor.BLACK);
            Font fontCompanyName = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD, BaseColor.BLACK);

            String strCurrTime;
            strCurrTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());


            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("INVOICE/CASH MEMO", fontInvoice), 300, 800, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(""+getString(R.string.app_name_pdf), fontCompanyName), 300, 783, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(""+getString(R.string.shop_addr_pdf)), 300, 770, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(""+getString(R.string.mob_no_pdf)), 300, 757, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Time: " + strCurrTime + "   Date: " + AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit)), 300, 744, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Vendor : " + strVendorName), 300, 731, 0);
        }

        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Page " + document.getPageNumber()), 550, 30, 0);
        }
    }


    private void insertCell(PdfPTable table, String text, int align, int colspan, Font font) {

        String tmpText = text;
        //create a new cell with the specified Text and Font
        if (TextUtils.isEmpty(tmpText)) {
            tmpText = "";
        }
        PdfPCell cell = new PdfPCell(new Phrase(tmpText.trim(), font));
        //set the cell alignment
        cell.setHorizontalAlignment(align);
        //set the cell column span in case you want to merge two or more cells
        cell.setColspan(colspan);
        //in case there is no text and you wan to create an empty row
       /* if(text.trim().equalsIgnoreCase("")){
            cell.setMinimumHeight(10f);
        }*/
        //add the call to the table
        table.addCell(cell);

    }

    private void printCustomReciept() {
        if (mBluetoothSocket == null) {
            if (!userSession.getPrinterAddress().equals("NA")) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);

                    startActivityForResult(enableBtIntent,
                            REQUEST_ENABLE_BT);
                } else {
                    Log.e("TAG", "onCreate: PrinterAddress " + userSession.getPrinterAddress());
                    beginPrinting();
                }
            }
        } else {
            beginPrinting();
        }
    }

    private void beginPrinting() {

        String BILL = "                   VEGETABLES\n\n\n\n" +

                "                INVOICE/CASH MEMO    \n"
                + getString(R.string.app_name_print) +
                ""+getString(R.string.shop_addr_print) +
                ""+getString(R.string.mob_no_print) +
                "  " + "     Time: " + strCurrTime + "   Date: " + AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit) + "        \n" +
                "  Vendor : " + strVendorName + "   \n";
        BILL = BILL
                + "-----------------------------------------------\n";


        BILL = BILL + String.format("%1$-9s %2$-12s %3$7s %4$10s", "SrNo.", "Item", "Qty", "Rate");
        BILL = BILL + "\n";
        BILL = BILL
                + "-----------------------------------------------";
//                    }


        for (int x = 0; x < VegetablesList.size(); x++) {
            try {
                BILL = BILL + "\n " + String.format("%1$-7s %2$-16s %3$-6s %4$8s", x + 1 + "", VegetablesList.get(x).getVegetable_Name(), VegetablesList.get(x).getWeight(), VegetablesList.get(x).getTotal_Amount());
            } catch (NumberFormatException e) {

            }

        }

        BILL = BILL
                + "\n-----------------------------------------------";
        BILL = BILL + "\n\n ";

//                    BILL = BILL + "                Sub Total:" + "     Rs. " + User_Total + "\n";
//                    BILL = BILL + "                  Delivery:" + "   + Rs. " + Delivery_Amount + "\n";
//                    BILL = BILL
//                            + "-----------------------------------------------\n";
        BILL = BILL + "                     Total:" + "     Rs. " + Total_Amount + "\n";
//                    BILL = BILL + "\n " + " ";
//                    BILL = BILL + "\n " + " ";
//                    BILL = BILL + "                                  Proprietor" + "\n";

        BILL = BILL
                + "-----------------------------------------------\n";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";

        Log.e("TAG", "onCreate: PrinterAddress " + userSession.getPrinterAddress());
        Intent intent = new Intent(VendorMonthlyBillsActivity.this, BluetoothDataService.class);
        intent.putExtra("MAC_ADDRESS", userSession.getPrinterAddress());
        intent.putExtra("BILL", BILL);
        startService(intent);

    }


}