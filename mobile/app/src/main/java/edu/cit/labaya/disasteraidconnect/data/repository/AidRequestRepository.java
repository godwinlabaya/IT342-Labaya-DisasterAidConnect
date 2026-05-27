package edu.cit.labaya.disasteraidconnect.data.repository;

import androidx.lifecycle.MutableLiveData;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.AidRequest;
import edu.cit.labaya.disasteraidconnect.data.remote.api.AidRequestApiService;
import edu.cit.labaya.disasteraidconnect.data.remote.api.ApiClient;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.AidRequestRequestDTO;
import edu.cit.labaya.disasteraidconnect.utils.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AidRequestRepository {

    private final AidRequestApiService apiService = ApiClient.create(AidRequestApiService.class);

    public void getAll(MutableLiveData<Resource<List<AidRequest>>> result) {
        result.setValue(Resource.loading());
        apiService.getAll().enqueue(new Callback<List<AidRequest>>() {
            @Override
            public void onResponse(Call<List<AidRequest>> call, Response<List<AidRequest>> response) {
                if (response.isSuccessful()) result.setValue(Resource.success(response.body()));
                else result.setValue(Resource.error("Failed to load aid requests"));
            }
            @Override
            public void onFailure(Call<List<AidRequest>> call, Throwable t) {
                result.setValue(Resource.error(t.getMessage()));
            }
        });
    }

    public void getByUser(String userId, MutableLiveData<Resource<List<AidRequest>>> result) {
        result.setValue(Resource.loading());
        apiService.getByUser(userId).enqueue(new Callback<List<AidRequest>>() {
            @Override
            public void onResponse(Call<List<AidRequest>> call, Response<List<AidRequest>> response) {
                if (response.isSuccessful()) result.setValue(Resource.success(response.body()));
                else result.setValue(Resource.error("Failed to load your requests"));
            }
            @Override
            public void onFailure(Call<List<AidRequest>> call, Throwable t) {
                result.setValue(Resource.error(t.getMessage()));
            }
        });
    }

    public void create(String disasterId, String aidType, String description,
                       String quantity, MutableLiveData<Resource<AidRequest>> result) {
        result.setValue(Resource.loading());
        String userId = SessionManager.getInstance().getUserId();
        AidRequestRequestDTO dto = new AidRequestRequestDTO(
            userId, disasterId, description, aidType, quantity
        );
        apiService.create(dto).enqueue(new Callback<AidRequest>() {
            @Override
            public void onResponse(Call<AidRequest> call, Response<AidRequest> response) {
                if (response.isSuccessful()) result.setValue(Resource.success(response.body()));
                else result.setValue(Resource.error("Failed to submit aid request"));
            }
            @Override
            public void onFailure(Call<AidRequest> call, Throwable t) {
                result.setValue(Resource.error(t.getMessage()));
            }
        });
    }

    public void updateStatus(String id, String status,
                              MutableLiveData<Resource<AidRequest>> result) {
        result.setValue(Resource.loading());
        apiService.updateStatus(id, status).enqueue(new Callback<AidRequest>() {
            @Override
            public void onResponse(Call<AidRequest> call, Response<AidRequest> response) {
                if (response.isSuccessful()) result.setValue(Resource.success(response.body()));
                else result.setValue(Resource.error("Failed to update status"));
            }
            @Override
            public void onFailure(Call<AidRequest> call, Throwable t) {
                result.setValue(Resource.error(t.getMessage()));
            }
        });
    }

    public void delete(String id, MutableLiveData<Resource<Void>> result) {
        result.setValue(Resource.loading());
        apiService.delete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) result.setValue(Resource.success(null));
                else result.setValue(Resource.error("Failed to delete"));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(Resource.error(t.getMessage()));
            }
        });
    }
}
