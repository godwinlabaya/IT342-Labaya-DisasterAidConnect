package edu.cit.labaya.disasteraidconnect.data.remote.api;

import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.PaymentRequestDTO;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.response.PaymentCheckoutResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApiService {

    // POST /api/payments/create  → returns { checkoutUrl, donationId }
    @POST("payments/create")
    Call<PaymentCheckoutResponseDTO> createCheckout(@Body PaymentRequestDTO request);
}
