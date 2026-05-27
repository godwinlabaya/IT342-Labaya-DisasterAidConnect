package edu.cit.labaya.disasteraidconnect.data.remote.api;

import java.util.List;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.DonationRequestDTO;
import retrofit2.Call;
import retrofit2.http.*;

public interface DonationApiService {

    // GET /api/donations
    @GET("donations")
    Call<List<Donation>> getAll();

    // GET /api/donations/{id}
    @GET("donations/{id}")
    Call<Donation> getById(@Path("id") String id);

    // GET /api/donations/user/{userId}
    @GET("donations/user/{userId}")
    Call<List<Donation>> getByUser(@Path("userId") String userId);

    // GET /api/donations/disaster/{disasterId}
    @GET("donations/disaster/{disasterId}")
    Call<List<Donation>> getByDisaster(@Path("disasterId") String disasterId);

    // POST /api/donations
    @POST("donations")
    Call<Donation> create(@Body DonationRequestDTO request);

    // PATCH /api/donations/{id}/status?status=Completed
    @PATCH("donations/{id}/status")
    Call<Donation> updateStatus(@Path("id") String id, @Query("status") String status);
}
