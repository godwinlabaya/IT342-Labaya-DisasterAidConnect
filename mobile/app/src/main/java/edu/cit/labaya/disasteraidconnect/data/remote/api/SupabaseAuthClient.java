package edu.cit.labaya.disasteraidconnect.data.remote.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseAuthClient {

    private static final String SUPABASE_URL =
        "https://wvwvxwkbjnvsrvnmrxsr.supabase.co/auth/v1/";

    // Real anon key from supabaseClient.js
    private static final String SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
        "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
        "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(
                    chain.request().newBuilder()
                        .header("Content-Type", "application/json")
                        .header("apikey", SUPABASE_ANON_KEY)
                        .build()
                ))
                .addInterceptor(logging)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }

    public static AuthApiService getAuthService() {
        return getClient().create(AuthApiService.class);
    }
}
