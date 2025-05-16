package com.cookandroid.food_check;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface YoloApi {
    @Multipart
    @POST("/predict")
    Call<YoloResponse> predict(@Part MultipartBody.Part file);
}
