package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.AppUtils;
import com.vasp.ishwariabhi.BluetoothDataService;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.CommonPojo;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;
import com.vasp.ishwariabhi.session.UserSession;

import java.io.IOException;
import java.text.DecimalFormat;
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

public class OrderDetailsNotificationActvity extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id, strDateSubmit, Order_Id, Order_Status, strUserName, strCurrTime, strDelivery, strVendorTotal;
    long dateMiliseconds;
    double UserTotal, SubTotal, Delivery, VendorTotal;
    public static DecimalFormat money = new DecimalFormat("0.00");
    EditText edtSearch, edtDelivery;
    TextView tvNoData, tvSubTotal, tvTotal, tvDelivery,tvTitle;
    private ArrayList<String> arrVeggiesList = new ArrayList<>();
    LinearLayout lvUpdateOrder, lvBack;
    CardView cvAddVegetables, cvAccept, cvReject;
    BottomSheetDialog bottomSheetDialog;
    ArrayList<String> arrWeightList = new ArrayList<>();
    ArrayList<String> arrWeightListPrint = new ArrayList<>();
    ArrayList<String> arrUserPriceList = new ArrayList<>();
    ArrayList<String> arrUserPriceListPrint = new ArrayList<>();
    ArrayList<String> arrVendorPriceList = new ArrayList<>();
    ArrayList<String> arrNotFoundList = new ArrayList<>();
    CheckBox cbCheckBox;
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    ImageView imgSearch;
    private ArrayList<String> arrSearchVeggiesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_admin_actvity);

        userSession = new UserSession(getApplicationContext());

        Intent i = getIntent();
        Order_Id = i.getStringExtra("Order_Id");
        strUserName = i.getStringExtra("User_Name");
        User_Id = i.getStringExtra("User_Id");
        Log.e(TAG, "onCreate: Order_Id "+Order_Id );
        Log.e(TAG, "onCreate: User_Name "+strUserName );
        Log.e(TAG, "onCreate: User_Id "+User_Id );

