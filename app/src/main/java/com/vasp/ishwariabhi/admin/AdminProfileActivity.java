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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.vasp.ishwariabhi.Config;
import com.vasp.ishwariabhi.FileUtilsNew;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.api.AllApi;
import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;
import com.vasp.ishwariabhi.session.UserSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminProfileActivity extends AppCompatActivity {

    TextView tvLogout;
    TextInputEditText edtPassword, edtPhone, edtEmail, edtName;
    CircleImageView imgProfile;
    UserSession userSession;
    String strUser_Id, strAttachment;
    CardView cvUpdateProfile;
    ProgressDialog progressDialog;
    ImageView add_img;
    Uri filePath = null;
    private static final int PICK_IMAGE_REQUEST = 11;
    private String filePathName = null;
    private String onlyPathName = null;
    private ArrayList<Uri> FilesList = new ArrayList<>();
    private ArrayList<String> files = new ArrayList<>();
    private ArrayList<String> pathWithoutFile = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 200;
    Uri compressUri = null;
    LinearLayout lvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        userSession = new UserSession(AdminProfileActivity.this);
        strUser_Id = userSession.getUserId();

        tvLogout = findViewById(R.id.tvLogout);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtName = findViewById(R.id.edtName);
        cvUpdateProfile = findViewById(R.id.cvUpdateProfile);
        add_img = findViewById(R.id.add_img);
        imgProfile = findViewById(R.id.imgProfile);
        lvBack = findViewById(R.id.lvBack);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (strAttachment != null) {
            add_img.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(strAttachment)
                    .placeholder(R.drawable.noimage)
                    .into(imgProfile);

        } else {

            Picasso.get()
                    .load(R.drawable.noimage)
                    .placeholder(R.drawable.noimage)
                    .into(imgProfile);

        }

        ActivityCompat.requestPermissions(AdminProfileActivity.this, new String[]{ACCESS_FINE_LOCATION, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);

        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });


        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSession.logoutUser();
            }
        });

        cvUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPassword, strPhone, strName, strEmail;
                strPassword = edtPassword.getText().toString();
                strPhone = edtPhone.getText().toString();
                strName = edtName.getText().toString();
                strEmail = edtEmail.getText().toString();

                if (strName.equals("")) {
                    Toast.makeText(AdminProfileActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                } else if (strEmail.equals("")) {
                    Toast.makeText(AdminProfileActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (strPhone.equals("")) {
                    Toast.makeText(AdminProfileActivity.this, "Enter Mobile No.", Toast.LENGTH_SHORT).show();
                } else if (strPassword.equals("")) {
                    Toast.makeText(AdminProfileActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else {
                    if (FilesList.size()>0){
                        updateProfile(FilesList.get(0), strUser_Id, strName, strEmail, strPhone, strPassword);
                    }else {
                        updateProfileWithoutImage(strUser_Id, strName, strEmail, strPhone, strPassword);
                    }
                }

            }
        });

        getUserProfile();

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

    private void getUserProfile() {

        progressDialog = ProgressDialog.show(AdminProfileActivity.this, "", "Please Wait...", true, false);


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
        Call<ReturnedResponsePojo> mService = mApiService.GetUserProfile(strUser_Id);

        mService.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {

                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    ReturnedResponsePojo jsonResponse = response.body();
                    String success = jsonResponse.success;
                    String message = jsonResponse.message;

                    if (success.equals("1")) {

                        edtName.setText(jsonResponse.Mgmt_Name);
                        edtPassword.setText(jsonResponse.Password);
                        edtEmail.setText(jsonResponse.Email_Id);
                        edtPhone.setText(jsonResponse.Mobile_No);

                        if (jsonResponse.Attachment != null) {
                            Picasso.get()
                                    .load(jsonResponse.Attachment)
                                    .placeholder(R.drawable.noimage)
                                    .into(imgProfile);

                        } else {

                            Picasso.get()
                                    .load(R.drawable.noimage)
                                    .placeholder(R.drawable.noimage)
                                    .into(imgProfile);

                        }

                    } else {
                        Toast.makeText(AdminProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(AdminProfileActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(AdminProfileActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AdminProfileActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(AdminProfileActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminProfileActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });
    }

    private void updateProfile(Uri fileUri, String strUser_Id, String strName, String strEmail, String strPhone, String strPassword) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(AdminProfileActivity.this, "", "Uploading...", false, false);

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
        File file = FileUtilsNew.getFile(AdminProfileActivity.this, fileUri);

        Log.e("TAG", ": in upload:file " + file);
        RequestBody requestFile = RequestBody.create(MediaType.parse(AdminProfileActivity.this.getContentResolver().getType(fileUri)), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part Attachment =
                MultipartBody.Part.createFormData("Attachment", file.getName(), requestFile);

        // add another part within the multipart request
        RequestBody User_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strUser_Id);
        RequestBody Mgmt_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strName);
        RequestBody Email_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strEmail);
        RequestBody Mobile_No = RequestBody.create(okhttp3.MultipartBody.FORM, strPhone);
        RequestBody Password = RequestBody.create(okhttp3.MultipartBody.FORM, strPassword);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateUserProfile(User_Id, Mgmt_Name, Email_Id, Mobile_No, Password, Attachment);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(AdminProfileActivity.this);
                builder1.setCancelable(false);
                builder1.setTitle("Success!!");
                builder1.setMessage("Profile Updated successfully.");
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        Intent refresh = new Intent();
//                        startActivity(refresh);//Start the same Activity
//                        finish();
                        Intent i = new Intent();
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
                    Toast.makeText(AdminProfileActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "onFailure: UPLOAD ERROR " + t);
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminProfileActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });
    }
    private void updateProfileWithoutImage(String strUser_Id, String strName, String strEmail, String strPhone, String strPassword) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(AdminProfileActivity.this, "", "Uploading...", false, false);

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
        RequestBody User_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strUser_Id);
        RequestBody Mgmt_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strName);
        RequestBody Email_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strEmail);
        RequestBody Mobile_No = RequestBody.create(okhttp3.MultipartBody.FORM, strPhone);
        RequestBody Password = RequestBody.create(okhttp3.MultipartBody.FORM, strPassword);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.UpdateUserProfile(User_Id, Mgmt_Name, Email_Id, Mobile_No, Password);
        call.enqueue(new Callback<ReturnedResponsePojo>() {
            @Override
            public void onResponse(Call<ReturnedResponsePojo> call,
                                   Response<ReturnedResponsePojo> response) {
                Log.e("TAG", "onResponse: in upload:" + response.toString());
                Log.v("Upload", "success");
                loading.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(AdminProfileActivity.this);
                builder1.setCancelable(false);
                builder1.setTitle("Success!!");
                builder1.setMessage("Profile Updated successfully.");
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        Intent refresh = new Intent();
//                        startActivity(refresh);//Start the same Activity
//                        finish();
                        Intent i = new Intent();
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
                    Toast.makeText(AdminProfileActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "onFailure: UPLOAD ERROR " + t);
                    // logging probably not necessary
                } else {
                    Toast.makeText(AdminProfileActivity.this, "Error " + t, Toast.LENGTH_SHORT).show();
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

                    filePathName = FileUtilsNew.getPath(AdminProfileActivity.this, filePath);
                    File pathwithoutfile = new File(filePathName);
                    onlyPathName = String.valueOf(FileUtilsNew.getPathWithoutFilename(pathwithoutfile));
                    Log.e("Single file path ", filePathName);
                    Log.e("file path 3 ", onlyPathName);
                    Log.e("absolute path in gal ", pathwithoutfile.getAbsolutePath());
                    pathWithoutFile.add(onlyPathName);
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(AdminProfileActivity.this.getContentResolver(), filePath);
                    imgProfile.setImageBitmap(bitmap1);
                    new ImageCompressionAsyncTask(AdminProfileActivity.this).execute(filePath.toString(),
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Eureka/images");

                    files.add("Profile");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AdminProfileActivity.this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
                add_img.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(AdminProfileActivity.this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
            add_img.setVisibility(View.VISIBLE);
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
                Cursor c = AdminProfileActivity.this.getContentResolver().query(compressUri, null, null, null, null);
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(AdminProfileActivity.this.getContentResolver(), compressUri);
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
}