package hu.janny.tomsschedule;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

public class LoginRegisterViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private LiveData<List<User>> users;

    public LoginRegisterViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        users = repository.getUsers();
    }

    public void insertUser(User user) {
        repository.insertUser(user);
    }

    public void updateUser(User user) {
        repository.updateUser(user);
    }

    public User getUserById(String id) {
        return repository.getUserByIdForUpdate(id);
    }

    public User isInDatabase(String id) {return repository.isInDatabase(id);}

    public void loginUser(String id) {
        repository.loginUser(id);
    }

    public void logoutUser(String id) {
        repository.logoutUser(id);
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }
}
