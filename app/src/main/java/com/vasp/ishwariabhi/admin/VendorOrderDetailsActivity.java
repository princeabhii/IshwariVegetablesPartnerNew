package com.vasp.ishwariabhi.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.text.DateFormat;
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

public class VendorOrderDetailsActivity extends AppCompatActivity {

    RecyclerView rcvVegetables;
    private List<CommonPojo> VegetablesList = new ArrayList<>();
    VegetablesListAdapter vegetablesListAdapter;
    ProgressDialog progressDialog;
    RelativeLayout lvNoData;
    UserSession userSession;
    String User_Id, Total_Amount, strVendorId, strCurrTime, strVendorName, strVegetableName, strVegetableId, strFromDate, strToDate, strPaymentStatus, strDateSubmit;
    long dateMiliseconds, fromMiliSecs, toMiliSecs;
    TextView tvNoData, tvSubTotal, tvTotal, tvDelivery;
    LinearLayout lvBack, lvOrderTotal;
    public static DecimalFormat money = new DecimalFormat("0.00");
    private List<CommonPojo> UsersList = new ArrayList<>();
    File gpxfile;
    String pdfName;
    CardView cvSharePdf, cvPrintReceipt, cvMarkPaid;
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_order_details);

        userSession = new UserSession(VendorOrderDetailsActivity.this);
