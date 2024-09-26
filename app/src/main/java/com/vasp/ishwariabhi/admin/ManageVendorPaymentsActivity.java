package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
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
import com.vasp.ishwariabhi.AppUtils;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageVendorPaymentsActivity extends AppCompatActivity {

    RecyclerView rcvPaymentDetails;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    PaymentsAdapter paymentsAdapter;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id = "", strFromDateSubmit, strDateSubmit, strCurrTime, strToDateSubmit, strPaymentDateSubmit,
            strUserName, strPaymentStatus, Remaining_Amount, Total_Paid, Total_Amount, strPaymentType = "",strVegetableId = "", pdfName;
    long fromMiliseconds, toMiliSeconds, paymentMiliSeconds;
    List<String> arrUsersId = new ArrayList<String>();
    List<String> arrUsersName = new ArrayList<String>();
    List<String> arrPaymentStatus = new ArrayList<String>();
    EditText edtSearch, edtWeight, edtAmount, edtTotalAmount, edtChequeNumber;
    TextInputLayout textInpChequeNo;
    TextView tvFromDate, tvToDate, tvNoData, tvPaymentDate, tvTotalAmount;
    LinearLayout lvBack;
    Spinner spnUsers, spnPaymentStatus, spnVegetables;
    private List<CommonPojo> UsersList = new ArrayList<>();
    protected static final String TAG = "TAG";
    BottomSheetDialog bottomSheetDialog;
    RadioGroup rgPaymentType;
    RadioButton rbCash, rbOnline, rbCheque;
    CardView cvAddPayment, cvAddPaymentBs, cvSharePdf;
    File gpxfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vendor_payments);

        userSession = new UserSession(ManageVendorPaymentsActivity.this);
