package com.vasp.ishwariabhi.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.vasp.ishwariabhi.R;

public class VendorPriceCategoryActivity extends AppCompatActivity {

    CardView cvSingleVendor,cvMultiVendor;
    LinearLayout lvBack;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_category);

        cvSingleVendor = findViewById(R.id.cvSingleVendor);
        cvMultiVendor = findViewById(R.id.cvMultiVendor);
        lvBack = findViewById(R.id.lvBack);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cvSingleVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VendorPriceCategoryActivity.this, ViewPricesActivity.class));
            }
        });

        cvMultiVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VendorPriceCategoryActivity.this, ViewQuantityVendorActivity.class));
            }
        });

    }
}