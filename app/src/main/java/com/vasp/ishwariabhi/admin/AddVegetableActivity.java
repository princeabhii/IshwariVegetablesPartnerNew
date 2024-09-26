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

public class AddVegetableActivity extends AppCompatActivity {

    EditText edVegetable, edVegetableNameMarathi;
    LinearLayout lvAddVegetables;
    String strName, strVegetableNameMarathi;
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
        setContentView(R.layout.activity_add_vegetable);

//        Intent i = getIntent();
//        strVendor_Id = i.getStringExtra("Vendors_Id");

        edVegetable = findViewById(R.id.edVegetableName);
        lvAddVegetables = findViewById(R.id.lvAddVegetables);
        ivBack = findViewById(R.id.ivBack);
        progressbar = findViewById(R.id.progressbar);
        CoverPicture = findViewById(R.id.CoverPicture);
        add_img = findViewById(R.id.add_img);
        img_add = findViewById(R.id.img_add);
        tv_Addphoto = findViewById(R.id.tv_Addphoto);
        edVegetableNameMarathi = findViewById(R.id.edVegetableNameMarathi);

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

        CoverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                img_add.setVisibility(View.GONE);
                tv_Addphoto.setVisibility(View.GONE);
                add_img.setVisibility(View.VISIBLE);
            }
        });

        lvAddVegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strName = edVegetable.getText().toString().trim();
                strVegetableNameMarathi = edVegetableNameMarathi.getText().toString().trim();

                if (strName.equals("")) {
                    Toast.makeText(AddVegetableActivity.this, "Enter Vegetable Name", Toast.LENGTH_SHORT).show();
                } else if (strVegetableNameMarathi.equals("")) {
                    Toast.makeText(AddVegetableActivity.this, "Enter Marathi Vegetable Name", Toast.LENGTH_SHORT).show();
                } else {
                    if (files.size() > 0) {
                        uploadFile(FilesList.get(0), strName,strVegetableNameMarathi);
                    } else {
                        Toast.makeText(AddVegetableActivity.this, "No Attachment Attached", Toast.LENGTH_SHORT).show();
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

    private void uploadFile(Uri fileUri, String strName,String strVegetable_NameMarathi) {
        // create upload service client
        final ProgressDialog loading = ProgressDialog.show(AddVegetableActivity.this, "", "Uploading...", false, false);

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
        RequestBody Vegetable_Name = RequestBody.create(okhttp3.MultipartBody.FORM, strName);
        RequestBody Vegetable_Name_Marathi = RequestBody.create(okhttp3.MultipartBody.FORM, strVegetable_NameMarathi);
//        RequestBody Vendor_Id = RequestBody.create(okhttp3.MultipartBody.FORM, strVendorId);

        // finally, execute the request
        Call<ReturnedResponsePojo> call = mApiService.AddVegetable(Vegetable_Name,Vegetable_Name_Marathi, body);
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

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(AddVegetableActivity.this);
                    builder1.setCancelable(false);
                    builder1.setTitle("Success!!");
                    builder1.setMessage("Vegetable Added Successfully.");
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
                    Toast.makeText(AddVegetableActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
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
}