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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.FileUtilsNew;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class UpdateAdminActivity extends AppCompatActivity {

    String strUser_Id, strMgmt_Name, strPassword, strEmail_Id, strMobile_No, strAttachment,strIsEnabled,strIs_Enabled;
    EditText edAdminName, edEmail, edPassword, edPhone;
    LinearLayout lvUpdateAdmin, lvDisableAdmin, lvEnableAdmin;
    ProgressBar progressbar;
    RelativeLayout CoverPicture, rlDisableAdmin, rlEnableAdmin;
    ImageView add_img, img_add, ivBack,imgContact;
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
    CardView cvShareWhatsapp,cvUpdateAdmin;
    int SELECT_PHONE_NUMBER = 7715;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);
        
        Intent i = getIntent();
        strUser_Id = i.getStringExtra("User_Id");
        strMgmt_Name = i.getStringExtra("Mgmt_Name");
        strPassword = i.getStringExtra("Password");
        strEmail_Id = i.getStringExtra("Email_Id");
        strMobile_No = i.getStringExtra("Mobile_No");
        strAttachment = i.getStringExtra("Attachment");
        strIsEnabled = i.getStringExtra("Is_Enabled");
        Log.e("TAG", "onCreate: IsEnabbled" + strIsEnabled);

        edAdminName = findViewById(R.id.edAdminName);
        edPhone = findViewById(R.id.edPhone);
        edPassword = findViewById(R.id.edPassword);
        edEmail = findViewById(R.id.edEmail);
        lvUpdateAdmin = findViewById(R.id.lvUpdateAdmin);
        progressbar = findViewById(R.id.progressbar);
        CoverPicture = findViewById(R.id.CoverPicture);
        rlEnableAdmin = findViewById(R.id.rlEnableAdmin);
        lvEnableAdmin = findViewById(R.id.lvEnableAdmin);
        rlDisableAdmin = findViewById(R.id.rlDisableAdmin);
        lvDisableAdmin = findViewById(R.id.lvDisableAdmin);
        add_img = findViewById(R.id.add_img);
        img_add = findViewById(R.id.img_add);
        ivBack = findViewById(R.id.ivBack);
        tv_Addphoto = findViewById(R.id.tv_Addphoto);
        cvShareWhatsapp = findViewById(R.id.cvShareWhatsapp);
        cvUpdateAdmin = findViewById(R.id.cvUpdateAdmin);
        imgContact = findViewById(R.id.imgContact);
