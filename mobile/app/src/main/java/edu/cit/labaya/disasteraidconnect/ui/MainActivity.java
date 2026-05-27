package edu.cit.labaya.disasteraidconnect.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.auth.login.LoginActivity;
import edu.cit.labaya.disasteraidconnect.ui.dashboard.DashboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.getInstance().isLoggedIn()) {
            Intent intent = SessionManager.getInstance().isAdmin()
                    ? new Intent(this, AdminDashboardActivity.class)
                    : new Intent(this, DashboardActivity.class);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}