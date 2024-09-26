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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
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
import java.text.DateFormat;
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

public class ManageOrdersActivity extends AppCompatActivity {

    RecyclerView rcvOrderDetails;
    private List<CommonPojo> OrderDetailsList = new ArrayList<>();
    OrderDetailsListAdapter orderDetailsListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id, strFromDateSubmit, strToDateSubmit;
    long orderDateMiliseconds;
    EditText edtSearch;
    TextView tvOrderDate, tvNoData;
    LinearLayout ly_back, lvAddOrders;
    CardView cvSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        userSession = new UserSession(ManageOrdersActivity.this);
        User_Id = userSession.getUserId();
        Log.e("TAG", "onCreateView: User_Id" + User_Id);
        rcvOrderDetails = findViewById(R.id.rcvOrderDetails);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvNoData = findViewById(R.id.tvNoData);
        ly_back = findViewById(R.id.ly_back);
        lvAddOrders = findViewById(R.id.lvAddOrders);
        cvSearch = findViewById(R.id.cvSearch);

        Calendar cal = Calendar.getInstance();
        strFromDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

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

        lvAddOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ManageOrdersActivity.this, AdminPlaceOrderActivity.class);
                startActivityForResult(i,9282);
            }
        });

        tvOrderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(ManageOrdersActivity.this);
            }
        });

        ly_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        strToDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mDate = sdf.parse(strToDateSubmit);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            orderDateMiliseconds = timeInMilliseconds;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
        getOrderDetails();
    }

    private void getOrderDetails() {

        progressDialog = ProgressDialog.show(ManageOrdersActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetPendingOrders(String.valueOf(orderDateMiliseconds));

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        OrderDetailsList = new ArrayList<>(Arrays.asList(jsonResponse.getOrderDetails()));

                        if (OrderDetailsList.size() > 0) {
                            rcvOrderDetails.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            cvSearch.setVisibility(View.VISIBLE);
                            orderDetailsListAdapter = new OrderDetailsListAdapter(OrderDetailsList, ManageOrdersActivity.this);
                            rcvOrderDetails.setLayoutManager(new LinearLayoutManager(ManageOrdersActivity.this));

                            rcvOrderDetails.setAdapter(orderDetailsListAdapter);

                        } else {
                            rcvOrderDetails.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);
                            cvSearch.setVisibility(View.GONE);

                        }
                    } else {
                        rcvOrderDetails.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        cvSearch.setVisibility(View.GONE);
                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ManageOrdersActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ManageOrdersActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ManageOrdersActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ManageOrdersActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ManageOrdersActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_order_list_admin, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo OrderDetailsList = searchMeetingList.get(i);

            holder.tvOrderNo.setText("Order No. " + OrderDetailsList.getOrder_Id());
            holder.tvOrderStatus.setText(OrderDetailsList.getOrder_Status());
            holder.tvAddedOn.setText(OrderDetailsList.getAdded_On());
            holder.tvItemCount.setText(OrderDetailsList.getTotal_Vegetable_Count() + " Items");


            holder.tvOrderDate.setText("Order Placed On " + getDate(Long.parseLong(OrderDetailsList.getOrder_Time()), "dd MMM"));
            holder.tvOrderBy.setText("Order By " + OrderDetailsList.getMgmt_Name());

            if (OrderDetailsList.getTotal_Amount().equals("0")) {
                holder.tvOrderAmount.setText("Price Not Set");
            } else {
                holder.tvOrderAmount.setText("Total Amount Rs. " + OrderDetailsList.getTotal_Amount());
            }

            if (OrderDetailsList.getOrder_Status().equals("Rejected")) {
                holder.tvOrderStatus.setTextColor(ManageOrdersActivity.this.getColor(R.color.Red));
//                holder.lvMain.setBackgroundColor(ManageOrdersActivity.this.getColor(R.color.bg_red));
            } else if (OrderDetailsList.getOrder_Status().equals("Accepted")) {
                holder.tvOrderStatus.setTextColor(ManageOrdersActivity.this.getColor(R.color.green));
//                holder.lvMain.setBackgroundColor(ManageOrdersActivity.this.getColor(R.color.bg_green));
            } else {
                holder.tvOrderStatus.setTextColor(ManageOrdersActivity.this.getColor(R.color.Accent_Blue));
//                holder.lvMain.setBackgroundColor(ManageOrdersActivity.this.getColor(R.color.bg_blue));
            }

            holder.lvViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ManageOrdersActivity.this, OrderDetailsAdminActvity.class);
                    i.putExtra("Order_Id", OrderDetailsList.getOrder_Id());
                    i.putExtra("User_Name", OrderDetailsList.getMgmt_Name());
                    i.putExtra("Order_Status", OrderDetailsList.getOrder_Status());
                    i.putExtra("Sub_Total", OrderDetailsList.getUser_Total());
                    i.putExtra("Total", OrderDetailsList.getTotal_Amount());
                    i.putExtra("Delivery", OrderDetailsList.getDelivery_Amount());
                    i.putExtra("User_Id", OrderDetailsList.getUser_Id());
                    startActivityForResult(i, 9282);
                }
            });

            holder.cvMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(ManageOrdersActivity.this)
                            .setTitle("Really Edit?")
                            .setMessage("Are you sure you want to Edit this Order?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    Intent i = new Intent(ManageOrdersActivity.this, OrderDetailsActivity.class);
                                    i.putExtra("Order_Id", OrderDetailsList.getOrder_Id());
                                    i.putExtra("Order_Status", OrderDetailsList.getOrder_Status());
                                    i.putExtra("Sub_Total", OrderDetailsList.getUser_Total());
                                    i.putExtra("Total", OrderDetailsList.getTotal_Amount());
                                    i.putExtra("Delivery", OrderDetailsList.getDelivery_Amount());
                                    i.putExtra("Hotel_Name", OrderDetailsList.getMgmt_Name());
                                    i.putExtra("User_Id", OrderDetailsList.getUser_Id());
                                    startActivity(i);

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


                                if (objectGallery.getMgmt_Name().toLowerCase().contains(charString)) {

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

            TextView tvOrderNo, tvOrderStatus, tvItemCount, tvOrderDate, tvOrderAmount, tvOrderBy,tvAddedOn;
            LinearLayout lvViewDetails, lvMain;
            CardView cvMain;


            public MyViewHolder(View view) {
                super(view);
                tvOrderNo = view.findViewById(R.id.tvOrderNo);
                tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
                tvItemCount = view.findViewById(R.id.tvItemCount);
                tvOrderDate = view.findViewById(R.id.tvOrderDate);
                tvOrderAmount = view.findViewById(R.id.tvOrderAmount);
                lvViewDetails = view.findViewById(R.id.lvViewDetails);
                tvOrderBy = view.findViewById(R.id.tvOrderBy);
                lvMain = view.findViewById(R.id.lvMain);
                cvMain = view.findViewById(R.id.cvMain);
                tvAddedOn = view.findViewById(R.id.tvAddedOn);
            }
        }
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

                strToDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date mDate = sdf.parse(strToDateSubmit);
                    long timeInMilliseconds = mDate.getTime();
                    System.out.println("Date in milli :: " + timeInMilliseconds);
                    orderDateMiliseconds = timeInMilliseconds;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
                getOrderDetails();


            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
//        dlg.getDatePicker().setMaxDate(new Date().getTime());
        dlg.show();
    }

    private String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9282) {
            getOrderDetails();
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(9282, i);
        finish();
    }

}