//        userSession = new UserSession(OrderDetailsAdminActvity.this);
//        User_Id = userSession.getUserId();
        Log.e("TAG", "onCreateView: User_Id" + User_Id);
        rcvVegetables = findViewById(R.id.rcvVegetables);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvNoData = findViewById(R.id.tvNoData);
        lvUpdateOrder = findViewById(R.id.lvUpdateOrder);
        lvBack = findViewById(R.id.lvBack);
        cvAddVegetables = findViewById(R.id.cvAddVegetables);
        cbCheckBox = findViewById(R.id.cbCheckBox);
        edtDelivery = findViewById(R.id.edtDelivery);
        imgSearch = findViewById(R.id.imgSearch);
        tvTitle = findViewById(R.id.tvTitle);

        tvTitle.setText(""+strUserName);

        bottomSheetDialog = new BottomSheetDialog(OrderDetailsNotificationActvity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_place_order);
        bottomSheetDialog.setCanceledOnTouchOutside(false);


        tvSubTotal = bottomSheetDialog.findViewById(R.id.tvSubTotal);
        tvTotal = bottomSheetDialog.findViewById(R.id.tvTotal);
        tvDelivery = bottomSheetDialog.findViewById(R.id.tvDelivery);
        cvAccept = bottomSheetDialog.findViewById(R.id.cvAccept);
        cvReject = bottomSheetDialog.findViewById(R.id.cvReject);

        cbCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtDelivery.setEnabled(true);
                } else {
                    edtDelivery.setEnabled(false);
                }

            }
        });

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        Calendar cal = Calendar.getInstance();
        strDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        strCurrTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdf.parse(strDateSubmit);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            dateMiliseconds = timeInMilliseconds;
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

        cvAddVegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OrderDetailsNotificationActvity.this, AddVegetableInOrderActivity.class);
                i.putExtra("Order_Id", Order_Id);
                i.putExtra("User_Id", User_Id);
                startActivityForResult(i, 9282);
            }
        });


        lvUpdateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbCheckBox.isChecked() && edtDelivery.getText().toString().equals("")) {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "Enter Delivery Amount.", Toast.LENGTH_SHORT).show();
                } else {
                    strDelivery = edtDelivery.getText().toString();
                    new AlertDialog.Builder(OrderDetailsNotificationActvity.this)
                            .setTitle("Confirm Order?")
                            .setMessage("Continue to confirm your order?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

//                                    updateOrder(String.valueOf(dateMiliseconds), strSubsList);
                                    arrWeightListPrint = new ArrayList<>();
                                    arrWeightList = new ArrayList<>();
                                    arrUserPriceListPrint = new ArrayList<>();
                                    arrUserPriceList = new ArrayList<>();
                                    arrVendorPriceList = new ArrayList<>();
                                    arrNotFoundList = new ArrayList<>();
                                    SubTotal = 0;
                                    VendorTotal = 0;
                                    UserTotal = 0;
                                    if (strDelivery.equals("")) {
                                        Delivery = 0;
                                    } else {
                                        Delivery = Double.parseDouble(strDelivery);
                                    }
                                    for (int i = 0; i < VegetablesList.size(); i++) {
                                        View view = rcvVegetables.getChildAt(i);
                                        EditText edtPrice = (EditText) view.findViewById(R.id.edtPrice);
                                        EditText edtQty = (EditText) view.findViewById(R.id.edtQuantity);
                                        TextView tvVendorTotal = (TextView) view.findViewById(R.id.tvVendorAmount);
                                        TextView tvUserTotal = (TextView) view.findViewById(R.id.tvTotalAmount);
                                        String price = edtPrice.getText().toString();
                                        String vendorprice = VegetablesList.get(i).getBuying_Price();
                                        String qty = edtQty.getText().toString();
                                        String vendorTotal = tvVendorTotal.getText().toString();
                                        String userTotal = tvUserTotal.getText().toString();

                                        if (!vendorTotal.equals("")) {
                                            vendorTotal = vendorTotal.replace("₹", "");
                                            VendorTotal = VendorTotal + Double.parseDouble(vendorTotal);
                                        }
                                        if (!userTotal.equals("")) {
                                            userTotal = userTotal.replace("₹", "");
                                            SubTotal += Double.parseDouble(userTotal);
                                        }
                                        if (!qty.equals("")) {
                                            arrWeightList.add(VegetablesList.get(i).getVegetable_Id() + "-" + qty);
                                            arrWeightListPrint.add("" + qty);
                                        }
                                        if (!price.equals("")) {
                                            arrUserPriceList.add(VegetablesList.get(i).getVegetable_Id() + "-" + price);
                                            arrUserPriceListPrint.add("" + price);
                                        }
                                        if (vendorprice != null) {
                                            arrVendorPriceList.add(VegetablesList.get(i).getVegetable_Id() + "-" + vendorprice);
                                        }
                                        if (price.equals("")||qty.equals("")||vendorprice == null){
                                            arrNotFoundList.add(VegetablesList.get(i).getVegetable_Name());
                                        }
                                        Log.e(TAG, "onClick: ORDER_DETAILS"+VendorTotal+" "+SubTotal+" "+qty+" "+price+ " "+vendorprice+" " +price+" ");
                                    }
                                    UserTotal = SubTotal + Delivery;
                                    tvSubTotal.setText("₹" + money.format(SubTotal));
                                    tvDelivery.setText("+ ₹" + money.format(Delivery));
                                    tvTotal.setText("₹" + money.format(UserTotal));
                                    bottomSheetDialog.show();
                                }
                            }).create().show();
                }
            }
        });

        cvAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VegetablesList.size() == arrUserPriceList.size() && VegetablesList.size() == arrVendorPriceList.size() && VegetablesList.size() == arrWeightList.size()) {
                    String strWeightList, strUserPriceList, strVendorPriceList;
                    strWeightList = String.join(",", arrWeightList);
                    strUserPriceList = String.join(",", arrUserPriceList);
                    strVendorPriceList = String.join(",", arrVendorPriceList);
                    Log.e("TAG", "onClick: Weight_List " + strWeightList + " User_Price_List " + strUserPriceList + " Vendor_Price_List " + strVendorPriceList);
                    Log.e("TAG", "onClick: SUB_TOTAL " + SubTotal + " VENDOR_TOTAL " + VendorTotal + " DELIVERY_CHARGES " + Delivery);
                    bottomSheetDialog.dismiss();
                    confirmOrder("Accepted", strWeightList, strUserPriceList, strVendorPriceList);
                } else {
                    Toast.makeText(OrderDetailsNotificationActvity.this, arrNotFoundList.get(0) +" Details are Empty", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            }
        });

        cvReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                confirmOrder("Rejected", "0-0", "0-0", "0-0");
            }
        });

        getVegetables();
    }

    private void performSearch() {
        String strSearchedValue = edtSearch.getText().toString();
        if (!strSearchedValue.equals("")) {
            arrSearchVeggiesList = new ArrayList<>();
            for (int i = 0; i < VegetablesList.size(); i++) {
                View viewrcv = rcvVegetables.getChildAt(i);
                TextView tvVegetableName = (TextView) viewrcv.findViewById(R.id.tvVegetableName);
                EditText edtQty = (EditText) viewrcv.findViewById(R.id.edtQuantity);
                String stVegetableName = tvVegetableName.getText().toString();

                if (stVegetableName.toString().toLowerCase().contains(strSearchedValue.toString().toLowerCase())) {
                    edtQty.requestFocus();
                    arrSearchVeggiesList.add(strSearchedValue);
                }
            }
            if (arrSearchVeggiesList.size()<1){
                Toast.makeText(OrderDetailsNotificationActvity.this, "Vegetable Not Found", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(OrderDetailsNotificationActvity.this, "Enter Vegetable Name", Toast.LENGTH_SHORT).show();
        }
    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(OrderDetailsNotificationActvity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetOrderHistoryAdmin(Order_Id);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getSubDetails()));
                        arrVeggiesList = new ArrayList<>();

                        if (VegetablesList.size() > 0) {
                            rcvVegetables.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, OrderDetailsNotificationActvity.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(OrderDetailsNotificationActvity.this));

                            rcvVegetables.setAdapter(vegetablesListAdapter);

                        } else {
                            rcvVegetables.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);

                        }
                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void confirmOrder(String Status, String strWeightList, String strUserPriceList, String strVendorPriceList) {

        progressDialog = ProgressDialog.show(OrderDetailsNotificationActvity.this, "", "Please Wait...", true, false);


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
        String abc = String.valueOf(SubTotal);
        Call<ReturnedResponsePojo> mService = mApiService.ConfirmOrder(Order_Id, Status, String.valueOf(money.format(SubTotal)), String.valueOf(money.format(VendorTotal)),
                String.valueOf(Delivery), String.valueOf(money.format(UserTotal)), strWeightList, strUserPriceList, strVendorPriceList);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        if (Status.equals("Accepted")){
                            new AlertDialog.Builder(OrderDetailsNotificationActvity.this)
                                    .setTitle("Successfully " + Status + "!")
                                    .setCancelable(false)
                                    .setMessage("Order No. " + Order_Id + " is " + Status + " Successfully!")
                                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent();
                                            setResult(9282, intent);
                                            finish();
                                        }
                                    })
                                    .setPositiveButton("PRINT", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface arg0, int arg1) {

                                            printCustomReciept();

                                        }
                                    }).create().show();
                        }else {
                            new AlertDialog.Builder(OrderDetailsNotificationActvity.this)
                                    .setTitle("Successfully " + Status + "!")
                                    .setCancelable(false)
                                    .setMessage("Order No. " + Order_Id + " is " + Status + " Successfully!")
                                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent();
                                            setResult(9282, intent);
                                            finish();
                                        }
                                    }).create().show();
                        }

                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_order_history_row, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo VegetablesList = searchMeetingList.get(i);

            holder.tvVegetableName.setText(VegetablesList.getVegetable_Name());
            holder.edtQuantity.setText(VegetablesList.getWeight());


            holder.cvMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(OrderDetailsNotificationActvity.this)
                            .setTitle("Really Delete?")
                            .setMessage("Are you sure you want to Delete " + VegetablesList.getVegetable_Name() + " from Order?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    deleteVegetables(VegetablesList.getDetail_Id(), VegetablesList.getVegetable_Name());

                                }
                            }).create().show();
                    return true;
                }
            });

            cvAddVegetables.setVisibility(View.VISIBLE);
            lvUpdateOrder.setVisibility(View.VISIBLE);

            try {
                double vegetablePrice, vegetableWeight, totalAmount, vendorAmount, vendorPrice;
                if (VegetablesList.getSelling_Price() != null) {
                    vegetablePrice = Double.parseDouble(VegetablesList.getSelling_Price().toString());
                    vendorPrice = Double.parseDouble(VegetablesList.getBuying_Price().toString());
                    vegetableWeight = Double.parseDouble(VegetablesList.getWeight().toString());
                    totalAmount = vegetablePrice * vegetableWeight;
                    vendorAmount = vendorPrice * vegetableWeight;
                    holder.edtPrice.setText(VegetablesList.getSelling_Price());
                    holder.tvTotalAmount.setText("₹" + money.format(totalAmount));
                    holder.tvVendorAmount.setText("₹" + money.format(vendorAmount));
                } else {
                    holder.edtPrice.setText("0");
                    vegetableWeight = Double.parseDouble(VegetablesList.getWeight().toString());
                    totalAmount = 0 * vegetableWeight;
                    vendorAmount = 0 * vegetableWeight;
                    holder.tvTotalAmount.setText("₹" + totalAmount);
                    holder.tvVendorAmount.setText("₹" + vendorAmount);
                }
            } catch (Exception e) {

            }

            holder.edtQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (s.length() != 0) {
                            double vegetablePrice, vegetableWeight, totalAmount, vendorAmount, vendorPrice;
                            if (!holder.edtPrice.getText().toString().equals("")) {
                                vegetablePrice = Double.parseDouble(holder.edtPrice.getText().toString());
                                vendorPrice = Double.parseDouble(VegetablesList.getBuying_Price().toString());
                                vegetableWeight = Double.parseDouble(holder.edtQuantity.getText().toString());
                                totalAmount = vegetablePrice * vegetableWeight;
                                vendorAmount = vendorPrice * vegetableWeight;
                                holder.tvTotalAmount.setText("₹" + money.format(totalAmount));
                                holder.tvVendorAmount.setText("₹" + money.format(vendorAmount));
                            }
                        } else {
                            holder.tvTotalAmount.setText("₹" + 0.0);
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            });

            holder.edtPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    try {
                        if (s.length() != 0) {
                            double vegetablePrice, vegetableWeight, totalAmount;
                            if (!holder.edtQuantity.getText().toString().equals("")) {
                                vegetablePrice = Double.parseDouble(holder.edtPrice.getText().toString());
                                vegetableWeight = Double.parseDouble(holder.edtQuantity.getText().toString());
                                totalAmount = vegetablePrice * vegetableWeight;
                                holder.tvTotalAmount.setText("₹" + money.format(totalAmount));
                            }
                        } else {
                            holder.tvTotalAmount.setText("₹" + 0.0);
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            });

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

            TextView tvVegetableName, tvTotalAmount, tvVendorAmount;
            CardView cvMain;
            EditText edtQuantity, edtPrice;
            ImageView imgVegetable;


            public MyViewHolder(View view) {
                super(view);
                tvVegetableName = view.findViewById(R.id.tvVegetableName);
                imgVegetable = view.findViewById(R.id.imgVegetable);
                cvMain = view.findViewById(R.id.cvMain);
                edtQuantity = view.findViewById(R.id.edtQuantity);
                edtPrice = view.findViewById(R.id.edtPrice);
                tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
                tvVendorAmount = view.findViewById(R.id.tvVendorAmount);
            }
        }
    }

    private void deleteVegetables(String strDetailId, String strVegetableName) {

        progressDialog = ProgressDialog.show(OrderDetailsNotificationActvity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.DeleteVegetableFromOrder(strDetailId);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        Toast.makeText(OrderDetailsNotificationActvity.this, strVegetableName + " deleted from order.", Toast.LENGTH_SHORT).show();
                        getVegetables();

                    } else {
                        Toast.makeText(OrderDetailsNotificationActvity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(OrderDetailsNotificationActvity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(OrderDetailsNotificationActvity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9282) {
            getVegetables();
        }
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
                    beginPrinting();
                }
            }
        } else {
            beginPrinting();
        }
    }

    private void beginPrinting() {

        String billNoPrintReciept;
        billNoPrintReciept = Order_Id;

        String BILL = "                   VEGETABLES\n\n\n\n" +

                "                INVOICE/CASH MEMO    \n"
                + getString(R.string.app_name_print) +
                ""+getString(R.string.shop_addr_print) +
                ""+getString(R.string.mob_no_print) +
                "  Bill No. " + billNoPrintReciept + "   Time: " + strCurrTime + "   Date: " + AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit) + "        \n" +
                "  Name: " + strUserName + "   \n";
        BILL = BILL
                + "-----------------------------------------------\n";


        BILL = BILL + String.format("%1$-7s %2$-11s %3$5s %4$9s %5$10s", "SrNo.", "Item", "Qty", "Rate", "Total");
        BILL = BILL + "\n";
        BILL = BILL
                + "-----------------------------------------------";
