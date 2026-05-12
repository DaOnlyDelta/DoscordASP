package com.example.doscord.api;

public class UpdateResponse {
    private boolean updateRequired;
    private int errorCode;

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public int getErrorCode() {
        return errorCode;
    }
}