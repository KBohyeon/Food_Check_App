package com.cookandroid.food_check;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

// 음식 정보 저장 API
public interface FoodApi {
    @Multipart
    @POST("save_food.php")
    Call<FoodSaveResponse> saveFoodData(
            @Part("userID") RequestBody userID,
            @Part("foods") RequestBody foods
    );
}