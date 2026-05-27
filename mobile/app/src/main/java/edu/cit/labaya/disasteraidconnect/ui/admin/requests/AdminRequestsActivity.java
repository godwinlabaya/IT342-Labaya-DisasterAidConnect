package edu.cit.labaya.disasteraidconnect.ui.admin.requests;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.cit.labaya.disasteraidconnect.R;

public class AdminRequestsActivity extends AppCompatActivity {

    private AdminRequestsViewModel viewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(AdminRequestsViewModel.class);
        viewModel.loadAllRequests();

        viewModel.requests.observe(this, resource -> {
            if (resource.status == edu.cit.labaya.disasteraidconnect.utils.Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
