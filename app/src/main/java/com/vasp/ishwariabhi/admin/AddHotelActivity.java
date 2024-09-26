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
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iceteck.silicompressorr.SiliCompressor;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddHotelActivity extends AppCompatActivity {
    LinearLayout lvAddHotel;
    ProgressBar progressbar;
    ImageView ivBack, imgContact;
    EditText edHotelName, edPhone, edEmail, edPassword;
    String strEmail, strPassword, User_Id, User_Name, User_Type, strPhone, strHotelName;
    ;
    UserSession userSession;
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
    int SELECT_PHONE_NUMBER = 7715;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_details);

        edPhone = findViewById(R.id.edPhone);
        edHotelName = findViewById(R.id.edHotelName);
        lvAddHotel = findViewById(R.id.lvAddHotel);
        progressbar = findViewById(R.id.progressbar);
        ivBack = findViewById(R.id.ivBack);
        edPassword = findViewById(R.id.edPassword);
        edEmail = findViewById(R.id.edEmail);
        ivBack = findViewById(R.id.ivBack);
        progressbar = findViewById(R.id.progressbar);
        CoverPicture = findViewById(R.id.CoverPicture);
        add_img = findViewById(R.id.add_img);
        img_add = findViewById(R.id.img_add);
        tv_Addphoto = findViewById(R.id.tv_Addphoto);
        imgContact = findViewById(R.id.imgContact);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);

        userSession = new UserSession(getApplicationContext());
        User_Id = userSession.getUserId();
        User_Name = userSession.getUserName();
        User_Type = "User";

        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

        imgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(i, SELECT_PHONE_NUMBER);
            }
        });

        lvAddHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strHotelName = edHotelName.getText().toString().trim();
                strPhone = edPhone.getText().toString().trim();
                strPassword = edPassword.getText().toString().trim();
                strEmail = edEmail.getText().toString().trim();

                if (edPhone.getText().toString().trim().equals("") || edPhone.getText().toString().trim().length() < 10) {
                    Toast.makeText(getApplicationContext(), "Enter Your Mobile No", Toast.LENGTH_LONG).show();
                } else if (edPassword.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Your Password", Toast.LENGTH_LONG).show();
                } else if (edHotelName.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Your HotelName", Toast.LENGTH_LONG).show();
                } else if (edEmail.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Your Email", Toast.LENGTH_LONG).show();
                } else {
                    if (files.size() > 0) {
                        uploadFile(FilesList.get(0), strEmail, strPassword, strPhone, strHotelName);
                    } else {
                        Toast.makeText(AddHotelActivity.this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
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

    private void uploadFile(Uri fileUri, String strEmail, String strPassword, String strPhone, String strHotelName) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(AddHotelActivity.this, "", "Uploading...", false, false);

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
        RequestBody Hotel_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strHotelName);
        RequestBody Email_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strEmail);
        RequestBody Password = RequestBody.create(okhttp3.MultipartBody.FORM, strPassword);
        RequestBody Phone = RequestBody.create(okhttp3.MultipartBody.FORM, strPhone);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.AddHotel(Hotel_Name, Email_Id, Phone, Password, body);
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

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(AddHotelActivity.this);
                    builder1.setCancelable(false);
                    builder1.setTitle("Success!!");
                    builder1.setMessage("Hotel Details Added Successfully.");
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
//                            Intent i = new Intent(AddVegetableActivity.this, AdminCategoriesActivity.class);
//                            startActivity(i);
//                            finish();
                            Intent i = new Intent();
                            setResult(9282, i);
                            finish();

                        }
                    });
                    AlertDialog dialog1 = builder1.create();
                    dialog1.show();
                } else {
                    Toast.makeText(AddHotelActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
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
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Ishwari_Vegetables/images");
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
        } else if (requestCode == SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
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
}

//    private void RegisterDetails(String hotelname, String phone, String password, String email, String User_Type) {
//        final ProgressDialog loading = ProgressDialog.show(AddHotelActivity.this, "", "Login..", false, false);
//
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(Config.APP_SERVER_URL)
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .build();
//
//        AllApi mApiService = retrofit.create(AllApi.class);
//        Call<ReturnedResponsePojo> mService = mApiService.Register(hotelname, phone, password, email, User_Type);
//        mService.enqueue(new Callback<ReturnedResponsePojo>() {
//
//            @Override
//            public void onResponse(Call<ReturnedResponsePojo> call, Response<ReturnedResponsePojo> response) {
//                loading.dismiss();
//                if (response.isSuccessful()) {
//                    ReturnedResponsePojo jsonResponse = response.body();
//                    String success = jsonResponse.success;
//                    String message = jsonResponse.message;
//
//                    if (success.equals("1")) {
//                        Toast.makeText(getApplicationContext(), "User Added Successfully!!! ", Toast.LENGTH_LONG).show();
//                        Intent i = new Intent(AddHotelActivity.this, AdminDashboard.class);
//                        startActivity(i);
//                        finish();
//
////
//                    } else if (success.equals("2")) {
//                        Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
//                    } else if (success.equals("0")) {
//                        Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    switch (response.code()) {
//                        case 404:
//                            Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();
//                            break;
//                        case 500:
//                            Toast.makeText(getApplicationContext(), "server broken", Toast.LENGTH_SHORT).show();
//                            break;
//                        default:
//                            Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ReturnedResponsePojo> call, Throwable t) {
//                loading.dismiss();
//                Toast.makeText(getApplicationContext(), "We are not able to connect right now. Please try again later.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//    public void onClick(DialogInterface arg0, int arg1) {
//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);
//    }
