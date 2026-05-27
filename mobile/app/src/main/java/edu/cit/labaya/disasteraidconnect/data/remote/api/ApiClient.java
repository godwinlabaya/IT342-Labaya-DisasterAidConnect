package edu.cit.labaya.disasteraidconnect.data.remote.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;

public class ApiClient {

    // Production backend on Render (matches DonationService.js)
    private static final String BASE_URL =
        "https://it342-labaya-disasteraidconnect.onrender.com/api/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = SessionManager.getInstance().getToken();
                    Request.Builder builder = chain.request().newBuilder()
                        .header("Content-Type", "application/json")
                        .header("ngrok-skip-browser-warning", "true");
                    if (token != null) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .addInterceptor(logging)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }

    public static <T> T create(Class<T> service) {
        return getClient().create(service);
    }
}
