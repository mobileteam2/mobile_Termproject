package com.example.mobile_termproject.Acitivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.mobile_termproject.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PriceDetailActivity extends AppCompatActivity {

    private String productName, formattedPrice, imageUrl, productUrl, itemName;
    private TextView tvName, tvPrice, tvChartNotice, tvMinPrice, tvMaxPrice;
    private ImageView ivImage;
    private Button btnBuy;
    private LineChart priceChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_detail);

        tvName = findViewById(R.id.tvDetailName);
        tvPrice = findViewById(R.id.tvDetailPrice);
        ivImage = findViewById(R.id.ivDetailImage);
        btnBuy = findViewById(R.id.btnBuy);
        tvChartNotice = findViewById(R.id.tvChartNotice);
        tvMinPrice = findViewById(R.id.tvMinPrice);
        tvMaxPrice = findViewById(R.id.tvMaxPrice);
        priceChart = findViewById(R.id.priceChart);

        Intent intent = getIntent();
        productName = intent.getStringExtra("productName");
        formattedPrice = intent.getStringExtra("formattedPrice");
        imageUrl = intent.getStringExtra("imageUrl");
        productUrl = intent.getStringExtra("productUrl");
        itemName = intent.getStringExtra("itemName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로 가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        tvName.setText(productName);
        tvPrice.setText("현재가: " + formattedPrice);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .into(ivImage);

        btnBuy.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(productUrl));
            startActivity(browserIntent);
        });

        loadPriceHistory();
    }

    private void loadPriceHistory() {
        String safeName = itemName.replaceAll("[.#$/\\[\\]]", "_");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("price_history")
                .document(safeName)
                .collection("prices")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Entry> entries = new ArrayList<>();
                    float minPrice = Float.MAX_VALUE;
                    float maxPrice = Float.MIN_VALUE;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp ts = doc.getTimestamp("timestamp");
                        Long price = doc.getLong("price");
                        if (ts != null && price != null) {
                            float x = ts.toDate().getTime();
                            float y = price.floatValue();
                            entries.add(new Entry(x, y));

                            minPrice = Math.min(minPrice, y);
                            maxPrice = Math.max(maxPrice, y);
                        }
                    }

                    if (entries.isEmpty()) {
                        priceChart.setVisibility(View.GONE);
                        tvChartNotice.setVisibility(View.VISIBLE);
                        tvMinPrice.setVisibility(View.GONE);
                        tvMaxPrice.setVisibility(View.GONE);
                    } else {
                        priceChart.setVisibility(View.VISIBLE);
                        tvChartNotice.setVisibility(View.GONE);
                        tvMinPrice.setVisibility(View.VISIBLE);
                        tvMaxPrice.setVisibility(View.VISIBLE);

                        tvMinPrice.setText(Html.fromHtml(
                                String.format("<font color='#337FD5'>최저가: %,d원</font>", (int) minPrice),
                                Html.FROM_HTML_MODE_LEGACY
                        ));
                        tvMaxPrice.setText(Html.fromHtml(
                                String.format("<font color='#D55C5A'>최고가: %,d원</font>", (int) maxPrice),
                                Html.FROM_HTML_MODE_LEGACY
                        ));

                        drawPriceChart(entries);
                    }
                });
    }

    private void drawPriceChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "가격 추이");
        dataSet.setColor(getResources().getColor(R.color.md_theme_light_primary));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        priceChart.setData(lineData);

        XAxis xAxis = priceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.KOREA);
            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });

        priceChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%,d원", (int) value);
            }
        });

        priceChart.getXAxis().setDrawGridLines(false);
        priceChart.getAxisLeft().setDrawGridLines(false);
        priceChart.getAxisRight().setEnabled(false);
        priceChart.setExtraBottomOffset(8f);
        priceChart.getDescription().setEnabled(false);
        priceChart.getLegend().setEnabled(false);
        priceChart.animateY(500);
        priceChart.invalidate();
    }
}
