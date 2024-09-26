package com.vasp.ishwariabhi.admin;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddVendorActivity extends AppCompatActivity {

    EditText edVendorName;
    LinearLayout lvAddVendor;
    String strVendor_Name;
    ProgressBar progressbar;
    ImageView ivBack;
    RelativeLayout CoverPicture;
    ImageView add_img, img_add;
    TextView tv_Addphoto;
    Uri filePath = null;
    private static final int PICK_IMAGE_REQUEST = 11;
    private String filePathName = null;
    private String onlyPathName = null;
    private ArrayList<Uri> FilesList = new ArrayList<>();
    private ArrayList<String> files = new ArrayList<>();
    private ArrayList<String> pathWithoutFile = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 200;
    Uri compressUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vendor);

        edVendorName = findViewById(R.id.edVendorName);
        lvAddVendor = findViewById(R.id.lvAddVendor);
        ivBack = findViewById(R.id.ivBack);
        progressbar = findViewById(R.id.progressbar);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);

//        CoverPicture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showFileChooser();
//            }
//        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        lvAddVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strVendor_Name = edVendorName.getText().toString().trim();

                if (strVendor_Name.equals("")) {
                    Toast.makeText(AddVendorActivity.this, "Enter Vendor Name", Toast.LENGTH_SHORT).show();
                } else {
                        uploadFile(strVendor_Name);

                    }
                }
        });
    }

    private void uploadFile (String strName){
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(AddVendorActivity.this, "", "Uploading...", false, false);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.APP_SERVER_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        AllApi mApiService = retrofit.create(AllApi.class);

        // MultipartBody.Part is used to send also the actual file name

        // add another part within the multipart request
        RequestBody Vendor_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strName);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.AddVendor(Vendor_Name);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();

                ReturnedResponsePojo jsonResponse = response.body();
                String success = jsonResponse.success;
                String message = jsonResponse.message;

                if (success.equals("1")) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(AddVendorActivity.this);
                    builder1.setCancelable(false);
                    builder1.setTitle("Success!!");
                    builder1.setMessage("Vendor Added Successfully.");
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
//                            Intent i = new Intent(AddVendorActivity.this, AdminCategoriesActivity.class);
//                            startActivity(i);
//                            finish();
                            Intent i = new Intent();
                            setResult(9282,i);
                            finish();

                        }
                    });
                    AlertDialog dialog1 = builder1.create();
                    dialog1.show();
                }else {
                    Toast.makeText(AddVendorActivity.this, "Error "+message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                loading.dismiss();
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
}