//        spnCategoriesList = findViewById(R.id.spnCategoriesList);

        edAdminName.setText(strMgmt_Name);
        edPhone.setText(strMobile_No);
        edPassword.setText(strPassword);
        edEmail.setText(strEmail_Id);

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

        if (strIsEnabled.equals("Y")) {
            rlDisableAdmin.setVisibility(View.VISIBLE);
            rlEnableAdmin.setVisibility(View.GONE);
        } else if (strIsEnabled.equals("N")) {
            rlDisableAdmin.setVisibility(View.GONE);
            rlEnableAdmin.setVisibility(View.VISIBLE);
        }

        imgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(i, SELECT_PHONE_NUMBER);
            }
        });

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

        cvShareWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDetailsOnWhatsapp(strEmail_Id,strPassword);
            }
        });

        lvDisableAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rlDisableAdmin.setVisibility(View.GONE);
                rlEnableAdmin.setVisibility(View.VISIBLE);
                strIs_Enabled = "N";

                ChangeAdminStatus(strUser_Id, strIs_Enabled);
                Log.e("TAG", "onClick: MGMT_ID " + strUser_Id + " IS_ENABLED " + strIs_Enabled);

            }
        });
        lvEnableAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rlDisableAdmin.setVisibility(View.VISIBLE);
                rlEnableAdmin.setVisibility(View.GONE);
                strIs_Enabled = "Y";

                ChangeAdminStatus(strUser_Id, strIs_Enabled);
            }
        });

        cvUpdateAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strMgmt_Name = edAdminName.getText().toString().trim();
                strPassword = edPassword.getText().toString().trim();
                strEmail_Id = edEmail.getText().toString().trim();
                strMobile_No = edPhone.getText().toString().trim();

                if (strMgmt_Name.equals("")) {
                    Toast.makeText(UpdateAdminActivity.this, "Enter Admin Name", Toast.LENGTH_SHORT).show();
                } else if (strPassword.equals("")) {
                    Toast.makeText(UpdateAdminActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (strEmail_Id.equals("")) {
                    Toast.makeText(UpdateAdminActivity.this, "Enter Email Id", Toast.LENGTH_SHORT).show();
                } else if (strMobile_No.equals("")) {
                    Toast.makeText(UpdateAdminActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else {
                    if (files.size() > 0) {
                        updateAdmins(FilesList.get(0), strMgmt_Name, strPassword, strEmail_Id, strMobile_No);
                    } else {
                        updateAdminsWithoutImage(strMgmt_Name, strPassword, strEmail_Id, strMobile_No);

//                        Toast.makeText(UpdateAdminActivity.this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

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

    private void updateAdmins(Uri fileUri, String strMgmt_Name, String strPassword, String strEmail_Id, String strMobile_No) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(UpdateAdminActivity.this, "", "Uploading...", false, false);

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
        RequestBody TextName = RequestBody.create(okhttp3.MultipartBody.FORM, strMgmt_Name);
        RequestBody TextedPassword = RequestBody.create(okhttp3.MultipartBody.FORM, strPassword);
        RequestBody TextEmail_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strEmail_Id);
        RequestBody TextedPhone = RequestBody.create(okhttp3.MultipartBody.FORM, strMobile_No);
        RequestBody TextUser_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strUser_Id);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateAdmin( TextUser_Id,TextName, TextedPassword, TextEmail_Id, TextedPhone, body);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateAdminActivity.this);
                builder1.setCancelable(false);
                builder1.setTitle("Success!!");
                builder1.setMessage("Admin Details Updated successfully.");
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

    private void updateAdminsWithoutImage(String strMgmt_Name, String strPassword, String strEmail_Id, String strMobile_No) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(UpdateAdminActivity.this, "", "Uploading...", false, false);

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
        RequestBody TextName = RequestBody.create(okhttp3.MultipartBody.FORM, strMgmt_Name);
        RequestBody TextedPassword = RequestBody.create(okhttp3.MultipartBody.FORM, strPassword);
        RequestBody TextEmail_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strEmail_Id);
        RequestBody TextedPhone = RequestBody.create(okhttp3.MultipartBody.FORM, strMobile_No);
        RequestBody TextUser_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strUser_Id);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateAdmin(TextUser_Id,TextName, TextedPassword, TextEmail_Id, TextedPhone);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateAdminActivity.this);
                builder1.setCancelable(false);
                builder1.setTitle("Success!!");
                builder1.setMessage("Admin Details Updated successfully.");
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        Intent i = new Intent(UpdateAdminActivity.this, AdminDashboardActivity.class);
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

    private void ChangeAdminStatus(String strUser_Id, String strIs_Enabled) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(UpdateAdminActivity.this, "", "Loading...", false, false);

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


        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.ChangeAdminStatus(strUser_Id, strIs_Enabled);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String status = "";
                    if (strIs_Enabled.equals("Y")) {
                        status = "Enabled";
                    } else {
                        status = "Disabled";
                    }

                    if (success.equals("1")) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateAdminActivity.this);
                        builder1.setCancelable(false);
                        builder1.setTitle("Success!!");
                        builder1.setMessage("Admin Status " + status + ".");
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
//                                onBackPressed();

                            }
                        });
                        AlertDialog dialog1 = builder1.create();
                        dialog1.show();
                    } else {
                        Toast.makeText(UpdateAdminActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                }
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
                    new UpdateAdminActivity.ImageCompressionAsyncTask(this).execute(filePath.toString(),
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
        }
        else if (requestCode == SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);

            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberIndex);
                edPhone.setText("" + number);
                // Do something with the phone number
            }

            cursor.close();
        }else {
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

    public void shareDetailsOnWhatsapp(String User_Id, String Password) {

        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Welcome To Ishwari Vegetables Admin App.\n\nDownload our app from https://play.google.com/store/apps/details?id=com.vasp.ishwarivegetablespartner \n\nHere are your login details : \nUser Id : "+User_Id+"\nPassword : "+Password;

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

}