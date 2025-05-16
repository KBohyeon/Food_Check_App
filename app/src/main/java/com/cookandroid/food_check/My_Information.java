package com.cookandroid.food_check;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class My_Information extends AppCompatActivity {

    private static final String TAG = "My_Information";
    private PieChart pieChart;
    private TextView tvTotalCalories;
    private String userID;
    private static final int RECOMMENDED_DAILY_CALORIES = 2000; // 일일 권장 칼로리 (기본값)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_information);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.info_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("오늘의 칼로리");
        }

        // 사용자 ID 가져오기
        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
        userID = pref.getString("userID", "");

        // 로그 추가: 사용자 ID 확인
        Log.d(TAG, "사용자 ID: " + userID);

        // 뷰 초기화
        pieChart = findViewById(R.id.piechart);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);

        if (userID.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 차트 초기화
        setupPieChart();

        // 사용자 칼로리 데이터 로드
        loadUserCaloriesData();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
    }

    private void loadUserCaloriesData() {
        String url = "http://192.168.45.197/foodcheck/get_user_calories.php?userID=" + userID;

        // 로그 추가: URL 확인
        Log.d(TAG, "요청 URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 로그 추가: 전체 응답 확인
                        Log.d(TAG, "서버 응답: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            // 로그 추가: 파싱된 응답 확인
                            Log.d(TAG, "success 값: " + jsonResponse.getBoolean("success"));

                            if (jsonResponse.getBoolean("success")) {
                                int totalCalories = jsonResponse.getInt("totalCalories");
                                JSONArray foodsArray = jsonResponse.getJSONArray("foods");

                                // 로그 추가: 칼로리 및 음식 개수 확인
                                Log.d(TAG, "총 칼로리: " + totalCalories + ", 음식 항목 수: " + foodsArray.length());

                                // 총 칼로리 표시
                                tvTotalCalories.setText("오늘 섭취한 칼로리: " + totalCalories + " kcal");

                                // 파이 차트 데이터 생성
                                updatePieChart(totalCalories, foodsArray);
                            } else {
                                String message = jsonResponse.has("message") ?
                                        jsonResponse.getString("message") : "알 수 없는 오류";
                                Log.e(TAG, "데이터 로딩 실패: " + message);
                                Toast.makeText(My_Information.this, "데이터 로딩 실패: " + message, Toast.LENGTH_SHORT).show();

                                // 빈 차트 표시
                                ArrayList<PieEntry> emptyEntries = new ArrayList<>();
                                emptyEntries.add(new PieEntry(1, "데이터 없음"));
                                updateEmptyChart(emptyEntries);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JSON 파싱 오류", e);
                            Toast.makeText(My_Information.this, "데이터 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();

                            // 빈 차트 표시
                            ArrayList<PieEntry> emptyEntries = new ArrayList<>();
                            emptyEntries.add(new PieEntry(1, "에러 발생"));
                            updateEmptyChart(emptyEntries);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 로그 추가: 자세한 오류 정보
                String errorMessage = "알 수 없는 오류";
                if (error.networkResponse != null) {
                    errorMessage = "상태 코드: " + error.networkResponse.statusCode;
                } else if (error.getMessage() != null) {
                    errorMessage = error.getMessage();
                }

                Log.e(TAG, "Volley 오류: " + errorMessage, error);
                Toast.makeText(My_Information.this, "서버 연결 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();

                // 빈 차트 표시
                ArrayList<PieEntry> emptyEntries = new ArrayList<>();
                emptyEntries.add(new PieEntry(1, "연결 오류"));
                updateEmptyChart(emptyEntries);
            }
        });

        // 타임아웃 설정 (5초)
        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                5000,
                com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);
    }

    // 빈 차트 업데이트 메서드 추가
    private void updateEmptyChart(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "데이터 없음");
        dataSet.setColors(Color.LTGRAY);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updatePieChart(int totalCalories, JSONArray foodsArray) {
        try {
            ArrayList<PieEntry> entries = new ArrayList<>();

            //먹은 음식들의 칼로리 항목
            if (foodsArray.length() > 0) {
                for (int i = 0; i < foodsArray.length(); i++) {
                    JSONObject foodItem = foodsArray.getJSONObject(i);
                    String foodName = foodItem.getString("name");
                    int calories = foodItem.getInt("calories");

                    // 로그 추가: 각 음식 항목 확인
                    Log.d(TAG, "음식: " + foodName + ", 칼로리: " + calories);

                    // 차트에 5% 이상 차지하는 항목만 개별 표시
                    float percentage = (float) calories / RECOMMENDED_DAILY_CALORIES * 100;
                    if (percentage >= 5.0f) {
                        entries.add(new PieEntry(calories, foodName));
                    }
                }
            }

            //남은 권장 칼로리 항목
            int remainingCalories = RECOMMENDED_DAILY_CALORIES - totalCalories;
            if (remainingCalories > 0) {
                entries.add(new PieEntry(remainingCalories, "남은 칼로리"));
            } else if (remainingCalories < 0) {
                // 권장량 초과
                entries.add(new PieEntry(Math.abs(remainingCalories), "초과 칼로리"));
            }

            // 데이터가 없는 경우
            if (entries.isEmpty()) {
                entries.add(new PieEntry(1, "데이터 없음"));
            }

            PieDataSet dataSet = new PieDataSet(entries, "칼로리 분석");

            // 색상
            ArrayList<Integer> colors = new ArrayList<>();
            for (int color : ColorTemplate.MATERIAL_COLORS) {
                colors.add(color);
            }
            for (int color : ColorTemplate.VORDIPLOM_COLORS) {
                colors.add(color);
            }

            // 권장량 초과 칼로리는 빨간색으로
            if (remainingCalories < 0) {
                colors.add(Color.RED);
            }

            // 남은 칼로리는 회색으로
            if (remainingCalories > 0) {
                colors.add(Color.LTGRAY);
            }

            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter(pieChart));
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.BLACK);

            pieChart.setData(data);
            pieChart.highlightValues(null);
            pieChart.invalidate();

            // 애니메이션
            pieChart.animateY(1000);

        } catch (Exception e) {
            Log.e(TAG, "차트 업데이트 오류", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}