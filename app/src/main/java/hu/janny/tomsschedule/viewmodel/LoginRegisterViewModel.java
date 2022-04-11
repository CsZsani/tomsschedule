package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.model.helper.SuccessCallback;
import hu.janny.tomsschedule.model.repository.UserRepository;

public class LoginRegisterViewModel extends AndroidViewModel {

    private final UserRepository repository;
    // All user in local database
    private LiveData<List<User>> users;
    // Logged in user
    private LiveData<User> currentUser;

    public LoginRegisterViewModel(@NonNull Application application) {
        super(application);

        repository = new UserRepository(application);
        users = repository.getUsers();
        currentUser = repository.getCurrentUser();
    }

    /**
     * Inserts a new user into local database
     *
     * @param user new user
     */
    public void insertUser(User user) {
        repository.insertUser(user);
    }

    /**
     * Updates the given user in local database
     *
     * @param user user to be updated
     */
    public void updateUser(User user) {
        repository.updateUser(user);
    }

    /**
     * Updates the given user in Firebase database
     *
     * @param user     user to be updated
     */
    public void updateUserInFirebase(User user) {
        FirebaseManager.updateUser(user);
    }

    /**
     * Signs in the user in database, means that it sets isLoggedIn field to 1 (true).
     *
     * @param id user id
     */
    public void loginUser(String id) {
        repository.loginUser(id);
    }

    /**
     * Signs out the user in database, means that it sets isLoggedIn field to 0 (false).
     *
     * @param id user id
     */
    public void logoutUser(String id) {
        repository.logoutUser(id);
    }

    /**
     * Returns all users from the database with LiveData
     *
     * @return all user from database LiveData
     */
    public LiveData<List<User>> getUsers() {
        return users;
    }

    /**
     * Returns the logged in user with LiveData
     *
     * @return logged in user from database LiveData
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
}
