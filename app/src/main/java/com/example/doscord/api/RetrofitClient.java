package com.example.doscord.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://doscord.top/api/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            // Build the DNS over HTTPS client
            OkHttpClient bootstrapClient = new OkHttpClient.Builder().build();
            Dns dns = new DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(HttpUrl.get("https://dns.google/dns-query"))
                    .bootstrapDnsHosts(getGoogleDns())
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .dns(dns) // Force the app to use Google's encrypted DNS
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static List<InetAddress> getGoogleDns() {
        try {
            // This is the call you asked about!
            // It converts the string "8.8.8.8" into a machine-readable address.
            return Collections.singletonList(InetAddress.getByName("8.8.8.8"));
        } catch (UnknownHostException e) {
            // This should technically never happen for a hardcoded IP string,
            // but Java requires the catch.
            return Collections.emptyList();
        }
    }
}