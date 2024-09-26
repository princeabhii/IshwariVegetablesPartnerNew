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

import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.CommonPojo;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageHotelActivity extends AppCompatActivity {

    ImageView ivBack;
    RecyclerView rcvHotels;
    private List<CommonPojo> HotelList = new ArrayList<>();
    HotelListAdapter hotelListAdapter;
    ProgressDialog progressDialog;
    LinearLayout lvAddHotels;
    RelativeLayout lvNoData;
    CardView cvSearch;
    EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        ivBack = findViewById(R.id.ivBack);
        rcvHotels = findViewById(R.id.rcvHotels);
        lvAddHotels = findViewById(R.id.lvAddHotel);
        lvNoData = findViewById(R.id.lvNoData);
        cvSearch = findViewById(R.id.cvSearch);
        edtSearch = findViewById(R.id.edtSearch);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        lvAddHotels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ManageHotelActivity.this, AddHotelActivity.class);
                startActivityForResult(i,9282);
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
                    hotelListAdapter.getFilter().filter(charSequence);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getHotels();
    }

    private void getHotels() {

        progressDialog = ProgressDialog.show(this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetHotels();

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();

                    HotelList = new ArrayList<>(Arrays.asList(jsonResponse.getHotels()));

                    if (HotelList.size() > 0) {

                        cvSearch.setVisibility(View.VISIBLE);
                        hotelListAdapter = new HotelListAdapter(HotelList, getApplicationContext());
                        rcvHotels.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        rcvHotels.setAdapter(hotelListAdapter);

                    } else {
                        rcvHotels.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        cvSearch.setVisibility(View.GONE);

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
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error "+t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    public class HotelListAdapter extends RecyclerView.Adapter<HotelListAdapter.MyViewHolder> {

        private List<CommonPojo> Hotel_List;
        private List<CommonPojo> searchMeetingList;
        private Context context;


        public HotelListAdapter(List<CommonPojo> Hotel_List, Context context) {
            this.Hotel_List = Hotel_List;
            this.searchMeetingList = Hotel_List;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_sample, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo HotelList = searchMeetingList.get(i);

            holder.tvName.setText(HotelList.getMgmt_Name());

            if (HotelList.getAttachment().equals("NA")) {
                Picasso.get()
                        .load(R.drawable.noimage)
                        .placeholder(R.drawable.noimage)
                        .into(holder.imgHotel);
            } else {
                Picasso.get()
                        .load(HotelList.getAttachment()
                        )
                        .placeholder(R.drawable.noimage)
                        .into(holder.imgHotel);

            }
            holder.cvHotel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(ManageHotelActivity.this)
                            .setTitle("Really Delete?")
                            .setMessage("Are you sure you want to delete " + HotelList.getMgmt_Name() + "?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    deleteVendor(HotelList.getUser_Id());
                                    Log.e("TAG", "onClick: User_Id "+HotelList.getUser_Id() );

                                }
                            }).create().show();
                    return true;
                }
            });
            holder.cvHotel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ManageHotelActivity.this, UpdateHotelActivity.class);
                    i.putExtra("Mgmt_Name", HotelList.getMgmt_Name());
                    i.putExtra("Password", HotelList.getPassword());
                    i.putExtra("Mobile_No", HotelList.getMobile_No());
                    i.putExtra("Email_Id", HotelList.getEmail_Id());
                    i.putExtra("User_Id", HotelList.getUser_Id());
                    i.putExtra("Attachment", HotelList.getAttachment());
                    i.putExtra("Is_Enabled", HotelList.getIs_Enabled());
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

                            for (CommonPojo objectGallery : Hotel_List) {


                                if (objectGallery.getMgmt_Name().toLowerCase().contains(charString)) {

                                    searchMeetingList.add(objectGallery);
                                }

                            }
                        } else {
                            searchMeetingList = Hotel_List;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.e("gallerylist data", " " + Hotel_List.size());
                    Log.e("searchlist data", " " + searchMeetingList.size());


                    FilterResults filterResults = new FilterResults();
                    filterResults.values = searchMeetingList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                    searchMeetingList = (List<CommonPojo>) filterResults.values;
                    // refresh the list with filtered data
                    rcvHotels.getRecycledViewPool().clear();
                    hotelListAdapter.notifyDataSetChanged();
                }
            };
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvName;
            CardView cvHotel;
            ImageView imgHotel;

            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvHotelName);
                cvHotel = view.findViewById(R.id.cvHotel);
                imgHotel = view.findViewById(R.id.imgHotel);
            }
        }
    }

    private void deleteVendor(String User_Id) {

        final ProgressDialog loading = ProgressDialog.show(this, "", "Deleting Hotel...", false, false);

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
        Call<ReturnedResponsePojo> mService = mApiService.DeleteHotel(User_Id);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {
                loading.dismiss();
                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;

                    if (success.equals("1")) {
                        getHotels();
                        Toast.makeText(ManageHotelActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ManageHotelActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

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

    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9282) {
            getHotels();
        }
    }
}