//        User_Id = userSession.getUserId();
        Log.e("TAG", "onCreateView: User_Id" + User_Id);
        rcvPaymentDetails = findViewById(R.id.rcvPaymentDetails);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvNoData = findViewById(R.id.tvNoData);
        lvBack = findViewById(R.id.lvBack);
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        spnUsers = findViewById(R.id.spnUsers);
        spnPaymentStatus = findViewById(R.id.spnPaymentStatus);
        cvAddPayment = findViewById(R.id.cvAddPayment);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        cvSharePdf = findViewById(R.id.cvSharePdf);

        bottomSheetDialog = new BottomSheetDialog(ManageVendorPaymentsActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_add_vendor_payment);
        bottomSheetDialog.setCanceledOnTouchOutside(false);


        tvPaymentDate = bottomSheetDialog.findViewById(R.id.tvPaymentDate);
        edtWeight = bottomSheetDialog.findViewById(R.id.edtWeight);
        edtAmount = bottomSheetDialog.findViewById(R.id.edtAmount);
        spnVegetables = bottomSheetDialog.findViewById(R.id.spnVegetables);
        edtTotalAmount = bottomSheetDialog.findViewById(R.id.edtTotalAmount);
        rgPaymentType = bottomSheetDialog.findViewById(R.id.rgPaymentType);
        textInpChequeNo = bottomSheetDialog.findViewById(R.id.textInpChequeNo);
        edtChequeNumber = bottomSheetDialog.findViewById(R.id.edtChequeNumber);
        cvAddPaymentBs = bottomSheetDialog.findViewById(R.id.cvAddPaymentBs);
        rbCash = bottomSheetDialog.findViewById(R.id.rbCash);
        rbOnline = bottomSheetDialog.findViewById(R.id.rbOnline);
        rbCheque = bottomSheetDialog.findViewById(R.id.rbCheque);

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
        strPaymentDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
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

            SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date mDateToDate = sdfToDate.parse(strToDateSubmit);
                long timeInMillisecondsToDate = mDateToDate.getTime();
                System.out.println("Date in milli :: " + timeInMillisecondsToDate);
                toMiliSeconds = timeInMillisecondsToDate;
                paymentMiliSeconds = timeInMillisecondsToDate;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            tvToDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
            tvPaymentDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strPaymentDateSubmit));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        rgPaymentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rbCash) {
                    strPaymentType = "Cash";
                    textInpChequeNo.setVisibility(View.GONE);
                } else if (i == R.id.rbCheque) {
                    strPaymentType = "Cheque";
                    textInpChequeNo.setVisibility(View.VISIBLE);
                } else if (i == R.id.rbOnline) {
                    strPaymentType = "Online";
                    textInpChequeNo.setVisibility(View.GONE);
                }
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
                    paymentsAdapter.getFilter().filter(charSequence);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        spnUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i > 0) {
                    strUserName = adapterView.getItemAtPosition(i).toString();
                    User_Id = arrUsersId.get(i);
                    GetPayments();
                } else {
                    User_Id = "";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                User_Id = "";
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

        tvPaymentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(ManageVendorPaymentsActivity.this, "Payment");
            }
        });

        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(ManageVendorPaymentsActivity.this, "From");
            }
        });

        cvAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User_Id.equals("")) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Select Vendor!", Toast.LENGTH_SHORT).show();
                } else {
                    bottomSheetDialog.show();
                }
            }
        });

        tvFromDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strFromDateSubmit));

        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(ManageVendorPaymentsActivity.this, "To");
            }
        });

        cvAddPaymentBs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String strWeight = edtWeight.getText().toString();
                String strAmount = edtAmount.getText().toString();
                String strTotalAmount = edtTotalAmount.getText().toString();
                String strChequeNo = edtChequeNumber.getText().toString();

                if (strPaymentDateSubmit.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Select Date!", Toast.LENGTH_SHORT).show();
                } else if (strWeight.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Enter Weight", Toast.LENGTH_SHORT).show();
                } else if (strAmount.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Enter Amount.", Toast.LENGTH_SHORT).show();
                } else if (strTotalAmount.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Enter Total Amount.", Toast.LENGTH_SHORT).show();
                } else if (strPaymentType.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Select Payment Type!", Toast.LENGTH_SHORT).show();
                } else if (strVegetableId.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Select Vegetable!", Toast.LENGTH_SHORT).show();
                } else if (strPaymentType.equals("Cheque") && strChequeNo.isEmpty()) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Enter Cheque No.", Toast.LENGTH_SHORT).show();
                } else {
                    InsertPayment(strWeight,strAmount,strTotalAmount, strChequeNo);
                }
            }
        });

        cvSharePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneratePDF pdf = new GeneratePDF();
                pdf.execute();
            }
        });

        populateSpinnerPayment();
        getUsers();

    }

    private void DeletePayment(String strPaymentId) {

        final ProgressDialog progressDialog = ProgressDialog.show(ManageVendorPaymentsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.DeleteVendorPayment(strPaymentId);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        new AlertDialog.Builder(ManageVendorPaymentsActivity.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Payment Deleted Successfully!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        GetPayments();

                                    }
                                }).create().show();

                    } else {
                        rcvPaymentDetails.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void InsertPayment(String strWeight,String strAmount,String strTotalAmount, String strChequeNo) {

        final ProgressDialog progressDialog = ProgressDialog.show(ManageVendorPaymentsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.InsertVendorPayment(userSession.getUserId(), String.valueOf(paymentMiliSeconds),
                strWeight,strAmount,strTotalAmount, strPaymentType, User_Id, strChequeNo);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        edtWeight.setText("");
                        edtAmount.setText("");
                        edtTotalAmount.setText("");
                        edtChequeNumber.setText("");
                        rgPaymentType.clearCheck();
                        strPaymentType = "";
                        textInpChequeNo.setVisibility(View.GONE);
                        bottomSheetDialog.dismiss();

                        new AlertDialog.Builder(ManageVendorPaymentsActivity.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Payment Inserted Successfully!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        GetPayments();

                                    }
                                }).create().show();

                    } else {
                        rcvPaymentDetails.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void GetPayments() {

        final ProgressDialog progressDialog = ProgressDialog.show(ManageVendorPaymentsActivity.this, "", "Please Wait...", true, false);


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
        Log.e(TAG, "GetPayments: User_Id " + User_Id + " fromMiliseconds " + String.valueOf(fromMiliseconds) + " toMiliSeconds "
                + toMiliSeconds + " strPaymentStatus " + strPaymentStatus);
        Call<ReturnedResponsePojo> mService = mApiService.GetVendorPayments(User_Id, String.valueOf(fromMiliseconds), String.valueOf(toMiliSeconds), strPaymentStatus);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;
                    Total_Amount = jsonResponse.Total_Amount;

                    tvTotalAmount.setText("Rs. " + Total_Amount);

                    if (success.equals("1")) {

                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getDetails()));

                        if (VegetablesList.size() > 0) {

                            rcvPaymentDetails.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            paymentsAdapter = new PaymentsAdapter(VegetablesList, ManageVendorPaymentsActivity.this);
                            rcvPaymentDetails.setLayoutManager(new LinearLayoutManager(ManageVendorPaymentsActivity.this));

                            rcvPaymentDetails.setAdapter(paymentsAdapter);

                        } else {
                            rcvPaymentDetails.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);

                        }
                    } else {
                        rcvPaymentDetails.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }


    public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.MyViewHolder> {

        private List<CommonPojo> OrderDetails_List;
        private List<CommonPojo> searchMeetingList;
        private Context context;


        public PaymentsAdapter(List<CommonPojo> OrderDetails_List, Context context) {
            this.OrderDetails_List = OrderDetails_List;
            this.searchMeetingList = OrderDetails_List;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.payments_item, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo arrPayments = searchMeetingList.get(i);

            holder.tvPaymentAmount.setText("Rs. " + arrPayments.getPayment_Amount());
            holder.tvPaymentType.setText(arrPayments.getPayment_Type());
            holder.tvPaymentDate.setText("Date : " + arrPayments.getPayment_Date() + "");
            holder.tvAddedOn.setText(arrPayments.getAdded_On());

            if (arrPayments.getCheque_No() == null || arrPayments.getCheque_No().equals("")) {
                holder.tvChequeNo.setVisibility(View.GONE);
                holder.tvChequeNoTitle.setVisibility(View.GONE);
            } else {
                holder.tvChequeNo.setText("" + arrPayments.getCheque_No());
            }

            holder.cvMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(ManageVendorPaymentsActivity.this)
                            .setTitle("Really Delete?")
                            .setMessage("Are you sure you want to delete this payment?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    DeletePayment(arrPayments.getPayment_Id());

                                }
                            }).create().show();

                    return true;
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
                    rcvPaymentDetails.getRecycledViewPool().clear();
                    paymentsAdapter.notifyDataSetChanged();
                }
            };
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvPaymentDate, tvPaymentType, tvPaymentAmount, tvAddedOn, tvChequeNoTitle, tvChequeNo;
            CardView cvMain;


            public MyViewHolder(View view) {
                super(view);
                tvPaymentDate = view.findViewById(R.id.tvPaymentDate);
                tvPaymentType = view.findViewById(R.id.tvPaymentType);
                tvPaymentAmount = view.findViewById(R.id.tvPaymentAmount);
                tvAddedOn = view.findViewById(R.id.tvAddedOn);
                tvChequeNoTitle = view.findViewById(R.id.tvChequeNoTitle);
                tvChequeNo = view.findViewById(R.id.tvChequeNo);
                cvMain = view.findViewById(R.id.cvMain);
            }
        }
    }

    private void getUsers() {

//        progressDialog = ProgressDialog.show(ManagePaymentsActivity.this, "", "Please Wait...", true, false);

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
        Call<ReturnedResponsePojo> mService = mApiService.GetVendorsForPayments(String.valueOf(fromMiliseconds), String.valueOf(toMiliSeconds));

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

//                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    UsersList = new ArrayList<>(Arrays.asList(jsonResponse.getDetails()));

                    if (success.equals("1")) {
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + UsersList.size());
                        populateSpinner();
//                        if (UsersList.size() > 0) {
//
//                        } else {
//                            Toast.makeText(ManagePaymentsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        populateSpinner();
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + UsersList.size());
                        Toast.makeText(ManageVendorPaymentsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ManageVendorPaymentsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
//                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ManageVendorPaymentsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
                } else if (module.equals("To")) {
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
                } else {
                    strPaymentDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date mDate = sdf.parse(strPaymentDateSubmit);
                        long timeInMilliseconds = mDate.getTime();
                        System.out.println("Date in milli :: " + timeInMilliseconds);
                        paymentMiliSeconds = timeInMilliseconds;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    tvPaymentDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strPaymentDateSubmit));
                }


            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        long now = System.currentTimeMillis() - 1000;
        dlg.getDatePicker().setMaxDate(now + (1000 * 60 * 60 * 24));
