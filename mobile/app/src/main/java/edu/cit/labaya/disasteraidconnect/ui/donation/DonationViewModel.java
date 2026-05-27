package edu.cit.labaya.disasteraidconnect.ui.donation;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.Donation;
import edu.cit.labaya.disasteraidconnect.data.repository.DonationRepository;

public class DonationViewModel extends ViewModel {

    private final DonationRepository repo = new DonationRepository();

    public final MutableLiveData<List<Donation>> donations = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> error = new MutableLiveData<>();

    public void loadDonations() {
        isLoading.setValue(true);
        repo.getDonationsByUser(list -> {
            donations.postValue(list);
            isLoading.postValue(false);
        }, err -> {
            error.postValue(err);
            isLoading.postValue(false);
        });
    }

    public void logDonation(String disasterId, String disasterTitle,
                            double amount, String status) {
        repo.recordDonation(disasterId, amount,
                donationId -> loadDonations(),
                err -> error.postValue(err));
    }
}