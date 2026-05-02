package com.example.doscord.api;

public class PfpRequest {
    private String userId;
    private String pfp;

    public PfpRequest(String userId, String pfp) {
        this.userId = userId;
        this.pfp = pfp;
    }

    // Getters (Retrofit/Gson uses these to build the JSON)
    public String getUserId() { return userId; }
    public String getPfp() { return pfp; }
}