//        dlg.getDatePicker().setMinDate(new Date().getTime());
        dlg.show();
    }

    private void populateSpinner() {

        arrUsersName = new ArrayList<String>();
        arrUsersId = new ArrayList<String>();

        arrUsersId.add("Select Vendor");
        arrUsersName.add("Select Vendor");

        for (int i = 0; i < UsersList.size(); i++) {
            arrUsersName.add(UsersList.get(i).getVendor_Name());
        }

        for (int i = 0; i < UsersList.size(); i++) {
            arrUsersId.add(UsersList.get(i).getVendor_Id());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(ManageVendorPaymentsActivity.this,
                R.layout.spinner_item, arrUsersName);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnUsers.setAdapter(spinnerAdapter);

    }

    private void populateSpinnerPayment() {

        arrPaymentStatus = new ArrayList<String>();

        arrPaymentStatus.add("All");
        arrPaymentStatus.add("Cash");
        arrPaymentStatus.add("Online");
        arrPaymentStatus.add("Cheque");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(ManageVendorPaymentsActivity.this,
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
            progressDialog = ProgressDialog.show(ManageVendorPaymentsActivity.this,
                    getString(R.string.generating_pdf),
                    getString(R.string.please_wait));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(ManageVendorPaymentsActivity.this);
            builder1.setMessage(R.string.pdf_generated_do_you_want_to_open);
            builder1.setCancelable(false);
            builder1.setPositiveButton(getResources().getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                                try {
                                    Intent i = new Intent(ManageVendorPaymentsActivity.this, ViewFileActivity.class);
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

            android.app.AlertDialog alert11 = builder1.create();
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
            File root = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name_export) + "/PDFS/");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//                String path = gpxfile.toURI();;
                root = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name_export) + "/PDFS/");
//                root = new File(ApplicationData.getInstance().mMainContext.getExternalFilesDir("") + "/Rakshak/RakshakPDF/");
                root.mkdirs();
            } else {
                root.mkdirs();
            }
            String name;

            Calendar calendar = Calendar.getInstance();
            Date newDate = calendar.getTime();
            long timeInMilliseconds = newDate.getTime();

            name = "" + timeInMilliseconds;


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
                PdfPTable table3 = new PdfPTable(10);
                table3.setWidthPercentage(80f);
                insertCell(table3, "Sr No.", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Date", Element.ALIGN_CENTER, 3, bfBold12);
                insertCell(table3, "Payment Type", Element.ALIGN_CENTER, 2, bfBold12);
                insertCell(table3, "Amount", Element.ALIGN_CENTER, 2, bfBold12);
                insertCell(table3, "Cheque No.", Element.ALIGN_CENTER, 2, bfBold12);
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
                    insertCell(table3, VegetablesList.get(x).getPayment_Date(), Element.ALIGN_CENTER, 3, bf12);
                    insertCell(table3, VegetablesList.get(x).getPayment_Type(), Element.ALIGN_CENTER, 2, bf12);
                    insertCell(table3, VegetablesList.get(x).getPayment_Amount(), Element.ALIGN_CENTER, 2, bf12);
                    insertCell(table3, VegetablesList.get(x).getCheque_No(), Element.ALIGN_CENTER, 2, bf12);
//                    try {
//                        double vegetablePrice, vegetableWeight, totalAmount;
//                        vegetablePrice = Double.parseDouble(VegetablesList.get(x).getUser_Price().toString());
//                        vegetableWeight = Double.parseDouble(VegetablesList.get(x).getWeight().toString());
//                        totalAmount = vegetablePrice * vegetableWeight;
//                        insertCell(table3, money.format(totalAmount), Element.ALIGN_CENTER, 1, bf12);
//                    } catch (NumberFormatException e) {
//
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

//                PdfPTable tableTotal = new PdfPTable(6);
//                tableTotal.setWidthPercentage(80);
//                insertCell(tableTotal, "Total", Element.ALIGN_CENTER, 5, fontTotalBold);
//                insertCell(tableTotal, "" + Total_Amount, Element.ALIGN_CENTER, 1, fontTotalBold);
//                tableTotal.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//                document.add(tableTotal);

                PdfPTable table1212 = new PdfPTable(1);
                table1212.setWidthPercentage(80);
                table1212.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                PdfPCell cell2211 = new PdfPCell(new Phrase("" + getString(R.string.document_generated_from), fontsmall));
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
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("" + getString(R.string.app_name_pdf), fontCompanyName), 300, 783, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("" + getString(R.string.shop_addr_pdf)), 300, 770, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("" + getString(R.string.mob_no_pdf)), 300, 757, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Time: " + strCurrTime + "   Date: " + AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit)), 300, 744, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Hotel Name: " + strUserName), 300, 731, 0);
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

    private String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


}