package com.example.doscord;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/check-username")
    Call<CheckResponse> checkUsername(@Body CheckRequest request);

    @POST("api/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}