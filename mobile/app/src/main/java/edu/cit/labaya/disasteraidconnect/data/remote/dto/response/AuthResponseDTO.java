package edu.cit.labaya.disasteraidconnect.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponseDTO {

    // Supabase returns: access_token, token_type, user { id, email, ... }
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("user")
    private SupabaseUser user;

    public String getAccessToken() { return accessToken; }
    public SupabaseUser getUser()  { return user; }

    public static class SupabaseUser {
        private String id;
        private String email;

        @SerializedName("user_metadata")
        private UserMetadata userMetadata;

        public String getId()    { return id; }
        public String getEmail() { return email; }
        public UserMetadata getUserMetadata() { return userMetadata; }
    }

    public static class UserMetadata {
        private String role;
        public String getRole() { return role; }
    }
}
