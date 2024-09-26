package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vasp.ishwariabhi.AppUtils;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.CommonPojo;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;

import java.io.IOException;
import java.text.DateFormat;
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

public class ProfitHistoryActivity extends AppCompatActivity {

    String strFromDateSubmit, strToDateSubmit,User_Total,Delivery_Amount,Total_Amount;
    long fromDateMiliseconds, toDateMiliseconds;
    ImageView lvBack;
    TextView tvStartDate, tvEndDate,tvTotal;
    RecyclerView rcvDailyProfit;
    private List<CommonPojo> ProfitsList = new ArrayList<>();
    ProfitsListAdapter profitsListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    public static DecimalFormat money = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_history);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvTotal = findViewById(R.id.tvTotal);
        tvEndDate = findViewById(R.id.tvEndDate);
        lvBack = findViewById(R.id.lvBack);
        rcvDailyProfit = findViewById(R.id.rcvDailyProfit);
        lvNoData = findViewById(R.id.lvNoData);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        Calendar cal = Calendar.getInstance();
        strFromDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectStartDatePicker(ProfitHistoryActivity.this);
            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectEndDatePicker(ProfitHistoryActivity.this);
            }
        });

        strToDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");
        try {
            Date mDate = sdf.parse(strFromDateSubmit);
            long toInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + toInMilliseconds);
            toDateMiliseconds = toInMilliseconds;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mDate);
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            strFromDateSubmit = AppUtils.formatDateForDisplay(calendar.getTime(), "yyyy-MM-dd");
            Date newDate = calendar.getTime();
            long timeInMilliseconds = newDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            fromDateMiliseconds = timeInMilliseconds;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tvStartDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strFromDateSubmit));
        tvEndDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
        getProfits();
    }

    private void selectStartDatePicker(Context context) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                strFromDateSubmit = AppUtils.formatDateForDisplay(cal.getTime(), "yyyy-MM-dd");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date mDate = sdf.parse(strFromDateSubmit);
                    long timeInMilliseconds = mDate.getTime();
                    System.out.println("Date in milli :: " + timeInMilliseconds);
                    fromDateMiliseconds = timeInMilliseconds;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tvStartDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strFromDateSubmit));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMaxDate(new Date().getTime());
        dlg.show();
    }

    private void selectEndDatePicker(Context context) {
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
                    toDateMiliseconds = timeInMilliseconds;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tvEndDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strToDateSubmit));
                getProfits();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMaxDate(new Date().getTime());
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

    private void getProfits() {

        final ProgressDialog progressDialog = ProgressDialog.show(ProfitHistoryActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetProfitDatewise(String.valueOf(fromDateMiliseconds), String.valueOf(toDateMiliseconds));

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

                    if (success.equals("1")) {

                        ProfitsList = new ArrayList<>(Arrays.asList(jsonResponse.getDetails()));

                        if (ProfitsList.size() > 0) {

//                            tvSubTotal.setText("₹" + User_Total);
                            double profit = Double.parseDouble(Total_Amount);
                            tvTotal.setText("₹" + money.format( profit));
//                            tvDelivery.setText("+ ₹" + Delivery_Amount);
//                            lvOrderTotal.setVisibility(View.VISIBLE);

                            rcvDailyProfit.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            profitsListAdapter = new ProfitsListAdapter(ProfitsList, ProfitHistoryActivity.this);
                            rcvDailyProfit.setLayoutManager(new LinearLayoutManager(ProfitHistoryActivity.this));

                            rcvDailyProfit.setAdapter(profitsListAdapter);

                        } else {
                            rcvDailyProfit.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);

                        }
                    } else {
                        rcvDailyProfit.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(ProfitHistoryActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(ProfitHistoryActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ProfitHistoryActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(ProfitHistoryActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(ProfitHistoryActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    public class ProfitsListAdapter extends RecyclerView.Adapter<ProfitsListAdapter.MyViewHolder> {

        private List<CommonPojo> Vegetables_List;
        private List<CommonPojo> searchMeetingList;
        private Context context;


        public ProfitsListAdapter(List<CommonPojo> Vegetables_List, Context context) {
            this.Vegetables_List = Vegetables_List;
            this.searchMeetingList = Vegetables_List;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_profit_sample, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo ProfitsList = searchMeetingList.get(i);
            double profit = Double.parseDouble(ProfitsList.getTotal_Profit());
            holder.tvProfitAmount.setText("Rs. " + money.format( profit));
            holder.tvProfitDate.setText(getDate(Long.parseLong(ProfitsList.getOrder_Time()), "dd MMM YYYY"));
            holder.tvProfitType.setText("Profit");


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
                    rcvDailyProfit.getRecycledViewPool().clear();
                    profitsListAdapter.notifyDataSetChanged();
                }
            };
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvProfitAmount, tvProfitDate, tvProfitType;


            public MyViewHolder(View view) {
                super(view);
                tvProfitAmount = view.findViewById(R.id.tvProfitAmount);
                tvProfitDate = view.findViewById(R.id.tvProfitDate);
                tvProfitType = view.findViewById(R.id.tvProfitType);
            }
        }
    }


}

