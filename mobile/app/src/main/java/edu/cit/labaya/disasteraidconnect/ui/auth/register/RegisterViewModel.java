package edu.cit.labaya.disasteraidconnect.ui.auth.register;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import edu.cit.labaya.disasteraidconnect.data.remote.dto.response.AuthResponseDTO;
import edu.cit.labaya.disasteraidconnect.data.repository.AuthRepository;
import edu.cit.labaya.disasteraidconnect.utils.Resource;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();
    public final MutableLiveData<Resource<AuthResponseDTO>> registerResult = new MutableLiveData<>();

    public void register(String email, String password, String username,
                         String securityQuestion, String securityAnswer) {
        repository.register(email, password, username,
            securityQuestion, securityAnswer, registerResult);
    }
}
