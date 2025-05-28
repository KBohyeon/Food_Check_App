package com.cookandroid.food_check;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private static final int REQUEST_CAMERA = 102;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private ImageView imageView;
    private TextView resultText;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnSave;
    private ProgressBar progressBar;

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

        // 뷰 초기화
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        btnSave.setVisibility(View.GONE); // 기본 숨김

        // 권한 확인 및 요청
        checkAndRequestPermissions();

        // 버튼 클릭 리스너
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener(v -> {
            if (foodResults != null && !foodResults.isEmpty()) {
                saveFoodToDB();
            } else {
                Toast.makeText(MainActivity.this, "저장할 음식 데이터가 없습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 권한 확인 및 요청
    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    // 로딩 시작
    private void showLoadingState() {
        runOnUiThread(() -> {
            resultText.setText("🔍 이미지 분석 중...\n잠시만 기다려주세요");
            resultText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            progressBar.setVisibility(View.VISIBLE);  // 프로그레스바 표시
            btnSave.setVisibility(View.GONE);
        });
    }

    // 로딩 완료
    private void hideLoadingState() {
        runOnUiThread(() -> {
            resultText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            progressBar.setVisibility(View.GONE);  // 프로그레스바 숨김
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

    // 카메라 열기
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "카메라를 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    // 갤러리 열기
    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // 갤러리에서 선택한 이미지 표시
                    imageView.setImageURI(uri);
                    uploadImageFromUri(uri);
                }
            } else if (requestCode == REQUEST_CAMERA && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        // 카메라에서 촬영한 이미지 표시
                        imageView.setImageBitmap(imageBitmap);
                        uploadImageFromBitmap(imageBitmap);
                    }
                }
            }
        }
    }

    // URI에서 이미지 업로드
    private void uploadImageFromUri(Uri uri) {
        Log.d("YOLO", "갤러리 이미지 업로드 시작");
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] imageBytes = getBytes(inputStream);
            uploadImageBytes(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            resultText.setText("이미지 처리 중 오류");
            Log.e("YOLO", "갤러리 이미지 처리 오류", e);
        }
    }

    // Bitmap에서 이미지 업로드
    private void uploadImageFromBitmap(Bitmap bitmap) {
        Log.d("YOLO", "카메라 이미지 업로드 시작");
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] imageBytes = stream.toByteArray();
            uploadImageBytes(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            resultText.setText("이미지 처리 중 오류");
            Log.e("YOLO", "카메라 이미지 처리 오류", e);
        }
    }

    // 이미지 바이트 배열을 서버에 업로드
    private void uploadImageBytes(byte[] imageBytes) {
        // 로딩 시작
        showLoadingState();

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "upload.jpg", reqFile);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        YoloApi api = retrofit.create(YoloApi.class);
        Call<YoloResponse> call = api.predict(body);

        call.enqueue(new Callback<YoloResponse>() {
            @Override
            public void onResponse(Call<YoloResponse> call, Response<YoloResponse> response) {
                // 로딩 완료
                hideLoadingState();

                if (response.isSuccessful() && response.body() != null) {
                    foodResults = response.body().getResults();

                    // 결과 텍스트 표시
                    StringBuilder sb = new StringBuilder();
                    if (foodResults != null && !foodResults.isEmpty()) {
                        sb.append("✅ 분석 완료!\n\n");
                        for (YoloResponse.Result item : foodResults) {
                            sb.append("🍽️ ").append(item.getLabel())
                                    .append(" : ").append(item.getCalories()).append(" kcal\n");
                        }
                    } else {
                        sb.append("❌ 음식을 감지하지 못했습니다.\n다른 각도에서 촬영해보세요.");
                    }
                    resultText.setText(sb.toString());

                    // 결과 이미지 표시
                    String imageUrl = serverUrl + response.body().getImage();
                    Log.d("RESULT_IMAGE", "결과 이미지 URL: " + imageUrl);

                    displayResultImage(imageUrl);

                    // 결과가 나왔으므로 저장 버튼 표시
                    if (foodResults != null && !foodResults.isEmpty()) {
                        btnSave.setVisibility(View.VISIBLE);
                    }
                } else {
                    resultText.setText("❌ 서버 오류가 발생했습니다\n다시 시도해주세요");
                    Log.e("YOLO", "서버 응답 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<YoloResponse> call, Throwable t) {
                // 로딩 완료
                hideLoadingState();

                resultText.setText("❌ 서버 연결에 실패했습니다\n인터넷 연결을 확인해주세요");
                Log.e("YOLO", "통신 실패", t);
            }
        });
    }

    // 결과 이미지를 ImageView에 표시
    private void displayResultImage(String imageUrl) {
        try {
            RequestOptions options = new RequestOptions()
                    .fitCenter()  // 전체 이미지가 보이도록
                    .format(DecodeFormat.PREFER_RGB_565);

            Glide.with(MainActivity.this)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView);

            Log.d("RESULT_IMAGE", "결과 이미지 표시 완료");
        } catch (Exception e) {
            Log.e("RESULT_IMAGE", "결과 이미지 표시 실패", e);
        }
    }

    // InputStream을 byte 배열로 변환
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // 음식 정보를 DB에 저장
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                Toast.makeText(this, "앱 사용을 위해 권한이 필요합니다", Toast.LENGTH_LONG).show();
            }
        }
    }
}