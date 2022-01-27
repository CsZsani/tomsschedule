package hu.janny.tomsschedule.ui.main.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.UserState;

public class AccountViewModel extends ViewModel {
    // TODO: Implement the ViewModel

    private MutableLiveData<User> user;

    public AccountViewModel() {
        user = new MutableLiveData<>();
        user.setValue(UserState.getUser());
    }

    public LiveData<User> getUser() {
        return user;
    }
}