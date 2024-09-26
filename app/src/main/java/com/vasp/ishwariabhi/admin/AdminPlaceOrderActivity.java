package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.vasp.ishwariabhi.session.UserSession;

import java.io.IOException;
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

public class AdminPlaceOrderActivity extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    private List<CommonPojo> UsersList = new ArrayList<>();
    List<String> arrUsersId = new ArrayList<String>();
    List<String> arrUsersName = new ArrayList<String>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id = "", strDateSubmit, strUserName;
    long dateMiliseconds;
    EditText edtSearch;
    TextView tvOrderDate, tvNoData;
    private ArrayList<String> arrVeggiesList = new ArrayList<>();
    CardView cvPlaceOrder, cvSearch;
    LinearLayout lvAddToCart, ly_back, lvBottom;
    Spinner spnUsers;
    ImageView imgSearch;
    private ArrayList<String> arrSearchVeggiesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_place_order);

        userSession = new UserSession(AdminPlaceOrderActivity.this);
//        User_Id = userSession.getUserId();
        Log.e("TAG", "onCreateView: User_Id" + User_Id);
        rcvVegetables = findViewById(R.id.rcvVegetables);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvNoData = findViewById(R.id.tvNoData);
        cvPlaceOrder = findViewById(R.id.cvPlaceOrder);
        ly_back = findViewById(R.id.ly_back);
        cvPlaceOrder = findViewById(R.id.cvPlaceOrder);
        spnUsers = findViewById(R.id.spnUsers);
        lvAddToCart = findViewById(R.id.lvAddToCart);
        lvBottom = findViewById(R.id.lvBottom);
        cvSearch = findViewById(R.id.cvSearch);
        imgSearch = findViewById(R.id.imgSearch);

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

        ly_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        lvAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!User_Id.equals("Select Hotel")) {
                    Intent i = new Intent(AdminPlaceOrderActivity.this, AddToCartAdmin.class);
                    i.putExtra("User_Id", User_Id);
                    i.putExtra("User_Name", strUserName);
                    startActivityForResult(i, 9282);
                } else {
                    Toast.makeText(AdminPlaceOrderActivity.this, "Select Hotel!", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        edtSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
//                if (charSequence.equals("") || charSequence == null) {
//
//                } else {
////                    vegetablesListAdapter.getFilter().filter(charSequence);
//
//                }
//
//
//            }
//
//            @Override
//                public void afterTextChanged(Editable editable) {
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

        spnUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

//                if (i > 0) {
                strUserName = adapterView.getItemAtPosition(i).toString();
                User_Id = arrUsersId.get(i);
                getVegetables();
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvOrderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDatePicker(AdminPlaceOrderActivity.this);
            }
        });

        tvOrderDate.setText(AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit));

        cvPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!User_Id.equals("")) {
                    arrVeggiesList = new ArrayList<>();
                    for (int i = 0; i < VegetablesList.size(); i++) {
                        View viewrcv = rcvVegetables.getChildAt(i);
                        EditText edtQty = (EditText) viewrcv.findViewById(R.id.edtQty);
                        String strQty = edtQty.getText().toString();

                        if (!strQty.equals("")) {
                            arrVeggiesList.add(VegetablesList.get(i).getVegetable_Id() + "-" + strQty);
                        }
                    }

                    String strSubsList;
                    strSubsList = String.join(",", arrVeggiesList);
                    Log.e("TAG", "onClick: WEIGHT_LIST" + strSubsList);
                    if (arrVeggiesList.size() > 1) {
                        new AlertDialog.Builder(AdminPlaceOrderActivity.this)
                                .setTitle("Place Order?")
                                .setMessage("Continue to place your order?")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        placeOrder(String.valueOf(dateMiliseconds), strSubsList);

                                    }
                                }).create().show();

                    } else {
                        Toast.makeText(AdminPlaceOrderActivity.this, "Add at least one quantity", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AdminPlaceOrderActivity.this, "Select Hotel!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getUsers();

    }

    private void performSearch() {
        String strSearchedValue = edtSearch.getText().toString();
        if (!strSearchedValue.equals("")) {
            arrSearchVeggiesList = new ArrayList<>();
            for (int i = 0; i < VegetablesList.size(); i++) {
                View viewrcv = rcvVegetables.getChildAt(i);
                TextView tvVegetableName = (TextView) viewrcv.findViewById(R.id.tvVegetableName);
                EditText edtQty = (EditText) viewrcv.findViewById(R.id.edtQty);
                String stVegetableName = tvVegetableName.getText().toString();

                if (stVegetableName.toString().toLowerCase().contains(strSearchedValue.toString().toLowerCase())) {
                    edtQty.requestFocus();
                    arrSearchVeggiesList.add(strSearchedValue);
                }
            }
            if (arrSearchVeggiesList.size()<1){
                Toast.makeText(AdminPlaceOrderActivity.this, "Vegetable Not Found", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(AdminPlaceOrderActivity.this, "Enter Vegetable Name", Toast.LENGTH_SHORT).show();
        }
    }


    private void getUsers() {

//        progressDialog = ProgressDialog.show(AdminPlaceOrderActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetHotelsForOrder(String.valueOf(dateMiliseconds));

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

//                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    UsersList = new ArrayList<>(Arrays.asList(jsonResponse.getHotels()));

//                    if (success.equals("1")) {
//
//
//
//                        if (UsersList.size() > 0) {
//                            populateSpinner();
//                        } else {
//                            Toast.makeText(AdminPlaceOrderActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        populateSpinner();
//                        Toast.makeText(AdminPlaceOrderActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
//                    }
                    if (success.equals("1")) {
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + UsersList.size());
                        populateSpinner();
//                        if (UsersList.size() > 0) {
//
//                        } else {
//                            Toast.makeText(DailyReceiptsActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        populateSpinner();
                        Log.e("TAG", "onResponse: USER_LIST_SIZE" + UsersList.size());
                        Toast.makeText(AdminPlaceOrderActivity.this, "No Hotels Found!", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminPlaceOrderActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminPlaceOrderActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminPlaceOrderActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
//                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminPlaceOrderActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminPlaceOrderActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(AdminPlaceOrderActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetCartVegetables(User_Id, String.valueOf(dateMiliseconds));
        Log.e("TAG", "getVegetables: User_Id "+User_Id +" dateMiliseconds "+dateMiliseconds);

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
                            lvAddToCart.setVisibility(View.VISIBLE);
                            lvBottom.setVisibility(View.VISIBLE);
                            rcvVegetables.setVisibility(View.VISIBLE);
                            cvSearch.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, AdminPlaceOrderActivity.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(AdminPlaceOrderActivity.this));

                            rcvVegetables.setAdapter(vegetablesListAdapter);

                        } else {
                            rcvVegetables.setVisibility(View.GONE);
                            cvSearch.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);
                            lvAddToCart.setVisibility(View.VISIBLE);
                            lvBottom.setVisibility(View.GONE);

                        }
                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        cvSearch.setVisibility(View.GONE);
                        lvAddToCart.setVisibility(View.VISIBLE);
                        lvBottom.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminPlaceOrderActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminPlaceOrderActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminPlaceOrderActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminPlaceOrderActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminPlaceOrderActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void populateSpinner() {

        arrUsersName.clear();
        arrUsersId.clear();

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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_row, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo VegetablesList = searchMeetingList.get(i);

            holder.tvVegetableName.setText(VegetablesList.getVegetable_Name());

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

            holder.cvMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(AdminPlaceOrderActivity.this)
                            .setTitle("Really Remove?")
                            .setMessage("Are you sure you want to remove " + VegetablesList.getVegetable_Name() + " from Cart?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    deleteVegetables(VegetablesList.getVegetable_Id(), VegetablesList.getVegetable_Name());

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

            TextView tvVegetableName;
            CardView cvMain;
            EditText edtQty;
            ImageView imgVegetable;


            public MyViewHolder(View view) {
                super(view);
                tvVegetableName = view.findViewById(R.id.tvVegetableName);
                imgVegetable = view.findViewById(R.id.imgVegetable);
                cvMain = view.findViewById(R.id.cvMain);
                edtQty = view.findViewById(R.id.edtQty);
            }
        }
    }

    private void placeOrder(String strOrderTime, String strOrderDetails) {

        progressDialog = ProgressDialog.show(AdminPlaceOrderActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.PlaceOrder(User_Id, strOrderTime, strOrderDetails);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {
                        new AlertDialog.Builder(AdminPlaceOrderActivity.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Order Placed Successfully!!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        getVegetables();
                                        getUsers();

                                    }
                                }).create().show();


                    } else {
                        getVegetables();
                        Toast.makeText(AdminPlaceOrderActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminPlaceOrderActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminPlaceOrderActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminPlaceOrderActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminPlaceOrderActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminPlaceOrderActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void deleteVegetables(String strVegetableId, String strVegetableName) {

        progressDialog = ProgressDialog.show(AdminPlaceOrderActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.DeleteCart(User_Id, strVegetableId);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        Toast.makeText(AdminPlaceOrderActivity.this, strVegetableName + " Removed From Cart", Toast.LENGTH_SHORT).show();
                        getVegetables();

                    } else {
                        Toast.makeText(AdminPlaceOrderActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminPlaceOrderActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminPlaceOrderActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminPlaceOrderActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminPlaceOrderActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminPlaceOrderActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
//        dlg.getDatePicker().setMaxDate(now + (1000 * 60 * 60 * 24));
//        dlg.getDatePicker().setMinDate(new Date().getTime());
        dlg.show();
    }

    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9282) {
            getVegetables();
        }

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(9282, i);
        finish();
    }


}