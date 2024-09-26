package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.AppUtils;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.CommonPojo;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;
import com.vasp.ishwariabhi.pojo.SubCommonPojo;
import com.vasp.ishwariabhi.session.UserSession;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SetPricesActivty extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id, strDateSubmit;
    long dateMiliseconds, yesterdaydateMiliseconds;
    EditText edtSearch, edtSellPercentage;
    TextView tvNoData, tvOrderDate;
    LinearLayout lvUpdatePrices, lvBack;
    ArrayList<String> arrUserPriceList = new ArrayList<>();
    ArrayList<String> arrVendorOrderList = new ArrayList<>();
    ArrayList<String> arrVendorPriceList = new ArrayList<>();
    ArrayList<String> arrNotFoundList = new ArrayList<>();
    CardView cvSetSellPercentage;
    public static DecimalFormat money = new DecimalFormat("0.00");
    SwitchCompat switchSellPercent;
    boolean isSwitchChecked = false;
    ImageView imgSearch;
    private ArrayList<String> arrSearchVeggiesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_prices_activty);

        userSession = new UserSession(SetPricesActivty.this);
        User_Id = userSession.getUserId();
        Log.e("TAG", "onCreateView: User_Id" + User_Id);
        rcvVegetables = findViewById(R.id.rcvVegetables);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvNoData = findViewById(R.id.tvNoData);
        lvUpdatePrices = findViewById(R.id.lvUpdatePrices);
        lvBack = findViewById(R.id.lvBack);
        edtSellPercentage = findViewById(R.id.edtSellPercentage);
        cvSetSellPercentage = findViewById(R.id.cvSetSellPercentage);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        switchSellPercent = findViewById(R.id.switchSellPercent);
        imgSearch = findViewById(R.id.imgSearch);

        edtSellPercentage.setText("" + userSession.getSell_Percentage());
        if (userSession.getSell_PercentageStatus()) {
            switchSellPercent.setChecked(true);
            isSwitchChecked = true;
        } else {
            switchSellPercent.setChecked(false);
            isSwitchChecked = false;
        }

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        switchSellPercent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);
                if (isChecked) {
                    isSwitchChecked = true;
                } else {
                    userSession.setSell_PercentageStatus(false);
//                    userSession.setSell_Percentage(0);
                    isSwitchChecked = false;
                }
            }

        });

        cvSetSellPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSwitchChecked) {
                    String strSellPercentage = edtSellPercentage.getText().toString();
                    if (strSellPercentage.equals("")) {
                        Toast.makeText(SetPricesActivty.this, "Sell Percentage Cannot be Blank", Toast.LENGTH_SHORT).show();
                    } else {
                        int SellPercentage = Integer.parseInt(strSellPercentage);
                        userSession.setSell_Percentage(SellPercentage);
                        userSession.setSell_PercentageStatus(true);
                        Toast.makeText(SetPricesActivty.this, "Sell Percentage Set Successfully!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SetPricesActivty.this, "Enable Switch First!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Calendar cal = Calendar.getInstance();
        strDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdf.parse(strDateSubmit);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            dateMiliseconds = timeInMilliseconds;

            Date mDate2 = sdf.parse(strDateSubmit);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mDate2);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            Date newDate = calendar.getTime();
            long yesterdaytimeInMilliseconds = newDate.getTime();
            System.out.println("Date in milli :: " + yesterdaytimeInMilliseconds);
            yesterdaydateMiliseconds = yesterdaytimeInMilliseconds;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        edtSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.equals("") || charSequence == null) {
//
//                } else {
//                    vegetablesListAdapter.getFilter().filter(charSequence);
//                }
//
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch();
            }
        });

        lvUpdatePrices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                arrUserPriceList = new ArrayList<>();
                arrVendorPriceList = new ArrayList<>();
                arrVendorOrderList = new ArrayList<>();
                arrNotFoundList = new ArrayList<>();
                String strSpinnerValue = "";
                for (int i = 0; i < VegetablesList.size(); i++) {
                    View viewrcv = rcvVegetables.getChildAt(i);
                    EditText edtUserPrice = (EditText) viewrcv.findViewById(R.id.edtUserPrice);
                    EditText edtVendorPrice = (EditText) viewrcv.findViewById(R.id.edtVendorPrice);
                    EditText edtWeight = (EditText) viewrcv.findViewById(R.id.edtWeight);
                    Spinner spnVendorId = viewrcv.findViewById(R.id.spnVendors);
                    TextView tvSpinnerId = viewrcv.findViewById(R.id.tvSpinnerId);
                    TextView tvTotalAmount = viewrcv.findViewById(R.id.tvTotalAmount);
                    strSpinnerValue = tvSpinnerId.getText().toString();
                    String strUserPrice = edtUserPrice.getText().toString();
                    String strVendorPrice = edtVendorPrice.getText().toString();
                    String strWeight = edtWeight.getText().toString();
                    String vendorTotal = tvTotalAmount.getText().toString();
                    vendorTotal = vendorTotal.replace("₹", "");
//                    Log.e("TAG", "onClick: SPINNER_VALUE "+i+" "+strSpinnerValue );

//                    if (strUserPrice.equals("")) {
//                        arrUserPriceList.add(VegetablesList.get(i).getVegetable_Id() + "-" + strUserPrice);
                    arrUserPriceList.add(VegetablesList.get(i).getVegetable_Id() + "-" + "0");
//                    }
                    if (!strVendorPrice.equals("")) {
                        arrVendorPriceList.add(VegetablesList.get(i).getVegetable_Id() + "-" + strVendorPrice);
                    }
                    if (!strWeight.equals("") && !vendorTotal.equals("")) {
                        arrVendorOrderList.add(VegetablesList.get(i).getVegetable_Id() + "-" + strWeight + "-" + strVendorPrice + "-" + strSpinnerValue + "-" + vendorTotal);
                        Log.e("TAG", "onClick: VENDOR_ORDER" + VegetablesList.get(i).getVegetable_Id() + "-" + strWeight + "-" + strVendorPrice + "-" + strSpinnerValue + "-" + vendorTotal);
                    }
                    if (strVendorPrice.equals("") || strWeight.equals("") || vendorTotal.equals("")) {
                        arrNotFoundList.add(VegetablesList.get(i).getVegetable_Name());
                    }
                }

                if (VegetablesList.size() == arrUserPriceList.size() && VegetablesList.size() == arrVendorPriceList.size() && VegetablesList.size() == arrVendorOrderList.size()) {
                    String strUserPriceList, strVendorPriceList, strVendorOrderList;
                    strUserPriceList = String.join(",", arrUserPriceList);
                    strVendorPriceList = String.join(",", arrVendorPriceList);
                    strVendorOrderList = String.join(",", arrVendorOrderList);
                    Log.e("TAG", "onClick:" + " User_Price_List " + strUserPriceList + " Vendor_Price_List " + strVendorPriceList + " Vendor_Order_List " + strVendorOrderList);
                    new AlertDialog.Builder(SetPricesActivty.this)
                            .setTitle("Set Prices?")
                            .setMessage("Continue to set prices?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    setPrices(strUserPriceList, strVendorPriceList, strVendorOrderList);
                                }
                            }).create().show();

                } else {
                    Toast.makeText(SetPricesActivty.this, arrNotFoundList.get(0) +" Details are Empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvOrderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(SetPricesActivty.this);
            }
        });

        tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit));

        getVegetables();
    }

    private void performSearch() {
        String strSearchedValue = edtSearch.getText().toString();
        if (!strSearchedValue.equals("")) {
            arrSearchVeggiesList = new ArrayList<>();
            for (int i = 0; i < VegetablesList.size(); i++) {
                View viewrcv = rcvVegetables.getChildAt(i);
                TextView tvVegetableName = (TextView) viewrcv.findViewById(R.id.tvVegetableName);
                EditText edtQty = (EditText) viewrcv.findViewById(R.id.edtVendorPrice);
                String stVegetableName = tvVegetableName.getText().toString();

                if (stVegetableName.toString().toLowerCase().contains(strSearchedValue.toString().toLowerCase())) {
                    edtQty.requestFocus();
                    arrSearchVeggiesList.add(strSearchedValue);
                }
            }
            if (arrSearchVeggiesList.size()<1){
                Toast.makeText(SetPricesActivty.this, "Vegetable Not Found", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(SetPricesActivty.this, "Enter Vegetable Name", Toast.LENGTH_SHORT).show();
        }
    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(SetPricesActivty.this, "", "Please Wait...", true, false);


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
        Log.e("TAG", "getVegetables: TODAY_DATE " + dateMiliseconds + " YESTERDAY_DATE " + yesterdaydateMiliseconds);
        Call<ReturnedResponsePojo> mService = mApiService.GetDailyWeightAdmin(String.valueOf(dateMiliseconds), String.valueOf(yesterdaydateMiliseconds));

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getVegetables()));

                        if (VegetablesList.size() > 0) {
                            rcvVegetables.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, SetPricesActivty.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(SetPricesActivty.this));

                            rcvVegetables.setAdapter(vegetablesListAdapter);

                        } else {
                            rcvVegetables.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);

                        }
                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(SetPricesActivty.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(SetPricesActivty.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SetPricesActivty.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(SetPricesActivty.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(SetPricesActivty.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void setPrices(String strUserPriceList, String strVendorPriceList, String strVendorOrderList) {

        progressDialog = ProgressDialog.show(SetPricesActivty.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.SetVendorVegetablePrices(String.valueOf(dateMiliseconds), strUserPriceList, strVendorPriceList, strVendorOrderList);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        new AlertDialog.Builder(SetPricesActivty.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Prices Set Successfully!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        Intent i = new Intent();
                                        setResult(9282, i);
                                        finish();

                                    }
                                }).create().show();

                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(SetPricesActivty.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(SetPricesActivty.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SetPricesActivty.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(SetPricesActivty.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(SetPricesActivty.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "onFailure: ERROR_WHILE_INSERTING " + t);
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    public class VegetablesListAdapter extends RecyclerView.Adapter<VegetablesListAdapter.MyViewHolder> {

        private List<CommonPojo> Vegetables_List;
        private List<CommonPojo> searchMeetingList;
        private Context context;


        public VegetablesListAdapter(List<CommonPojo> Vegetables_List, Context context) {
            this.Vegetables_List = Vegetables_List;
            this.searchMeetingList = Vegetables_List;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_price_row, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo VegetablesList = searchMeetingList.get(i);

            holder.tvVegetableName.setText(VegetablesList.getVegetable_Name());
            double TotalWeight = Double.parseDouble(VegetablesList.getTotal_Weight());
            holder.tvWeight.setText(money.format(TotalWeight) + " KG");
            holder.edtWeight.setText(money.format(TotalWeight));
            holder.edtUserPrice.setText(VegetablesList.getUser_Price());
            holder.edtVendorPrice.setText(VegetablesList.getVendor_Price());

            if (!VegetablesList.getUser_Price().equals("") && !VegetablesList.getVendor_Price().equals("")) {
                double vegetablePrice, vegetableWeight, userAmount, vendorAmount, vendorPrice;
                int SellPercentage;
                if (!holder.edtWeight.getText().toString().equals("")) {
                    vegetablePrice = Double.parseDouble(String.valueOf(userSession.getSell_Percentage()));
//                                vegetablePrice = Double.parseDouble(String.valueOf(userSession.getSell_Percentage()));
                    vendorPrice = Double.parseDouble(holder.edtVendorPrice.getText().toString());
                    vegetableWeight = Double.parseDouble(holder.edtWeight.getText().toString());
                    vendorAmount = vendorPrice * vegetableWeight;
                    holder.tvTotalAmount.setText("₹" + money.format(vendorAmount));
                    if (isSwitchChecked) {
                        SellPercentage = userSession.getSell_Percentage();
                        userAmount = SellPercentage * vendorPrice / 100;
                        userAmount = userAmount + vendorPrice;
                        holder.edtUserPrice.setText(money.format(userAmount));
                    }
                } else {
//                            holder.tvTotalAmount.setText("₹" + 0.0);
                    Toast.makeText(context, "Set Quantity First", Toast.LENGTH_SHORT).show();
                }
            }

            holder.arrVendorsList = new ArrayList<>(Arrays.asList(VegetablesList.getVendors()));

            if (VegetablesList.getAttachment().equals("NA")) {
                Picasso.get()
                        .load(R.drawable.noimage)
                        .placeholder(R.drawable.noimage)
                        .into(holder.imgVegetable);
            } else {
                Picasso.get()
                        .load(VegetablesList.getAttachment()
                        )
                        .placeholder(R.drawable.noimage)
                        .into(holder.imgVegetable);

            }
            populateSpinnerVendors(holder.arrVendorName, holder.arrVendorId, holder.arrVendorsList, holder.spnVendors, VegetablesList.getVendor_Name());

            holder.spnVendors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

//                        if (i > 0) {
                    holder.strVendorName = adapterView.getItemAtPosition(i).toString();
                    holder.strVendor_Id = holder.arrVendorId.get(i);
                    holder.tvSpinnerId.setText(holder.strVendor_Id);
//                        }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            holder.edtVendorPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        if (editable.length() != 0) {
                            double vegetablePrice, vegetableWeight, userAmount, vendorAmount, vendorPrice;
                            int SellPercentage;
                            if (!holder.edtWeight.getText().toString().equals("")) {
                                vegetablePrice = Double.parseDouble(String.valueOf(userSession.getSell_Percentage()));
//                                vegetablePrice = Double.parseDouble(String.valueOf(userSession.getSell_Percentage()));
                                vendorPrice = Double.parseDouble(holder.edtVendorPrice.getText().toString());
                                vegetableWeight = Double.parseDouble(holder.edtWeight.getText().toString());
                                vendorAmount = vendorPrice * vegetableWeight;
                                holder.tvTotalAmount.setText("₹" + money.format(vendorAmount));
                                if (isSwitchChecked) {
                                    SellPercentage = userSession.getSell_Percentage();
                                    userAmount = SellPercentage * vendorPrice / 100;
                                    userAmount = userAmount + vendorPrice;
                                    holder.edtUserPrice.setText(money.format(userAmount));
                                }
                            } else {
//                            holder.tvTotalAmount.setText("₹" + 0.0);
                                Toast.makeText(context, "Set Quantity First", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
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

                            for (CommonPojo objectGallery : Vegetables_List) {


                                if (objectGallery.getVegetable_Name().toLowerCase().contains(charString)) {

                                    searchMeetingList.add(objectGallery);
                                }

                            }
                        } else {
                            searchMeetingList = Vegetables_List;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.e("gallerylist data", " " + Vegetables_List.size());
                    Log.e("searchlist data", " " + searchMeetingList.size());


                    FilterResults filterResults = new FilterResults();
                    filterResults.values = searchMeetingList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                    searchMeetingList = (List<CommonPojo>) filterResults.values;
                    // refresh the list with filtered data
                    rcvVegetables.getRecycledViewPool().clear();
                    vegetablesListAdapter.notifyDataSetChanged();
                }
            };
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvVegetableName, tvWeight, tvTotalAmount, tvSpinnerId;
            CardView cvMain;
            EditText edtUserPrice, edtVendorPrice, edtWeight;
            ImageView imgVegetable;
            Spinner spnVendors;
            ArrayList<String> arrVendorName = new ArrayList<String>();
            ArrayList<String> arrVendorId = new ArrayList<String>();
            String strVendor_Id, strVendorName;
            private ArrayList<SubCommonPojo> arrVendorsList = new ArrayList<>();


            public MyViewHolder(View view) {
                super(view);
                tvVegetableName = view.findViewById(R.id.tvVegetableName);
                tvWeight = view.findViewById(R.id.tvWeight);
                imgVegetable = view.findViewById(R.id.imgVegetable);
                cvMain = view.findViewById(R.id.cvMain);
                edtUserPrice = view.findViewById(R.id.edtUserPrice);
                edtVendorPrice = view.findViewById(R.id.edtVendorPrice);
                spnVendors = view.findViewById(R.id.spnVendors);
                edtWeight = view.findViewById(R.id.edtWeight);
                tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
                tvSpinnerId = view.findViewById(R.id.tvSpinnerId);
            }
        }
    }

    private void populateSpinnerVendors(ArrayList<String> arrVendorName, ArrayList<String> arrVendorId, ArrayList<SubCommonPojo> arrVendorsList, Spinner spnVendors, String strVendorName) {

        arrVendorName.clear();
        arrVendorId.clear();

//        arrVendorName.add("Select Vendor");
//        arrVendorId.add("Select Vendor");

        for (int i = 0; i < arrVendorsList.size(); i++) {
            arrVendorName.add(arrVendorsList.get(i).getVendor_Name());
        }

        for (int i = 0; i < arrVendorsList.size(); i++) {
            arrVendorId.add(arrVendorsList.get(i).getVendor_Id());
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arrVendorName);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnVendors.setAdapter(spinnerAdapter);

        if (strVendorName != null) {
            if (!strVendorName.equals("")) {
                selectSpinnerValue(spnVendors, strVendorName);
            }
        }
//        Log.e("TAG", "populateSpinnerDesig: DESIGNATION_RETURNED"+strVendorName );

    }

    private void selectDatePicker(Context context) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                strDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date mDate = sdf.parse(strDateSubmit);
                    long timeInMilliseconds = mDate.getTime();
                    System.out.println("Date in milli :: " + timeInMilliseconds);
                    dateMiliseconds = timeInMilliseconds;

                    Date mDate2 = sdf.parse(strDateSubmit);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mDate2);
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    Date newDate = calendar.getTime();
                    long yesterdaytimeInMilliseconds = newDate.getTime();
                    System.out.println("Date in milli :: " + yesterdaytimeInMilliseconds);
                    yesterdaydateMiliseconds = yesterdaytimeInMilliseconds;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit));
                getVegetables();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        long now = System.currentTimeMillis() - 1000;
        dlg.getDatePicker().setMaxDate(now + (1000 * 60 * 60 * 24));
//        dlg.getDatePicker().setMinDate(new Date().getTime());
        dlg.show();
    }

    private void selectSpinnerValue(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(myString)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}