package edu.cit.labaya.disasteraidconnect.data.remote.api;

import java.util.List;
import edu.cit.labaya.disasteraidconnect.data.model.Disaster;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.DisasterRequestDTO;
import retrofit2.Call;
import retrofit2.http.*;

public interface DisasterApiService {

    // GET /api/disasters
    @GET("disasters")
    Call<List<Disaster>> getAll();

    // GET /api/disasters/{id}
    @GET("disasters/{id}")
    Call<Disaster> getById(@Path("id") String id);

    // GET /api/disasters/user/{userId}
    @GET("disasters/user/{userId}")
    Call<List<Disaster>> getByUser(@Path("userId") String userId);

    // POST /api/disasters
    @POST("disasters")
    Call<Disaster> create(@Body DisasterRequestDTO request);

    // PUT /api/disasters/{id}
    @PUT("disasters/{id}")
    Call<Disaster> update(@Path("id") String id, @Body DisasterRequestDTO request);

    // DELETE /api/disasters/{id}
    @DELETE("disasters/{id}")
    Call<Void> delete(@Path("id") String id);
}