//                    }


        for (int x = 0; x < VegetablesList.size(); x++) {
            try {
                Log.e(TAG, "beginPrinting: VEGETABLE_LIST" + arrUserPriceListPrint.get(x) + " " + arrWeightListPrint.get(x));
                double vegetablePrice, vegetableWeight, totalAmount;
                vegetablePrice = Double.parseDouble(arrUserPriceListPrint.get(x).toString());
                vegetableWeight = Double.parseDouble(arrWeightListPrint.get(x).toString());
                totalAmount = vegetablePrice * vegetableWeight;
                BILL = BILL + "\n " + String.format("%1$-5s %2$-14s %3$-8s %4$-8s %5$-6s", x + 1 + "", VegetablesList.get(x).getVegetable_Name(), arrWeightListPrint.get(x), arrUserPriceListPrint.get(x), money.format(totalAmount));
            } catch (NumberFormatException e) {

            }

        }

        BILL = BILL
                + "\n-----------------------------------------------";
        BILL = BILL + "\n\n ";

        BILL = BILL + "                Sub Total:" + "     Rs. " + money.format(SubTotal) + "\n";
        BILL = BILL + "                  Delivery:" + "   + Rs. " + money.format(Delivery) + "\n";
        BILL = BILL
                + "-----------------------------------------------\n";
        BILL = BILL + "                     Total:" + "     Rs. " + money.format(UserTotal) + "\n";
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
        Intent intent = new Intent(OrderDetailsNotificationActvity.this, BluetoothDataService.class);
        intent.putExtra("MAC_ADDRESS", userSession.getPrinterAddress());
        intent.putExtra("BILL", BILL);
        startService(intent);

        Intent intent2 = new Intent();
        setResult(9282, intent2);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(OrderDetailsNotificationActvity.this, AdminDashboard.class);
        startActivityForResult(i, 9282);
        finish();
    }
}