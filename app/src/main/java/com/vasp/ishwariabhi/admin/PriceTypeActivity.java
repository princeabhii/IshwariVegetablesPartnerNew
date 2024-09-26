package com.vasp.ishwariabhi.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.vasp.ishwariabhi.R;

public class PriceTypeActivity extends AppCompatActivity {

    CardView cvHotel,cvVendor;
    LinearLayout lvBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_type);

        cvHotel = findViewById(R.id.cvHotel);
        cvVendor = findViewById(R.id.cvVendor);
        lvBack = findViewById(R.id.lvBack);

        lvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cvHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PriceTypeActivity.this, ViewUserPriceActivity.class));
            }
        });

        cvVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PriceTypeActivity.this, VendorPriceCategoryActivity.class));
            }
        });

    }
}