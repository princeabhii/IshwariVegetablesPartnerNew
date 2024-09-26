package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ViewVendorPriceVegetableWise extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id = "", strDateSubmit, strVegetableId, strVegetableAtt, strVegetableName, strWeightTotal,
            strCurrTime, strCurrDate,strVendorTotal;
    long dateMiliseconds;
    TextView tvNoData, tvQuantity, tvVegetableName, tvTitle;
    EditText edtSearch;
    LinearLayout ly_back;
    ImageView imgVegetable;
    CardView cvSearch, cvAddVendor;
    public static DecimalFormat money = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vendor_price_vegetable_wise);

        userSession = new UserSession(ViewVendorPriceVegetableWise.this);
        Intent i = getIntent();
//        User_Id = i.getStringExtra("User_Id");
        dateMiliseconds = i.getLongExtra("Date", 0);
        strVegetableId = i.getStringExtra("Vegetable_Id");
        strVegetableName = i.getStringExtra("Vegetable_Name");
        strWeightTotal = i.getStringExtra("Total_Weight");
        rcvVegetables = findViewById(R.id.rcvVegetables);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        ly_back = findViewById(R.id.ly_back);
        tvNoData = findViewById(R.id.tvNoData);
        cvSearch = findViewById(R.id.cvSearch);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvVegetableName = findViewById(R.id.tvVegetableName);
        imgVegetable = findViewById(R.id.imgVegetable);
        tvTitle = findViewById(R.id.tvTitle);
        cvAddVendor = findViewById(R.id.cvAddVendor);

        Calendar cal = Calendar.getInstance();
        strDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

        strCurrDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        strCurrTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        tvQuantity.setText("(" + strWeightTotal + " KG)");

        tvVegetableName.setText(strVegetableName);
        tvTitle.setText(strVegetableName);

        ly_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cvAddVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ViewVendorPriceVegetableWise.this, AddVendorPriceVegetableWise.class);
                i.putExtra("Date", dateMiliseconds);
                i.putExtra("Vegetable_Id", strVegetableId);
                i.putExtra("Vegetable_Name", strVegetableName);
                i.putExtra("Total_Weight", strWeightTotal);
                startActivity(i);
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
                    vegetablesListAdapter.getFilter().filter(charSequence);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getVegetables();
    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(ViewVendorPriceVegetableWise.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetVendorOrdersVegetableWise(String.valueOf(dateMiliseconds), strVegetableId);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {


                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getVendors()));

                        if (VegetablesList.size() > 0) {
                            rcvVegetables.setVisibility(View.VISIBLE);
                            cvAddVendor.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            cvSearch.setVisibility(View.GONE);
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, ViewVendorPriceVegetableWise.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(ViewVendorPriceVegetableWise.this));

                            rcvVegetables.setAdapter(vegetablesListAdapter);

                        } else {
                            rcvVegetables.setVisibility(View.GONE);
                            cvAddVendor.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);
                            cvSearch.setVisibility(View.GONE);

                        }
                    } else {
                        Toast.makeText(ViewVendorPriceVegetableWise.this, "No Data Found", Toast.LENGTH_SHORT).show();
                        rcvVegetables.setVisibility(View.GONE);
                        cvAddVendor.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        cvSearch.setVisibility(View.GONE);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ViewVendorPriceVegetableWise.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ViewVendorPriceVegetableWise.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ViewVendorPriceVegetableWise.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ViewVendorPriceVegetableWise.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ViewVendorPriceVegetableWise.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendor_prices_item, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo VegetablesList = searchMeetingList.get(i);

            holder.tvAddOrder.setText("UPDATE PRICE");
            holder.edtVendorPrice.setText(VegetablesList.getVendor_Price());
            holder.tvVendorTotal.setText("Rs. " + VegetablesList.getTotal_Amount());
            holder.edtWeight.setText(VegetablesList.getWeight());

            holder.tvVendorName.setText(VegetablesList.getVendor_Name());

            holder.edtVendorPrice.addTextChangedListener(new TextWatcher() {
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
                            double vendorPrice, vegetableWeight, totalAmount;
                            if (!holder.edtWeight.getText().toString().equals("")) {
                                vendorPrice = Double.parseDouble(holder.edtVendorPrice.getText().toString());
                                vegetableWeight = Double.parseDouble(holder.edtWeight.getText().toString());
                                totalAmount = vendorPrice * vegetableWeight;
                                holder.tvVendorTotal.setText("Rs. " + money.format(totalAmount));
                            }
                        } else {
                            holder.tvVendorTotal.setText("Rs. " + 0.0);
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            });

            holder.edtWeight.addTextChangedListener(new TextWatcher() {
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
                            double vendorPrice, vegetableWeight, totalAmount;
                            if (!holder.edtVendorPrice.getText().toString().equals("")) {
                                vendorPrice = Double.parseDouble(holder.edtVendorPrice.getText().toString());
                                vegetableWeight = Double.parseDouble(holder.edtWeight.getText().toString());
                                totalAmount = vendorPrice * vegetableWeight;
                                holder.tvVendorTotal.setText("Rs. " + money.format(totalAmount));
                            }
                        } else {
                            holder.tvVendorTotal.setText("Rs. " + 0.0);
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            });

            holder.lvAddOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    strVendorTotal = holder.tvVendorTotal.getText().toString();
                    String strWeight = holder.edtWeight.getText().toString();
                    String strVendorPrice = holder.edtVendorPrice.getText().toString();

                    if (strVendorPrice.equals("")){
                        Toast.makeText(context, "Enter Vendor Price", Toast.LENGTH_SHORT).show();
                    } else if (strWeight.equals("")) {
                        Toast.makeText(context, "Enter Quantity", Toast.LENGTH_SHORT).show();
                    } else if (strVendorTotal.equals("")) {
                        Toast.makeText(context, "Total Cant be empty", Toast.LENGTH_SHORT).show();
                    }else {
                        strVendorTotal = strVendorTotal.replace("Rs. ", "");
                        Log.e("TAG", "onClick: OUTPUT "+VegetablesList.getV_Order_Id()+"  "+VegetablesList.getVegetable_Id()+"  "+strWeight+"  "
                                +strVendorPrice+"  "+VegetablesList.getVendor_Id()+"  "+strVendorTotal+"  "+ String.valueOf(dateMiliseconds) );
                        new AlertDialog.Builder(ViewVendorPriceVegetableWise.this)
                                .setTitle("Update Prices?")
                                .setMessage("Continue to update prices?")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {
                                        setPrices(VegetablesList.getV_Order_Id(),VegetablesList.getVegetable_Id(),strWeight,strVendorPrice,VegetablesList.getVendor_Id(),
                                                strVendorTotal, String.valueOf(dateMiliseconds));
                                    }
                                }).create().show();
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

            TextView tvVendorName, tvVendorTotal, edtVendorPrice, edtWeight,tvAddOrder;
            LinearLayout lvAddOrder;

            public MyViewHolder(View view) {
                super(view);
                tvVendorName = view.findViewById(R.id.tvVendorName);
                tvVendorTotal = view.findViewById(R.id.tvVendorTotal);
                edtVendorPrice = view.findViewById(R.id.edtVendorPrice);
                edtWeight = view.findViewById(R.id.edtWeight);
                lvAddOrder = view.findViewById(R.id.lvAddOrder);
                tvAddOrder = view.findViewById(R.id.tvAddOrder);
            }
        }
    }

    private void setPrices(String V_Order_Id, String Vegetable_Id, String Weight, String Vendor_Price,
                           String Vendor_Id, String Total_Amount, String Added_On) {

        progressDialog = ProgressDialog.show(ViewVendorPriceVegetableWise.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.UpdateVendorOrder(V_Order_Id, Vegetable_Id, Weight,
                Vendor_Price, Vendor_Id, Total_Amount, Added_On);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        new AlertDialog.Builder(ViewVendorPriceVegetableWise.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Price Updated Successfully!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        getVegetables();

                                    }
                                }).create().show();

                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        Toast.makeText(ViewVendorPriceVegetableWise.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ViewVendorPriceVegetableWise.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ViewVendorPriceVegetableWise.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ViewVendorPriceVegetableWise.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ViewVendorPriceVegetableWise.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ViewVendorPriceVegetableWise.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "onFailure: ERROR_WHILE_INSERTING " + t);
                    // todo log to some central bug tracking service
                }
            }
        });

    }

}