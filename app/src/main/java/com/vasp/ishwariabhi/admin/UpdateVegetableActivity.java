package com.vasp.ishwariabhi.admin;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.getExternalStoragePublicDirectory;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.FileUtilsNew;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.CommonPojo;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateVegetableActivity extends AppCompatActivity {

    String strVegetable_Id, strVegetable_Name, strVendor_Id, strAttachment, strVendorName,strVegetableNameMarathi;
    EditText edVegetableName,edVegetableNameMarathi;
    LinearLayout lvUpdateVegetables;
    ProgressBar progressbar;
    RelativeLayout CoverPicture;
    ImageView add_img, img_add, ivBack;
    TextView tv_Addphoto;
    Uri filePath = null;
    private static final int PICK_IMAGE_REQUEST = 11;
    private String filePathName = null;
    private String onlyPathName = null;
    private ArrayList<Uri> FilesList = new ArrayList<>();
    private ArrayList<String> files = new ArrayList<>();
    private ArrayList<String> pathWithoutFile = new ArrayList<>();
    private ArrayList<CommonPojo> arrVendorsList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 200;
    Uri compressUri = null;
    Spinner spnVendors;
    List<String> arrVendorName = new ArrayList<String>();
    List<String> arrVendorId = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_vegetable);

        Intent i = getIntent();
        strVegetable_Id = i.getStringExtra("Vegetable_Id");
        strVegetable_Name = i.getStringExtra("Vegetable_Name");
        strVegetableNameMarathi = i.getStringExtra("Vegetable_Name_Marathi");
        strVendor_Id = i.getStringExtra("Vendor_Id");
        strVendorName = i.getStringExtra("Vendor_Name");
        Log.e("TAG", "onCreate: VENDOR_NAME " + strVendorName);
        strAttachment = i.getStringExtra("Attachment");
        Log.e("TAG", "onCreate:Vendor_Id " + strVendor_Id);

        edVegetableName = findViewById(R.id.edVegetableName);
        lvUpdateVegetables = findViewById(R.id.lvUpdateVegetables);
        progressbar = findViewById(R.id.progressbar);
        CoverPicture = findViewById(R.id.CoverPicture);
        add_img = findViewById(R.id.add_img);
        img_add = findViewById(R.id.img_add);
        ivBack = findViewById(R.id.ivBack);
        tv_Addphoto = findViewById(R.id.tv_Addphoto);
        spnVendors = findViewById(R.id.spnVendors);
        edVegetableNameMarathi = findViewById(R.id.edVegetableNameMarathi);
