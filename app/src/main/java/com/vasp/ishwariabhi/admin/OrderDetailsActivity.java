package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderDetailsActivity extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id, strDateSubmit,strCurrTime, Order_Id, Order_Status,Total,Sub_Total,Delivery;
    long dateMiliseconds;
    EditText edtSearch;
    TextView tvNoData,tvSubTotal,tvTotal,tvDelivery;
    private ArrayList<String> arrVeggiesList = new ArrayList<>();
    LinearLayout lvUpdateOrder, lvBack,lvOrderTotal;
    CardView cvAddVegetables ,cvUpdateOrder,cvSharePdf,cvPrint;
    public static DecimalFormat money = new DecimalFormat("0.00");
    File gpxfile;
    String pdfName,strHotelName;
    TextView tvTitle;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        userSession = new UserSession(OrderDetailsActivity.this);

        Intent i = getIntent();
        Order_Id = i.getStringExtra("Order_Id");
        Order_Status = i.getStringExtra("Order_Status");
        Total = i.getStringExtra("Total");
        Sub_Total = i.getStringExtra("Sub_Total");
        Delivery = i.getStringExtra("Delivery");
        strHotelName = i.getStringExtra("Hotel_Name");
        User_Id = i.getStringExtra("User_Id");

//        userSession = new UserSession(OrderDetailsActivity.this);
//        User_Id = userSession.getUserId();
        Log.e("TAG", "onCreateView: User_Id" + User_Id);
        rcvVegetables = findViewById(R.id.rcvVegetables);
        lvNoData = findViewById(R.id.lvNoData);
        edtSearch = findViewById(R.id.edtSearch);
        tvNoData = findViewById(R.id.tvNoData);
        lvUpdateOrder = findViewById(R.id.lvUpdateOrder);
        lvOrderTotal = findViewById(R.id.lvOrderTotal);
        lvBack = findViewById(R.id.lvBack);
        cvAddVegetables = findViewById(R.id.cvAddVegetables);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvTotal = findViewById(R.id.tvTotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        cvUpdateOrder = findViewById(R.id.cvUpdateOrder);
        cvSharePdf = findViewById(R.id.cvSharePdf);
        tvTitle = findViewById(R.id.tvTitle);
        cvPrint = findViewById(R.id.cvPrint);

        tvSubTotal.setText("₹" + Sub_Total);
        tvTotal.setText("₹" + Total);
        tvDelivery.setText("+ ₹" + Delivery);
        tvTitle.setText(strHotelName);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cvSharePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneratePDF pdf = new GeneratePDF();
                pdf.execute();
            }
        });

        cvPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printCustomReciept();
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

        cvAddVegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OrderDetailsActivity.this, AddVegetableInOrderActivity.class);
                i.putExtra("Order_Id", Order_Id);
                i.putExtra("User_Id", User_Id);
                startActivityForResult(i, 9282);
            }
        });


        cvUpdateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrVeggiesList = new ArrayList<>();
                for (int i = 0; i < VegetablesList.size(); i++) {
                    View viewrcv = rcvVegetables.getChildAt(i);
                    EditText edtQty = (EditText) viewrcv.findViewById(R.id.edtQty);
                    String strQty = edtQty.getText().toString();

                    if (!strQty.equals("")) {
                        arrVeggiesList.add(VegetablesList.get(i).getDetail_Id() + "-" + strQty);
                    }
                }
                String strSubsList;
                strSubsList = String.join(",", arrVeggiesList);
                if (arrVeggiesList.size() > 1) {
                    new AlertDialog.Builder(OrderDetailsActivity.this)
                            .setTitle("Update Order?")
                            .setMessage("Continue to update your order?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                    updateOrder(String.valueOf(dateMiliseconds), strSubsList);

                                }
                            }).create().show();

                } else {
                    Toast.makeText(OrderDetailsActivity.this, "Update atleast one quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getVegetables();
    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(OrderDetailsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetOrdersHistory(Order_Id);

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
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, OrderDetailsActivity.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(OrderDetailsActivity.this));

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
                            Toast.makeText(OrderDetailsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(OrderDetailsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(OrderDetailsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(OrderDetailsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(OrderDetailsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_history_row, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo VegetablesList = searchMeetingList.get(i);

            holder.tvVegetableName.setText(VegetablesList.getVegetable_Name());
            holder.edtQty.setText(VegetablesList.getWeight());

            if (Order_Status.equals("Pending")) {
                cvAddVegetables.setVisibility(View.VISIBLE);
                lvUpdateOrder.setVisibility(View.VISIBLE);
                lvOrderTotal.setVisibility(View.GONE);
                holder.lvStatusPending.setVisibility(View.VISIBLE);
                holder.lvStatusAccepted.setVisibility(View.GONE);
                holder.cvMain.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new AlertDialog.Builder(OrderDetailsActivity.this)
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
            } else {
                cvAddVegetables.setVisibility(View.GONE);
                lvUpdateOrder.setVisibility(View.GONE);
                lvOrderTotal.setVisibility(View.VISIBLE);
                holder.lvStatusPending.setVisibility(View.GONE);
                holder.lvStatusAccepted.setVisibility(View.VISIBLE);
                holder.tvVegetableNameAccepted.setText(VegetablesList.getVegetable_Name());
                holder.tvPrice.setText("₹"+VegetablesList.getUser_Price());
                holder.tvQuantity.setText(VegetablesList.getWeight()+" KG");
                try {
                    double vegetablePrice,vegetableWeight,totalAmount;
                    vegetablePrice = Double.parseDouble(VegetablesList.getUser_Price().toString());
                    vegetableWeight = Double.parseDouble(VegetablesList.getWeight().toString());
                    totalAmount = vegetablePrice * vegetableWeight;
                    holder.tvTotalAmount.setText("₹" + money.format(totalAmount));
                } catch (NumberFormatException e) {

                }
            }

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

            TextView tvVegetableName, tvTotalAmount, tvQuantity, tvPrice, tvVegetableNameAccepted;
            CardView cvMain;
            EditText edtQty;
            ImageView imgVegetable;
            LinearLayout lvStatusAccepted, lvStatusPending;


            public MyViewHolder(View view) {
                super(view);
                tvVegetableName = view.findViewById(R.id.tvVegetableName);
                imgVegetable = view.findViewById(R.id.imgVegetable);
                cvMain = view.findViewById(R.id.cvMain);
                edtQty = view.findViewById(R.id.edtQty);
                tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
                tvQuantity = view.findViewById(R.id.tvQuantity);
                tvPrice = view.findViewById(R.id.tvPrice);
                tvVegetableNameAccepted = view.findViewById(R.id.tvVegetableNameAccepted);
                lvStatusAccepted = view.findViewById(R.id.lvStatusAccepted);
                lvStatusPending = view.findViewById(R.id.lvStatusPending);
            }
        }
    }

    private void updateOrder(String strOrderTime, String strOrderDetails) {

        progressDialog = ProgressDialog.show(OrderDetailsActivity.this, "", "Please Wait...", true, false);


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
                        new AlertDialog.Builder(OrderDetailsActivity.this)
                                .setTitle("Success!")
                                .setCancelable(false)
                                .setMessage("Order Updated Successfully!!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0, int arg1) {

                                        getVegetables();

                                    }
                                }).create().show();


                    } else {
                        Toast.makeText(OrderDetailsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(OrderDetailsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(OrderDetailsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(OrderDetailsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(OrderDetailsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(OrderDetailsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void deleteVegetables(String strDetailId, String strVegetableName) {

        progressDialog = ProgressDialog.show(OrderDetailsActivity.this, "", "Please Wait...", true, false);


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

                        Toast.makeText(OrderDetailsActivity.this, strVegetableName + " deleted from order.", Toast.LENGTH_SHORT).show();
                        getVegetables();

                    } else {
                        Toast.makeText(OrderDetailsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(OrderDetailsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(OrderDetailsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(OrderDetailsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(OrderDetailsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(OrderDetailsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    private class GeneratePDF extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(OrderDetailsActivity.this,
                    getString(R.string.generating_pdf),
                    getString(R.string.please_wait));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(OrderDetailsActivity.this);
            builder1.setMessage(R.string.pdf_generated_do_you_want_to_open);
            builder1.setCancelable(false);
            builder1.setPositiveButton(getResources().getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                                try {
                                    Intent i = new Intent(OrderDetailsActivity.this, ViewFileActivity.class);
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

            name = "" + dateMiliseconds;


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
                PdfPTable table3 = new PdfPTable(6);
                table3.setWidthPercentage(80f);
                insertCell(table3, "Sr No.", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Vegetables", Element.ALIGN_CENTER, 3, bfBold12);
//                insertCell(table3, "Amount", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Quantity(Kgs)", Element.ALIGN_CENTER, 2, bfBold12);
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
                    insertCell(table3, VegetablesList.get(x).getVegetable_Name(), Element.ALIGN_CENTER, 3, bf12);
//                    insertCell(table3, VegetablesList.get(x).getUser_Price(), Element.ALIGN_CENTER, 1, bf12);
                    insertCell(table3, VegetablesList.get(x).getWeight(), Element.ALIGN_CENTER, 2, bf12);
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
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Hotel Name: " + strHotelName), 300, 731, 0);
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

        String BILL = "                 TOTAL QUANTITY\n\n\n\n" +

                "                INVOICE/CASH MEMO    \n"
                + getString(R.string.app_name_print) +
                ""+getString(R.string.shop_addr_print) +
                ""+getString(R.string.mob_no_print) +
                "        Time: " + strCurrTime + "   Date: " + AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit) + "        \n" +
                "  Name: " + strHotelName + "   \n";
        BILL = BILL
                + "-----------------------------------------------\n";


        BILL = BILL + String.format("%1$-17s %2$-15s %3$10s", "SrNo.", "Item", "Qty");
        BILL = BILL + "\n";
        BILL = BILL
                + "-----------------------------------------------";
//                    }


        for (int x = 0; x < VegetablesList.size(); x++) {
            try {
//                double TotalWeight = Double.parseDouble(VegetablesList.get(x).getTotal_Weight());
                BILL = BILL + "\n " + String.format("%1$-15s %2$-15s %3$10s", x + 1 + "", VegetablesList.get(x).getVegetable_Name(), VegetablesList.get(x).getWeight());
            } catch (NumberFormatException e) {

            }

        }

        BILL = BILL
                + "\n-----------------------------------------------";
        BILL = BILL + "\n\n ";

//                    BILL = BILL + "                       Total:" + "     " + Total_Amount + "\n";
//                    BILL = BILL + "\n " + " ";
//                    BILL = BILL + "\n " + " ";
//                    BILL = BILL + "                                  Proprietor" + "\n";
//
//                    BILL = BILL
//                            + "-----------------------------------------------\n";
//                    BILL = BILL + "\n " + " ";
//                    BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";
        BILL = BILL + "\n " + " ";

        Log.e("TAG", "onCreate: PrinterAddress " + userSession.getPrinterAddress());
        Intent intent = new Intent(OrderDetailsActivity.this, BluetoothDataService.class);
        intent.putExtra("MAC_ADDRESS", userSession.getPrinterAddress());
        intent.putExtra("BILL", BILL);
        startService(intent);
    }


    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9282) {
            getVegetables();
        }
    }
}