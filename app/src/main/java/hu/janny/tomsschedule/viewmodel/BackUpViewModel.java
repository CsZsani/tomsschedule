package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.helper.SuccessCallback;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

/**
 * The view model of making backups in settings fragment.
 */
public class BackUpViewModel extends AndroidViewModel {

    private final Repository repository;
    private final UserRepository userRepository;

    private final LiveData<User> user;

    public BackUpViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);
        user = userRepository.getCurrentUser();
    }

    /**
     * Saves the data from local database to Firebase during creating backup.
     *
     * @param userId the id of the current user who creates backup
     */
    public boolean saveData(String userId, SuccessCallback callback) {
        return repository.saveData(userId, callback);
    }

    /**
     * Saves the data from Firebase to local database during restoring backup.
     *
     * @param userId the id of the current user who creates backup
     */
    public void restoreBackup(String userId, SuccessCallback callback) {
        repository.restoreBackup(userId, callback);
    }

    /**
     * Returns the current user.
     *
     * @return the user who is logged in
     */
    public LiveData<User> getUser() {
        return user;
    }
}
