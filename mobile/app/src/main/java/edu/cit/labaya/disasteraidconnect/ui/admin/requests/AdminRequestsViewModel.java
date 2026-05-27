package edu.cit.labaya.disasteraidconnect.ui.admin.requests;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.data.model.AidRequest;
import edu.cit.labaya.disasteraidconnect.data.repository.AidRequestRepository;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class AdminRequestsViewModel extends ViewModel {
    private final AidRequestRepository repository = new AidRequestRepository();
    public final MutableLiveData<Resource<List<AidRequest>>> requests = new MutableLiveData<>();

    public void loadAllRequests() {
        repository.getAll(requests);
    }
}