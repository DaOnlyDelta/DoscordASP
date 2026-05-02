package com.example.doscord.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("check-username")
    Call<CheckResponse> checkUsername(@Body CheckRequest request);

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // For default avatar selection
    @POST("update-pfp-default")
    Call<Void> updatePfpDefault(@Body PfpRequest request);

    // For gallery upload
    @Multipart
    @POST("update-pfp-custom")
    Call<Void> updatePfpCustom(
            @Part("userId") RequestBody userId,
            @Part MultipartBody.Part image
    );
}