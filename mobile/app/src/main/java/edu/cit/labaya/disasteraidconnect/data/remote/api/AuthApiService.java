package edu.cit.labaya.disasteraidconnect.data.remote.api;

// NOTE: Authentication is handled by Supabase directly (not through Spring Boot).
// The Android app will call Supabase Auth REST API for login/register,
// then use the returned JWT token in all Spring Boot API requests.
// Supabase Auth endpoint: https://wvwvxwkbjnvsrvnmrxsr.supabase.co/auth/v1/

import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.LoginRequestDTO;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.RegisterRequestDTO;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.response.AuthResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("token?grant_type=password")
    Call<AuthResponseDTO> login(@Body LoginRequestDTO request);

    @POST("signup")
    Call<AuthResponseDTO> register(@Body RegisterRequestDTO request);
}
