package edu.cit.labaya.disasteraidconnect.data.repository;

import androidx.lifecycle.MutableLiveData;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.remote.api.AuthApiService;
import edu.cit.labaya.disasteraidconnect.data.remote.api.SupabaseAuthClient;
import edu.cit.labaya.disasteraidconnect.data.remote.api.SupabaseDbClient;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.LoginRequestDTO;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.RegisterRequestDTO;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.response.AuthResponseDTO;
import edu.cit.labaya.disasteraidconnect.utils.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApiService authService = SupabaseAuthClient.getAuthService();

    /**
     * Login flow mirrors Login.js:
     * 1. supabase.auth.signInWithPassword → get session token + userId
     * 2. Query users table for role
     * 3. Save to SessionManager
     */
    public void login(String email, String password,
                      MutableLiveData<Resource<AuthResponseDTO>> result) {
        result.setValue(Resource.loading());
        authService.login(new LoginRequestDTO(email, password))
            .enqueue(new Callback<AuthResponseDTO>() {
                @Override
                public void onResponse(Call<AuthResponseDTO> call,
                                       Response<AuthResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponseDTO body = response.body();
                        String userId = body.getUser() != null ? body.getUser().getId() : null;
                        String userEmail = body.getUser() != null ? body.getUser().getEmail() : email;
                        String token = body.getAccessToken();

                        // Fetch role from users table (matches Login.js behavior)
                        if (userId != null) {
                            SupabaseDbClient.getUserRole(userId, token, role -> {
                                // Also fetch username so SessionManager.getUsername() works
                                fetchUsernameAndSave(userId, token, role, email, result, body);
                            }, error -> {
                                SessionManager.getInstance().saveSession(token, userId, "user", email);
                                result.setValue(Resource.success(body));
                            });
                        } else {
                            result.setValue(Resource.error("Login failed: no user returned"));
                        }
                    } else {
                        result.setValue(Resource.error("Invalid email or password"));
                    }
                }
                @Override
                public void onFailure(Call<AuthResponseDTO> call, Throwable t) {
                    result.setValue(Resource.error(t.getMessage()));
                }
            });
    }

    /**
     * Register flow mirrors Register.js:
     * 1. supabase.auth.signUp
     * 2. Insert into users table with username, security_question, security_answer
     */
    public void register(String email, String password, String username,
                         String securityQuestion, String securityAnswer,
                         MutableLiveData<Resource<AuthResponseDTO>> result) {
        result.setValue(Resource.loading());
        authService.register(new RegisterRequestDTO(email, password, username))
            .enqueue(new Callback<AuthResponseDTO>() {
                @Override
                public void onResponse(Call<AuthResponseDTO> call,
                                       Response<AuthResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponseDTO body = response.body();
                        String userId = body.getUser() != null ? body.getUser().getId() : null;
                        String token = body.getAccessToken();

                        if (userId != null && token != null) {
                            // Insert into users table (mirrors Register.js supabase.from("users").insert)
                            SupabaseDbClient.insertUser(userId, username, email,
                                securityQuestion, securityAnswer, token,
                                () -> result.postValue(Resource.success(body)),
                                error -> result.postValue(Resource.error("Account created but profile setup failed: " + error))
                            );
                        } else {
                            result.setValue(Resource.success(body));
                        }
                    } else {
                        result.setValue(Resource.error("Registration failed. Email may already be in use."));
                    }
                }
                @Override
                public void onFailure(Call<AuthResponseDTO> call, Throwable t) {
                    result.setValue(Resource.error(t.getMessage()));
                }
            });
    }

    private void fetchUsernameAndSave(String userId, String token, String role,
                                      String email, MutableLiveData<Resource<AuthResponseDTO>> result,
                                      AuthResponseDTO body) {
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url("https://wvwvxwkbjnvsrvnmrxsr.supabase.co/rest/v1/" +
                                "users?select=username&id=eq." + userId)
                        .header("apikey",
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                                        "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0." +
                                        "HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg")
                        .header("Authorization", "Bearer " + token)
                        .build();
                okhttp3.Response resp = client.newCall(req).execute();
                String username = email; // fallback to email if fetch fails
                if (resp.isSuccessful() && resp.body() != null) {
                    org.json.JSONArray arr = new org.json.JSONArray(resp.body().string());
                    if (arr.length() > 0) {
                        username = arr.getJSONObject(0).optString("username", email);
                    }
                }
                SessionManager.getInstance().saveSession(token, userId, role, email);
                SessionManager.getInstance().saveUsername(username);
                result.postValue(Resource.success(body));
            } catch (Exception e) {
                SessionManager.getInstance().saveSession(token, userId, role, email);
                result.postValue(Resource.success(body));
            }
        }).start();
    }
}
