package edu.cit.labaya.disasteraidconnect.data.remote.api;

import java.util.List;
import edu.cit.labaya.disasteraidconnect.data.model.AidRequest;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.AidRequestRequestDTO;
import retrofit2.Call;
import retrofit2.http.*;

public interface AidRequestApiService {

    // GET /api/aid-requests
    @GET("aid-requests")
    Call<List<AidRequest>> getAll();

    // GET /api/aid-requests/{id}
    @GET("aid-requests/{id}")
    Call<AidRequest> getById(@Path("id") String id);

    // GET /api/aid-requests/user/{userId}
    @GET("aid-requests/user/{userId}")
    Call<List<AidRequest>> getByUser(@Path("userId") String userId);

    // GET /api/aid-requests/disaster/{disasterId}
    @GET("aid-requests/disaster/{disasterId}")
    Call<List<AidRequest>> getByDisaster(@Path("disasterId") String disasterId);

    // POST /api/aid-requests
    @POST("aid-requests")
    Call<AidRequest> create(@Body AidRequestRequestDTO request);

    // PATCH /api/aid-requests/{id}/status?status=Approved
    @PATCH("aid-requests/{id}/status")
    Call<AidRequest> updateStatus(@Path("id") String id, @Query("status") String status);

    // DELETE /api/aid-requests/{id}
    @DELETE("aid-requests/{id}")
    Call<Void> delete(@Path("id") String id);
}
