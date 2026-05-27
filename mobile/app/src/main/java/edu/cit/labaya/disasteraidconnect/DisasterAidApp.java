package edu.cit.labaya.disasteraidconnect;

import android.app.Application;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;

public class DisasterAidApp extends Application {

    private static DisasterAidApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.init(this);
    }

    public static DisasterAidApp getInstance() {
        return instance;
    }
}
