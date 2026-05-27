package edu.cit.labaya.disasteraidconnect.ui.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.ui.admin.AdminDashboardActivity;
import edu.cit.labaya.disasteraidconnect.ui.auth.register.RegisterActivity;
import edu.cit.labaya.disasteraidconnect.ui.dashboard.DashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already logged in, skip login screen
        SessionManager sm = SessionManager.getInstance();
        if (sm.getToken() != null && !sm.getToken().isEmpty()) {
            navigateByRole(sm.getRole(), sm.getUsername(), false);
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        progressBar= findViewById(R.id.progressBar);
        tvRegister = findViewById(R.id.tvRegister);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        viewModel.loginResult.observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    String role     = SessionManager.getInstance().getRole();
                    String username = SessionManager.getInstance().getUsername();
                    navigateByRole(role, username, true);
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /**
     * Mirrors web behavior:
     * - Admin  → "Welcome back, Admin! Redirecting..." → AdminDashboard
     * - User   → "Welcome back, [username]! Redirecting..." → Dashboard
     */
    private void navigateByRole(String role, String username, boolean showToast) {
        if (showToast) {
            String name = username != null ? username : "User";
            String msg  = "admin".equals(role)
                    ? "Welcome back, Admin! Redirecting..."
                    : "Welcome back, " + name + "! Redirecting...";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

        Intent intent = "admin".equals(role)
                ? new Intent(this, AdminDashboardActivity.class)
                : new Intent(this, DashboardActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}