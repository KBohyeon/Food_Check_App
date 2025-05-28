package com.cookandroid.food_check;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText et_id, et_pass;
    Button btn_login, btn_register;
    TextView resultText;

    String loginUrl = "http://192.168.45.197/foodcheck/Login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
//        resultText = findViewById(R.id.textView_main_result); // 로그인 결과 출력용

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = et_id.getText().toString().trim();
                String userPassword = et_pass.getText().toString().trim();

                if (userID.isEmpty() || userPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                login(userID, userPassword);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login(String userID, String userPassword) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, loginUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LOGIN_RESPONSE", response);
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray userArray = json.getJSONArray("user");
                            if (userArray.length() > 0) {
                                JSONObject user = userArray.getJSONObject(0);
                                String id = user.getString("userID");
                                String sort = user.getString("userSort");

                                Toast.makeText(LoginActivity.this, id + "님 환영합니다!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("loginID", id);
                                intent.putExtra("loginSort", sort);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "예외 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("LOGIN_ERROR", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN_ERROR", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("userPassword", userPassword);
                return params;
            }
        };

        queue.add(request);
    }
}
