package edu.cit.labaya.disasteraidconnect.ui.aidrequest;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import edu.cit.labaya.disasteraidconnect.core.session.SessionManager;
import edu.cit.labaya.disasteraidconnect.data.model.AidRequest;
import edu.cit.labaya.disasteraidconnect.data.repository.AidRequestRepository;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class AidRequestViewModel extends ViewModel {
    private final AidRequestRepository repository = new AidRequestRepository();
    public final MutableLiveData<Resource<List<AidRequest>>> aidRequests = new MutableLiveData<>();
    public final MutableLiveData<Resource<AidRequest>> submitResult = new MutableLiveData<>();

    public void loadMyRequests() {
        String userId = SessionManager.getInstance().getUserId();
        repository.getByUser(userId, aidRequests);
    }

    public void submit(String disasterId, String aidType, String description) {
        repository.create(disasterId, aidType, description, null, submitResult);
    }
}