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

public class UpdateVendorActivity extends AppCompatActivity {

    EditText edVendorName;
    LinearLayout lvUpdateVendor;
    String strVendorName, strVendorId;
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
        setContentView(R.layout.activity_update_vendor);

        Intent i = getIntent();
        strVendorId = i.getStringExtra("Vendor_Id");
        strVendorName = i.getStringExtra("Vendor_Name");

        edVendorName = findViewById(R.id.edVendorName);
        lvUpdateVendor = findViewById(R.id.lvUpdateVendor);
        ivBack = findViewById(R.id.ivBack);

        edVendorName.setText(strVendorName);

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

        lvUpdateVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strVendorName = edVendorName.getText().toString().trim();

                if (strVendorName.equals("")) {
                    Toast.makeText(UpdateVendorActivity.this, "Enter Vendor Name", Toast.LENGTH_SHORT).show();
                } else {
                        updateVendors(strVendorName);
                }
            }
        });
    }

    private void updateVendors(String strVendorName) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(UpdateVendorActivity.this, "", "Uploading...", false, false);

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

        // add another part within the multipart request
        RequestBody TextVendor_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strVendorName);
        RequestBody TextVendor_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strVendorId);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateVendor(TextVendor_Name, TextVendor_Id);
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

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateVendorActivity.this);
                    builder1.setCancelable(false);
                    builder1.setTitle("Success!!");
                    builder1.setMessage("Vendor Updated Successfully.");
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent i = new Intent();
                            setResult(9282, i);
                            finish();
                        }
                    });
                    AlertDialog dialog1 = builder1.create();
                    dialog1.show();
                } else {
                    Toast.makeText(UpdateVendorActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
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


}