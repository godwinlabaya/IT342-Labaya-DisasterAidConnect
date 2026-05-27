package edu.cit.labaya.disasteraidconnect.core.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME     = "DisasterAidPrefs";
    private static final String KEY_TOKEN     = "auth_token";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_ROLE      = "user_role";
    private static final String KEY_EMAIL     = "user_email";
    private static final String KEY_USERNAME  = "username";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) instance = new SessionManager(context.getApplicationContext());
    }

    public static SessionManager getInstance() { return instance; }

    public void saveSession(String token, String userId, String role, String email) {
        prefs.edit()
            .putString(KEY_TOKEN,   token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ROLE,    role)
            .putString(KEY_EMAIL,   email)
            .apply();
    }

    public void saveUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getToken()    { return prefs.getString(KEY_TOKEN,    null); }
    public String getUserId()   { return prefs.getString(KEY_USER_ID,  null); }
    public String getRole()     { return prefs.getString(KEY_ROLE,     "user"); }
    public String getEmail()    { return prefs.getString(KEY_EMAIL,    null); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }

    public boolean isLoggedIn() { return getToken() != null; }
    public boolean isAdmin()    { return "admin".equals(getRole()); }

    public void clearSession()  { prefs.edit().clear().apply(); }
}