//        spnCategoriesList = findViewById(R.id.spnCategoriesList);

        edVegetableName.setText(strVegetable_Name);
        edVegetableNameMarathi.setText(strVegetableNameMarathi);

        if (strAttachment != null) {
            img_add.setVisibility(View.GONE);
            tv_Addphoto.setVisibility(View.GONE);
            add_img.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(strAttachment)
                    .placeholder(R.drawable.noimage)
                    .into(add_img);

        } else {

            Picasso.get()
                    .load(R.drawable.noimage)
                    .placeholder(R.drawable.noimage)
                    .into(add_img);

        }

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);


        CoverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                img_add.setVisibility(View.GONE);
                tv_Addphoto.setVisibility(View.GONE);
                add_img.setVisibility(View.VISIBLE);
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        spnVendors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i > 0) {
                    strVendorName = adapterView.getItemAtPosition(i).toString();
                    strVendor_Id = arrVendorId.get(i);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lvUpdateVegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strVegetable_Name = edVegetableName.getText().toString().trim();
                strVegetableNameMarathi = edVegetableNameMarathi.getText().toString().trim();

                if (strVegetable_Name.equals("")) {
                    Toast.makeText(UpdateVegetableActivity.this, "Enter Vegetable Name", Toast.LENGTH_SHORT).show();
                } else if (strVegetableNameMarathi.equals("")){
                    Toast.makeText(UpdateVegetableActivity.this, "Enter Marathi Vegetable Name", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (files.size() > 0) {
                        updateVegetable(FilesList.get(0), strVegetable_Id, strVegetable_Name,strVegetableNameMarathi, strVendor_Id);
                    } else {
                        updateVegetableWithoutImage(strVegetable_Id, strVegetable_Name,strVegetableNameMarathi, strVendor_Id);

//                        Toast.makeText(UpdateVegetableActivity.this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        getVendors();

    }

    private void getVendors() {

        final ProgressDialog loading = ProgressDialog.show(this, "", "Loading...", false, false);

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
        Call<ReturnedResponsePojo> mService = mApiService.GetVendors();

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {
                ReturnedResponsePojo jsonResponse = response.body();
                loading.dismiss();
                arrVendorsList = new ArrayList<>(Arrays.asList(jsonResponse.getVendors()));

                if (arrVendorsList.size() > 0) {
                    populateSpinnerVendors();
                } else {
                    populateSpinnerVendors();
                    Toast.makeText(UpdateVegetableActivity.this, "Data Not Found", Toast.LENGTH_SHORT).show();
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

    private void populateSpinnerVendors() {

        arrVendorName.clear();
        arrVendorId.clear();

        arrVendorName.add("Select Vendor");
        arrVendorId.add("Select Vendor");

        for (int i = 0; i < arrVendorsList.size(); i++) {
            arrVendorName.add(arrVendorsList.get(i).getVendor_Name());
        }

        for (int i = 0; i < arrVendorsList.size(); i++) {
            arrVendorId.add(arrVendorsList.get(i).getVendor_Id());
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arrVendorName);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnVendors.setAdapter(spinnerAdapter);

        if (strVendorName != null) {
            if (!strVendorName.equals("")) {
                selectSpinnerValue(spnVendors, strVendorName);
            }
        }
//        Log.e("TAG", "populateSpinnerDesig: DESIGNATION_RETURNED"+strVendorName );

    }


    private void takePhoto() {

        galleryIntent_1();
    }

    private void galleryIntent_1() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void updateVegetable(Uri fileUri, String strVegetable_Id, String strVegetable_Name, String strVegetable_NameMarathi, String strVendor_Id) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(UpdateVegetableActivity.this, "", "Uploading...", false, false);

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
        Log.e("TAG", ": in upload:fileUri " + fileUri);
        File file = FileUtilsNew.getFile(getApplicationContext(), fileUri);

        Log.e("TAG", ": in upload:file " + file);
        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("Attachment", file.getName(), requestFile);

        // add another part within the multipart request
        RequestBody Vegetable_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_Id);
        RequestBody Vegetable_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_Name);
        RequestBody Vegetable_Name_Marathi = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_NameMarathi);
        RequestBody Vendor_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strVendor_Id);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateVegetable(Vegetable_Id, Vegetable_Name,Vegetable_Name_Marathi, Vendor_Id, body);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateVegetableActivity.this);
                builder1.setCancelable(false);
                builder1.setTitle("Success!!");
                builder1.setMessage("Vegetable Details Updated successfully.");
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        Intent refresh = new Intent();
//                        startActivity(refresh);//Start the same Activity
//                        finish();
                        Intent i = new Intent();
                        setResult(9282, i);
                        finish();
//                        onBackPressed();

                    }
                });
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                loading.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "onFailure: UPLOAD ERROR " + t);
                    // logging probably not necessary
                } else {
                    Toast.makeText(getApplicationContext(), "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });
    }

    private void updateVegetableWithoutImage(String strVegetable_Id, String strVegetable_Name,String strVegetable_NameMarathi, String strVendor_Id) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(UpdateVegetableActivity.this, "", "Uploading...", false, false);

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
//        Log.e("TAG", ": in upload:fileUri " + fileUri);
//        File file = FileUtilsNew.getFile(getApplicationContext(), fileUri);
//
//        Log.e("TAG", ": in upload:file " + file);
//        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);
//
//        // MultipartBody.Part is used to send also the actual file name
//        MultipartBody.Part body =
//                MultipartBody.Part.createFormData("Attachment", file.getName(), requestFile);

        // add another part within the multipart request
        RequestBody Vegetable_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_Id);
        RequestBody Vegetable_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_Name);
        RequestBody Vegetable_Name_Marathi = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_NameMarathi);
        RequestBody Vendor_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strVendor_Id);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateVegetable(Vegetable_Id, Vegetable_Name,Vegetable_Name_Marathi, Vendor_Id);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateVegetableActivity.this);
                builder1.setCancelable(false);
                builder1.setTitle("Success!!");
                builder1.setMessage("Vegetable Details Updated successfully.");
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        Intent i = new Intent(UpdateVegetableActivity.this, AdminDashboardActivity.class);
//                        startActivity(i);
//                        finish();
                        onBackPressed();

                    }
                });
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                loading.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "onFailure: UPLOAD ERROR " + t);
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


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            if (data != null && data.getData() != null) {
                filePath = data.getData();
                try {
                    FilesList = new ArrayList<>();

                    filePathName = FileUtilsNew.getPath(getApplicationContext(), filePath);
                    File pathwithoutfile = new File(filePathName);
                    onlyPathName = String.valueOf(FileUtilsNew.getPathWithoutFilename(pathwithoutfile));
                    Log.e("Single file path ", filePathName);
                    Log.e("file path 3 ", onlyPathName);
                    Log.e("absolute path in gal ", pathwithoutfile.getAbsolutePath());
                    pathWithoutFile.add(onlyPathName);
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    add_img.setImageBitmap(bitmap1);
                    new ImageCompressionAsyncTask(this).execute(filePath.toString(),
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Eureka/images");

                    files.add("Profile");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
                img_add.setVisibility(View.VISIBLE);
                tv_Addphoto.setVisibility(View.VISIBLE);
                add_img.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
            img_add.setVisibility(View.VISIBLE);
            tv_Addphoto.setVisibility(View.VISIBLE);
            add_img.setVisibility(View.GONE);
        }

    }

    class ImageCompressionAsyncTask extends AsyncTask<String, Void, String> {

        Context mContext;

        public ImageCompressionAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {

            return SiliCompressor.with(mContext).compress(params[0], new File(params[1]));

        }

        @SuppressLint("Range")
        @Override
        protected void onPostExecute(String s) {

            float length = 0;
            String name;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                compressUri = Uri.parse(s);
                Cursor c = getContentResolver().query(compressUri, null, null, null, null);
                c.moveToFirst();
                name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                length = c.getLong(c.getColumnIndex(OpenableColumns.SIZE)) / 1024;

                FilesList.add(compressUri);

            } else {
                File imageFile = new File(s);
                compressUri = Uri.fromFile(imageFile);
                name = imageFile.getName();
                length = imageFile.length() / 1024f; // Size in KB

                FilesList.add(compressUri);
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri);
//                imageView.setImageBitmap(bitmap);
                int compressWidth = bitmap.getWidth();
                int compressHieght = bitmap.getHeight();
                String text = String.format(Locale.US, "Name: %s\nSize: %fKB\nWidth: %d\nHeight: %d", name, length, compressWidth, compressHieght);
//                picDescription.setVisibility(View.VISIBLE);
//                picDescription.setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void selectSpinnerValue(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(myString)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

}