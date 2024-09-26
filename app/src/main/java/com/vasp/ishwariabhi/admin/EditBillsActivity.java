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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.AppUtils;
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
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditBillsActivity extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id = "", strDateSubmit, strUserName, Order_Id, Order_Status, strTotal, strSubTotal, strDelivery, Delivery_Amount;
    long dateMiliseconds;
    double UserTotal, SubTotal, Delivery, VendorTotal;
    public static DecimalFormat money = new DecimalFormat("0.00");
    EditText edtSearch, edtDelivery;
    TextView tvNoData, tvSubTotal, tvTotal, tvDelivery, tvOrderDate;
    private ArrayList<String> arrVeggiesList = new ArrayList<>();
    LinearLayout lvUpdateOrder, lvBack;
    CardView cvAddVegetables, cvConfirm, cvCancel;
    BottomSheetDialog bottomSheetDialog;
    ArrayList<String> arrWeightList = new ArrayList<>();
    ArrayList<String> arrUserPriceList = new ArrayList<>();
    ArrayList<String> arrVendorPriceList = new ArrayList<>();
    ArrayList<String> arrNotFoundList = new ArrayList<>();
    CheckBox cbCheckBox;
    Spinner spnUsers;
    private List<CommonPojo> UsersList = new ArrayList<>();
    List<String> arrUsersId = new ArrayList<String>();
    List<String> arrUsersName = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bills);

        Intent i = getIntent();
        Order_Id = i.getStringExtra("Order_Id");
        Order_Status = i.getStringExtra("Order_Status");

        userSession = new UserSession(EditBillsActivity.this);
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
        spnUsers = findViewById(R.id.spnUsers);
        tvOrderDate = findViewById(R.id.tvOrderDate);

        bottomSheetDialog = new BottomSheetDialog(EditBillsActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_edit_order);
        bottomSheetDialog.setCanceledOnTouchOutside(false);


        tvSubTotal = bottomSheetDialog.findViewById(R.id.tvSubTotal);
        tvTotal = bottomSheetDialog.findViewById(R.id.tvTotal);
        tvDelivery = bottomSheetDialog.findViewById(R.id.tvDelivery);
        cvConfirm = bottomSheetDialog.findViewById(R.id.cvConfirm);
        cvCancel = bottomSheetDialog.findViewById(R.id.cvCancel);

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

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.equals("") || charSequence == null) {

                } else {
                    vegetablesListAdapter.getFilter().filter(charSequence);
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
                    getVegetables();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        cvAddVegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!User_Id.equals("")) {
                Intent i = new Intent(EditBillsActivity.this, AddVegetableInEditOrderActivity.class);
                i.putExtra("Order_Id", Order_Id);
                i.putExtra("User_Id", User_Id);
                startActivityForResult(i, 9282);
                } else {
                    Toast.makeText(EditBillsActivity.this, "Select Hotel!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        lvUpdateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!User_Id.equals("")) {
                    if (cbCheckBox.isChecked() && edtDelivery.getText().toString().equals("")) {
                        Toast.makeText(EditBillsActivity.this, "Enter Delivery Amount.", Toast.LENGTH_SHORT).show();
                    } else {
                        strDelivery = edtDelivery.getText().toString();
                        new AlertDialog.Builder(EditBillsActivity.this)
                                .setTitle("Update Order?")
                                .setMessage("Continue to update your order?")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

//                                    updateOrder(String.valueOf(dateMiliseconds), strSubsList);
                                        arrWeightList = new ArrayList<>();
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
                                            String vendorprice = VegetablesList.get(i).getVendor_Price();
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
                                                arrWeightList.add(VegetablesList.get(i).getDetail_Id() + "-" + qty);
                                            }
                                            if (!price.equals("")) {
                                                arrUserPriceList.add(VegetablesList.get(i).getDetail_Id() + "-" + price);
                                            }
                                            if (vendorprice != null) {
                                                arrVendorPriceList.add(VegetablesList.get(i).getDetail_Id() + "-" + vendorprice);
                                            }
                                            if (price.equals("")||qty.equals("")||vendorprice == null){
                                                arrNotFoundList.add(VegetablesList.get(i).getVegetable_Name());
                                            }
                                        }
                                        UserTotal = SubTotal + Delivery;
                                        tvSubTotal.setText("₹" + money.format(SubTotal));
                                        tvDelivery.setText("+ ₹" + money.format(Delivery));
                                        tvTotal.setText("₹" + money.format(UserTotal));
                                        bottomSheetDialog.show();
                                    }
                                }).create().show();
                    }
                } else {
                    Toast.makeText(EditBillsActivity.this, "Select Hotel!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cvConfirm.setOnClickListener(new View.OnClickListener() {
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
                    updateOrder("Accepted", strWeightList, strUserPriceList, strVendorPriceList);
                } else {
                    Toast.makeText(EditBillsActivity.this, arrNotFoundList.get(0) +" Details are Empty", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            }
        });

        cvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        tvOrderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(EditBillsActivity.this);
            }
        });

        tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit));

        getUsers();
    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(EditBillsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetOrderDetailsUserWise(User_Id, String.valueOf(dateMiliseconds));

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;
                    Delivery_Amount = jsonResponse.Delivery_Amount;
                    Order_Id = jsonResponse.Order_Id;

                    if (success.equals("1")) {

                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getSubDetails()));
                        arrVeggiesList = new ArrayList<>();

                        if (VegetablesList.size() > 0) {
                            rcvVegetables.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, EditBillsActivity.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(EditBillsActivity.this));

                            rcvVegetables.setAdapter(vegetablesListAdapter);

                            if (!Delivery_Amount.equals("0.0")) {
                                cbCheckBox.setChecked(true);
                                edtDelivery.setText(Delivery_Amount);
                            } else {
                                cbCheckBox.setChecked(false);
                                edtDelivery.setText("");
                            }

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
                            Toast.makeText(EditBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(EditBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(EditBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(EditBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(EditBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void updateOrder(String Status, String strWeightList, String strUserPriceList, String strVendorPriceList) {

        progressDialog = ProgressDialog.show(EditBillsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.UpdateOrder(Order_Id, Status, String.valueOf(money.format(SubTotal)), String.valueOf(money.format(VendorTotal)),
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

                        new AlertDialog.Builder(EditBillsActivity.this)
                                .setTitle("Successfully Updated!")
                                .setCancelable(false)
                                .setMessage("Order No. " + Order_Id + " is Updated Successfully!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

//                                        Intent i = new Intent();
//                                        setResult(9282, i);
//                                        finish();

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
                            Toast.makeText(EditBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(EditBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(EditBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(EditBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(EditBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
                    new AlertDialog.Builder(EditBillsActivity.this)
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
                if (VegetablesList.getUser_Price() != null) {
                    vegetablePrice = Double.parseDouble(VegetablesList.getUser_Price().toString());
                    vendorPrice = Double.parseDouble(VegetablesList.getVendor_Price().toString());
                    vegetableWeight = Double.parseDouble(VegetablesList.getWeight().toString());
                    totalAmount = vegetablePrice * vegetableWeight;
                    vendorAmount = vendorPrice * vegetableWeight;
                    holder.edtPrice.setText(VegetablesList.getUser_Price());
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
                                vendorPrice = Double.parseDouble(VegetablesList.getVendor_Price().toString());
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

    private void updateOrder(String strOrderTime, String strOrderDetails) {

        progressDialog = ProgressDialog.show(EditBillsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.EditOrderByUser(strOrderDetails);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {
                        new AlertDialog.Builder(EditBillsActivity.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Order Updated Successfully!!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        getVegetables();

                                    }
                                }).create().show();


                    } else {
                        Toast.makeText(EditBillsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(EditBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(EditBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(EditBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(EditBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(EditBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void deleteVegetables(String strDetailId, String strVegetableName) {

        progressDialog = ProgressDialog.show(EditBillsActivity.this, "", "Please Wait...", true, false);


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

                        Toast.makeText(EditBillsActivity.this, strVegetableName + " deleted from order.", Toast.LENGTH_SHORT).show();
                        getVegetables();
                        new AlertDialog.Builder(EditBillsActivity.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Vegetable Deleted Successfully!! Update Order to update the total!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                    }
                                }).create().show();


                    } else {
                        Toast.makeText(EditBillsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(EditBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(EditBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(EditBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(EditBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(EditBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void getUsers() {

        progressDialog = ProgressDialog.show(EditBillsActivity.this, "", "Please Wait...", true, false);

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
        Call<ReturnedResponsePojo> mService = mApiService.GetHotelsForReceipt(String.valueOf(dateMiliseconds));

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    UsersList = new ArrayList<>(Arrays.asList(jsonResponse.getHotels()));

                    if (success.equals("1")) {
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + UsersList.size());
                        populateSpinner();
//                        if (UsersList.size() > 0) {
//
//                        } else {
//                            Toast.makeText(EditBillsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        populateSpinner();
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + UsersList.size());
                        Toast.makeText(EditBillsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(EditBillsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(EditBillsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(EditBillsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(EditBillsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(EditBillsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

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
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit));
                getUsers();


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

        arrUsersId.add("Select Hotel");
        arrUsersName.add("Select Hotel");

        for (int i = 0; i < UsersList.size(); i++) {
            arrUsersName.add(UsersList.get(i).getMgmt_Name());
        }

        for (int i = 0; i < UsersList.size(); i++) {
            arrUsersId.add(UsersList.get(i).getUser_Id());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arrUsersName);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnUsers.setAdapter(spinnerAdapter);

    }


    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9282) {
            getVegetables();
        }
    }
}