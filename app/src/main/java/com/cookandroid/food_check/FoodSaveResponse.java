package com.cookandroid.food_check;

import com.google.gson.annotations.SerializedName;


 //음식 저장 응답 클래스

public class FoodSaveResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}