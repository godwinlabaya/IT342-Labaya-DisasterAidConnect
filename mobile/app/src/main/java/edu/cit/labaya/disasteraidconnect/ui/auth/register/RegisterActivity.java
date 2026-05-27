package edu.cit.labaya.disasteraidconnect.ui.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.ui.auth.login.LoginActivity;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword, etSecurityAnswer;
    private Spinner spSecurityQuestion;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;

    // Security questions from Register.js
    private static final String[] SECURITY_QUESTIONS = {
        "What is your mother's maiden name?",
        "What is your childhood nickname?",
        "What is your first pet's name?",
        "What is your favorite color?",
        "What city were you born in?",
        "What is the name of your elementary school?",
        "What was the make of your first car?"
    };

    // Password regex from Register.js: 8+ chars, one uppercase, one special char
    private static final java.util.regex.Pattern PASSWORD_REGEX =
        java.util.regex.Pattern.compile("^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername        = findViewById(R.id.etUsername);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etSecurityAnswer  = findViewById(R.id.etSecurityAnswer);
        spSecurityQuestion = findViewById(R.id.spSecurityQuestion);
        btnRegister       = findViewById(R.id.btnRegister);
        progressBar       = findViewById(R.id.progressBar);
        tvLogin           = findViewById(R.id.tvLogin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, SECURITY_QUESTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSecurityQuestion.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etConfirmPassword.getText().toString().trim();
            String question = spSecurityQuestion.getSelectedItem().toString();
            String answer   = etSecurityAnswer.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()
                    || confirm.isEmpty() || answer.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!PASSWORD_REGEX.matcher(password).matches()) {
                Toast.makeText(this,
                    "Password must be 8+ characters with an uppercase letter and special character",
                    Toast.LENGTH_LONG).show();
                return;
            }
            viewModel.register(email, password, username, question, answer);
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        viewModel.registerResult.observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnRegister.setEnabled(false);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                        "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}
