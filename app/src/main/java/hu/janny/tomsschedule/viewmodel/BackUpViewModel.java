package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

public class BackUpViewModel extends AndroidViewModel {

    private final Repository repository;
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> ready;
    private final LiveData<User> user;

    public BackUpViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);
        ready = repository.getReady();
        user = userRepository.getCurrentUser();
    }

    public void saveData(String userId) {
        repository.saveData(userId);
    }

    public void restoreBackup(String userId) {
        repository.restoreBackup(userId);
    }

    public void setReady(boolean ready) {
        repository.setReady(ready);
    }

    public MutableLiveData<Boolean> getReady() {
        return ready;
    }

    public void deleteActivitiesByUserId(String id) {
        repository.deleteActivitiesByUserId(id);
    }

    public LiveData<User> getUser() {
        return user;
    }
}
