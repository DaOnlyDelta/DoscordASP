package com.example.doscord.api;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("logout")
    Call<Void> logout(@Body LogoutRequest request);

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
            @Part MultipartBody.Part pfp
    );

    @POST("send-friend-request")
    Call<FriendRequestResponse> sendFriendRequest(@Body FriendRequestRequest request);

    @POST("handle-friend-request")
    Call<Void> handleFriendRequest(@Body Map<String, Object> body);

    @GET("token-login")
    Call<TokenLoginResponse> tokenLogin(@Query("token") String token);

    @POST("get-messages")
    Call<MessagesResponse> getMessages(@Body MessagesRequest request);

    @POST("get-older-messages")
    Call<MessagesResponse> getOlderMessages(@Body OlderMessagesRequest request);

    @POST("get-new-messages")
    Call<MessagesResponse> getNewMessages(@Body NewMessagesRequest request);

    @Multipart
    @POST("send-message")
    Call<Void> sendMessage(
            @Part MultipartBody.Part channel_id,
            @Part MultipartBody.Part sender_id,
            @Part MultipartBody.Part message_text,
            @Part List<MultipartBody.Part> files
    );

    @POST("get-updates")
    Call<UpdateResponse> checkUpdates(@Body UpdateRequest request);
}