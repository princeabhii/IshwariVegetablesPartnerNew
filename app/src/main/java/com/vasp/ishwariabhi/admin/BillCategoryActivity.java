package com.vasp.ishwariabhi.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.vasp.ishwariabhi.R;

public class BillCategoryActivity extends AppCompatActivity {

    CardView cvHotelBills, cvVendorBills;
    LinearLayout lvBack;
    TextView tvTitle;
    String strTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_category);

        Intent i = getIntent();
        strTitle = i.getStringExtra("Title");

        cvHotelBills = findViewById(R.id.cvHotelBills);
        cvVendorBills = findViewById(R.id.cvVendorBills);
        lvBack = findViewById(R.id.lvBack);
        tvTitle = findViewById(R.id.tvTitle);

        tvTitle.setText("" + strTitle);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cvHotelBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (strTitle.equals("Bill Category")) {
                    startActivity(new Intent(BillCategoryActivity.this, UserMonthlyBillsActivity.class));
                }else{
                    startActivity(new Intent(BillCategoryActivity.this, ManagePaymentsActivity.class));
                }
            }
        });

        cvVendorBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (strTitle.equals("Bill Category")) {
                    startActivity(new Intent(BillCategoryActivity.this, VendorMonthlyBillsActivity.class));
                }else{
                    startActivity(new Intent(BillCategoryActivity.this, ManageVendorPaymentsActivity.class));
                }
            }
        });

    }
}