package edu.cit.labaya.disasteraidconnect.ui.auth.login;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.response.AuthResponseDTO;
import edu.cit.labaya.disasteraidconnect.data.repository.AuthRepository;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();
    public final MutableLiveData<Resource<AuthResponseDTO>> loginResult = new MutableLiveData<>();

    public void login(String email, String password) {
        repository.login(email, password, loginResult);
    }
}
