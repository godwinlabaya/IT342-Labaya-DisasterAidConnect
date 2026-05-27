package edu.cit.labaya.disasteraidconnect.data.remote.api;

import edu.cit.labaya.disasteraidconnect.data.model.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserApiService {

    // GET /api/users/{id}  — only endpoint in UserController
    @GET("users/{id}")
    Call<User> getById(@Path("id") String id);
}
