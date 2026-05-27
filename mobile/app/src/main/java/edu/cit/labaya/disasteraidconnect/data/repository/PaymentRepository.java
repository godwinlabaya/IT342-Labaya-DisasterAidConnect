package edu.cit.labaya.disasteraidconnect.data.repository;

import androidx.lifecycle.MutableLiveData;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.remote.api.ApiClient;
import edu.cit.labaya.disasteraidconnect.data.remote.api.PaymentApiService;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.request.PaymentRequestDTO;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.response.PaymentCheckoutResponseDTO;
import edu.cit.labaya.disasteraidconnect.utils.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {

    private final PaymentApiService apiService = ApiClient.create(PaymentApiService.class);

    /**
     * POST /api/payments/create
     * Matches PaymentController.createCheckout()
     * Returns { checkoutUrl, donationId }
     */
    public void createGCashCheckout(String disasterId, double amount,
                                     MutableLiveData<Resource<PaymentCheckoutResponseDTO>> result) {
        result.setValue(Resource.loading());
        String userId = SessionManager.getInstance().getUserId();

        apiService.createCheckout(new PaymentRequestDTO(userId, disasterId, amount))
            .enqueue(new Callback<PaymentCheckoutResponseDTO>() {
                @Override
                public void onResponse(Call<PaymentCheckoutResponseDTO> call,
                                       Response<PaymentCheckoutResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.setValue(Resource.success(response.body()));
                    } else {
                        result.setValue(Resource.error("Payment creation failed"));
                    }
                }
                @Override
                public void onFailure(Call<PaymentCheckoutResponseDTO> call, Throwable t) {
                    result.setValue(Resource.error(t.getMessage()));
                }
            });
    }
}
