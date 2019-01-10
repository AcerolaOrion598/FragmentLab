package com.djaphar.fragmentlab;

import com.google.gson.annotations.SerializedName;

public class AccessToken {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;


    public String getToken() {
        return accessToken;
    }
}
