package com.cookandroid.food_check;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 101;

    private ImageView imageView;
    private TextView resultText;
    private Button btnGallery;
    private Button btnSave;

    private String serverUrl = "http://192.168.45.197:8001/"; // YOLO 서버 URL
    private String phpUrl = "http://192.168.45.197/foodcheck/save_food.php"; // PHP 파일 URL

    private java.util.List<YoloResponse.Result> foodResults; // 감지된 음식 결과 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 툴바
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);  // 버튼 제거
        }

        // 로그인 정보 저장 - Intent에서 사용자 ID 가져와 SharedPreferences에 저장
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("loginID")) {
            String loginID = intent.getStringExtra("loginID");
            String loginSort = intent.getStringExtra("loginSort");

            // SharedPreferences에 저장
            SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("userID", loginID);
            editor.putString("userSort", loginSort);
            editor.apply();
        }

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        btnGallery = findViewById(R.id.btnGallery);
        btnSave = findViewById(R.id.btnSave); // onCreate 내부에서 초기화
        btnSave.setVisibility(View.GONE); // 기본 숨김

        // 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        btnGallery.setOnClickListener(v -> openGallery());

        // 버튼 저장 클릭 리스너 추가
        btnSave.setOnClickListener(v -> {
            if (foodResults != null && !foodResults.isEmpty()) {
                saveFoodToDB();
            } else {
                Toast.makeText(MainActivity.this, "저장할 음식 데이터가 없습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 옵션 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 옵션 메뉴 선택 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent infoIntent = new Intent(MainActivity.this, My_Information.class);
                startActivity(infoIntent);
                return true;

            case R.id.action_logout:
                SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY && data != null) {
            Uri uri = data.getData();
            imageView.setImageURI(uri);
            uploadImage(uri);
        }
    }

    private void uploadImage(Uri uri) {
        Log.d("YOLO", "이미지 업로드 시작");
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] imageBytes = getBytes(inputStream);

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "upload.jpg", reqFile);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.45.197:8001/") //욜로 fastApi IP
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            YoloApi api = retrofit.create(YoloApi.class);
            Call<YoloResponse> call = api.predict(body);

            call.enqueue(new Callback<YoloResponse>() {
                @Override
                public void onResponse(Call<YoloResponse> call, Response<YoloResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        foodResults = response.body().getResults(); // 결과 저장

                        StringBuilder sb = new StringBuilder();
                        for (YoloResponse.Result item : foodResults) {
                            sb.append(item.getLabel()).append(" : ").append(item.getCalories()).append(" kcal\n");
                        }
                        resultText.setText(sb.toString());

                        // 결과 이미지 표시
                        String imageUrl = "http://192.168.45.197:8001/" + response.body().getImage();
                        RequestOptions options = new RequestOptions()
                                .override(300, 300)
                                .format(DecodeFormat.PREFER_RGB_565)
                                .dontAnimate();

                        Glide.with(MainActivity.this)
                                .setDefaultRequestOptions(options)
                                .load(imageUrl)
                                .into(imageView);

                        // 결과가 나왔으므로 저장 버튼 표시
                        btnSave.setVisibility(View.VISIBLE);
                    } else {
                        resultText.setText("서버 오류: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<YoloResponse> call, Throwable t) {
                    resultText.setText("서버 통신 실패: " + t.getMessage());
                    Log.e("YOLO", "통신 실패", t);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            resultText.setText("이미지 처리 중 오류");
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

     //음식 정보를 DB에 저장
    private void saveFoodToDB() {
        try {
            // 유저 ID 가져오기
            SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
            String userID = pref.getString("userID", "");

            if (userID.isEmpty()) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();

                // 로그인 화면으로 이동
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }

            // 음식 데이터를 JSON 배열로 변환
            JSONArray foodArray = new JSONArray();
            for (YoloResponse.Result food : foodResults) {
                JSONObject foodObject = new JSONObject();
                foodObject.put("label", food.getLabel());
                foodObject.put("calories", food.getCalories());
                foodArray.put(foodObject);
            }

            // HTTP 요청 생성
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(phpUrl.substring(0, phpUrl.lastIndexOf("/") + 1))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // 요청 전송
            FoodApi foodApi = retrofit.create(FoodApi.class);
            RequestBody userIDBody = RequestBody.create(MediaType.parse("text/plain"), userID);
            RequestBody foodsBody = RequestBody.create(MediaType.parse("text/plain"), foodArray.toString());

            Call<FoodSaveResponse> call = foodApi.saveFoodData(userIDBody, foodsBody);
            call.enqueue(new Callback<FoodSaveResponse>() {
                @Override
                public void onResponse(Call<FoodSaveResponse> call, Response<FoodSaveResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            Toast.makeText(MainActivity.this, "음식 정보가 저장되었습니다", Toast.LENGTH_SHORT).show();
                            // 저장 후 버튼 다시 숨기기
                            btnSave.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MainActivity.this, "저장 실패: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FoodSaveResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FOOD_SAVE", "통신 실패", t);
                }
            });

        } catch (JSONException e) {
            Toast.makeText(this, "JSON 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}