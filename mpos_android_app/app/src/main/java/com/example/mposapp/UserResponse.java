package com.example.mposapp;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("token")
    String token;
    @SerializedName("user_id")
    int userId;
}
