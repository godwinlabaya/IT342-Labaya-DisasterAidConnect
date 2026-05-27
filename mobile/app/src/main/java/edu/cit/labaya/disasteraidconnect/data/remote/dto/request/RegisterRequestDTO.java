package edu.cit.labaya.disasteraidconnect.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class RegisterRequestDTO {
    private String email;
    private String password;

    // Supabase signup accepts data for user_metadata
    @SerializedName("data")
    private Map<String, String> data;

    public RegisterRequestDTO(String email, String password, String username) {
        this.email    = email;
        this.password = password;
        this.data     = Map.of("username", username);
    }

    public String getEmail()              { return email; }
    public String getPassword()           { return password; }
    public Map<String, String> getData()  { return data; }
}