//        User_Id = userSession.getUserId();
//        Log.e("TAG", "onCreateView: User_Id" + User_Id);

        Intent i = getIntent();
        fromMiliSecs = i.getLongExtra("From_Date", 0);
        strFromDate = String.valueOf(fromMiliSecs);
        toMiliSecs = i.getLongExtra("To_Date", 0);
        strToDate = String.valueOf(toMiliSecs);
        strPaymentStatus = i.getStringExtra("Payment_Status");
        strVendorId = i.getStringExtra("Vendor_Id");
        strVendorName = i.getStringExtra("Vendor_Name");
        strVegetableId = i.getStringExtra("Vegetable_Id");

        rcvVegetables = findViewById(R.id.rcvVegetables);
        lvNoData = findViewById(R.id.lvNoData);
        tvNoData = findViewById(R.id.tvNoData);
        lvOrderTotal = findViewById(R.id.lvOrderTotal);
        lvBack = findViewById(R.id.lvBack);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvTotal = findViewById(R.id.tvTotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        cvSharePdf = findViewById(R.id.cvSharePdf);
        cvPrintReceipt = findViewById(R.id.cvPrintReceipt);
        cvMarkPaid = findViewById(R.id.cvMarkPaid);

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

        cvSharePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneratePDF pdf = new GeneratePDF();
                pdf.execute();
            }
        });

        cvPrintReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printCustomReciept();
            }
        });

        getVegetables();
    }

    private void getVegetables() {

        progressDialog = ProgressDialog.show(VendorOrderDetailsActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetVendorOrderDetails(strVendorId, strVegetableId, strFromDate, strToDate, strPaymentStatus);
        Log.e(TAG, "getVegetables: PASSED_STRINGS" + strVendorId + " " + strVegetableId + " " + strFromDate + " " + strToDate + " " + strPaymentStatus);
        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;
                    Total_Amount = jsonResponse.Total_Amount;

                    if (success.equals("1")) {

                        VegetablesList = new ArrayList<>(Arrays.asList(jsonResponse.getDetails()));

                        if (VegetablesList.size() > 0) {

                            tvTotal.setText("₹" + Total_Amount);
                            lvOrderTotal.setVisibility(View.VISIBLE);

                            rcvVegetables.setVisibility(View.VISIBLE);
                            lvNoData.setVisibility(View.GONE);
                            vegetablesListAdapter = new VegetablesListAdapter(VegetablesList, VendorOrderDetailsActivity.this);
                            rcvVegetables.setLayoutManager(new LinearLayoutManager(VendorOrderDetailsActivity.this));

                            rcvVegetables.setAdapter(vegetablesListAdapter);

                            if (strPaymentStatus.equals("UnPaid")) {
                                cvMarkPaid.setVisibility(View.VISIBLE);
                                cvMarkPaid.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(VendorOrderDetailsActivity.this);
                                        builder1.setCancelable(false);
                                        builder1.setTitle("Mark Paid?");
                                        builder1.setMessage("Do you want to mark these bills as Paid?");
                                        builder1.setNegativeButton("Cancel",null);
                                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                changePaymentStatus("Paid");

                                            }
                                        });
                                        androidx.appcompat.app.AlertDialog dialog1 = builder1.create();
                                        dialog1.show();
                                    }
                                });

                            } else {
                                cvMarkPaid.setVisibility(View.GONE);
                            }

                        } else {
                            lvOrderTotal.setVisibility(View.GONE);
                            rcvVegetables.setVisibility(View.GONE);
                            cvMarkPaid.setVisibility(View.GONE);
                            lvNoData.setVisibility(View.VISIBLE);

                        }
                    } else {
                        rcvVegetables.setVisibility(View.GONE);
                        lvNoData.setVisibility(View.VISIBLE);
                        cvMarkPaid.setVisibility(View.GONE);
//                        tvNoData.setText(message);
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(VendorOrderDetailsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(VendorOrderDetailsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(VendorOrderDetailsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(VendorOrderDetailsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(VendorOrderDetailsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });

    }

    private void changePaymentStatus(String strPaymentStatus) {

        progressDialog = ProgressDialog.show(VendorOrderDetailsActivity.this, "", "Please Wait...", true, false);

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
        Log.e("TAG", "getUsers: DATA_PASSED" + strFromDate + " " + strToDate + " " + strPaymentStatus);
        Call<ReturnedResponsePojo> mService = mApiService.UpdateVendorPaymentStatus(strFromDate, strToDate, strPaymentStatus, strVendorId, strVegetableId);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {
                        androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(VendorOrderDetailsActivity.this);
                        builder1.setCancelable(false);
                        builder1.setTitle("Success!!");
                        builder1.setMessage("Vegetable have been Marked as paid!");
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                Intent i = new Intent();
                                setResult(9282, i);
                                finish();

                            }
                        });
                        androidx.appcompat.app.AlertDialog dialog1 = builder1.create();
                        dialog1.show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(VendorOrderDetailsActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(VendorOrderDetailsActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(VendorOrderDetailsActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(VendorOrderDetailsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(VendorOrderDetailsActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_wise_vegetables_item, parent, false);
            return new MyViewHolder(v);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {

            final CommonPojo VegetablesList = searchMeetingList.get(i);

            holder.tvVegetableName.setText(VegetablesList.getVegetable_Name());
            strVegetableName = VegetablesList.getVegetable_Name();
            holder.tvDate.setText("" + getDate(Long.parseLong(VegetablesList.getAdded_On()), "dd MMM YYYY"));
            holder.tvVendorPrice.setText("Vendor Price : Rs." + VegetablesList.getVendor_Price());
            holder.tvTotalAmount.setText("Rs. " + VegetablesList.getTotal_Amount());
            holder.tvTotalWeight.setText("Total Weight : " + VegetablesList.getWeight() + " KG");


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

            TextView tvDate, tvTotalAmount, tvVendorPrice, tvTotalWeight, tvVegetableName;


            public MyViewHolder(View view) {
                super(view);
                tvDate = view.findViewById(R.id.tvDate);
                tvVendorPrice = view.findViewById(R.id.tvVendorPrice);
                tvTotalWeight = view.findViewById(R.id.tvTotalWeight);
                tvVegetableName = view.findViewById(R.id.tvVegetableName);
                tvTotalAmount = view.findViewById(R.id.tvTotalAmount);

            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GeneratePDF extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(VendorOrderDetailsActivity.this,
                    getString(R.string.generating_pdf),
                    getString(R.string.please_wait));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(VendorOrderDetailsActivity.this);
            builder1.setMessage(R.string.pdf_generated_do_you_want_to_open);
            builder1.setCancelable(false);
            builder1.setPositiveButton(getResources().getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                                try {
                                    Intent i = new Intent(VendorOrderDetailsActivity.this, ViewFileActivity.class);
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

            AlertDialog alert11 = builder1.create();
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
                insertCell(table3, "Date", Element.ALIGN_CENTER, 2, bfBold12);
                insertCell(table3, "Amount", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Weight", Element.ALIGN_CENTER, 1, bfBold12);
                insertCell(table3, "Total", Element.ALIGN_CENTER, 1, bfBold12);
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
                    insertCell(table3, getDate(Long.parseLong(VegetablesList.get(x).getAdded_On()),"dd-MM-yyyy"), Element.ALIGN_CENTER, 2, bf12);
                    insertCell(table3, VegetablesList.get(x).getVendor_Price(), Element.ALIGN_CENTER, 1, bf12);
                    insertCell(table3, VegetablesList.get(x).getWeight(), Element.ALIGN_CENTER, 1, bf12);
                    insertCell(table3, VegetablesList.get(x).getTotal_Amount(), Element.ALIGN_CENTER, 1, bf12);
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

                PdfPTable tableTotal = new PdfPTable(6);
                tableTotal.setWidthPercentage(80);
                insertCell(tableTotal, "Total", Element.ALIGN_CENTER, 5, fontTotalBold);
                insertCell(tableTotal, "" + Total_Amount, Element.ALIGN_CENTER, 1, fontTotalBold);
                tableTotal.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                document.add(tableTotal);

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
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Vendor Name: " + strVendorName), 300, 731, 0);
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
    }

    private void beginPrinting() {

        String BILL = "                     " + strVegetableName.toUpperCase() + "\n\n\n\n" +

                "                INVOICE/CASH MEMO    \n"
                + getString(R.string.app_name_print) +
                ""+getString(R.string.shop_addr_print) +
                ""+getString(R.string.mob_no_print) +
                "Time: " + strCurrTime + "   Date: " + AppUtils.convertDateyyyymmddToddmmyyyy(strDateSubmit) + "        \n" +
                "  Name: " + strVendorName + "   \n";
        BILL = BILL
                + "-----------------------------------------------\n";


        BILL = BILL + String.format("%1$-8s %2$-10s %3$5s %4$9s %5$8s", "SrNo.", "Date", "Qty", "Rate", "Total");
        BILL = BILL + "\n";
        BILL = BILL
                + "-----------------------------------------------";
//                    }


        for (int x = 0; x < VegetablesList.size(); x++) {
            try {
//                double vegetablePrice, vegetableWeight, totalAmount;
//                vegetablePrice = Double.parseDouble(VegetablesList.get(x).getUser_Price().toString());
//                vegetableWeight = Double.parseDouble(VegetablesList.get(x).getWeight().toString());
//                totalAmount = vegetablePrice * vegetableWeight;
                BILL = BILL + "\n " + String.format("%1$-4s %2$-15s %3$-8s %4$-8s %5$-4s", x + 1 + "",  getDate(Long.parseLong(VegetablesList.get(x).getAdded_On()),"dd-MM-yyyy"), VegetablesList.get(x).getWeight(), VegetablesList.get(x).getVendor_Price(), VegetablesList.get(x).getTotal_Amount());
            } catch (NumberFormatException e) {

            }

        }

//        BILL = BILL
//                + "\n-----------------------------------------------";
//        BILL = BILL + "\n\n ";
//
//        BILL = BILL + "                Sub Total:" + "     Rs. " + User_Total + "\n";
//        BILL = BILL + "                  Delivery:" + "   + Rs. " + Delivery_Amount + "\n";
        BILL = BILL
                + "\n-----------------------------------------------\n";
        BILL = BILL + "                     Total:" + "     Rs. " + Total_Amount + "\n";
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
        Intent intent = new Intent(VendorOrderDetailsActivity.this, BluetoothDataService.class);
        intent.putExtra("MAC_ADDRESS", userSession.getPrinterAddress());
        intent.putExtra("BILL", BILL);
        startService(intent);


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