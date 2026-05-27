package edu.cit.labaya.disasteraidconnect.ui.aidrequest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.cit.labaya.disasteraidconnect.R;
import edu.cit.labaya.disasteraidconnect.data.model.AidRequest;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class AidRequestActivity extends AppCompatActivity {

    private AidRequestViewModel viewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText etDisasterId, etType, etDescription;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid_request);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        etDisasterId = findViewById(R.id.etDisasterId);
        etType = findViewById(R.id.etType);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(AidRequestViewModel.class);
        viewModel.loadMyRequests();

        viewModel.aidRequests.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                progressBar.setVisibility(View.GONE);
                List<AidRequest> list = resource.data != null
                        ? resource.data : new ArrayList<>();
                recyclerView.setAdapter(new AidRequestAdapter(list));
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.submitResult.observe(this, resource -> {
            if (resource.status == edu.cit.labaya.disasteraidconnect.utils.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Aid request submitted!", Toast.LENGTH_SHORT).show();
                viewModel.loadMyRequests();
            } else if (resource.status == edu.cit.labaya.disasteraidconnect.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String disasterId = etDisasterId.getText().toString().trim();
            String type = etType.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (disasterId.isEmpty() || type.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.submit(disasterId, type, description);
        });
    }
}
