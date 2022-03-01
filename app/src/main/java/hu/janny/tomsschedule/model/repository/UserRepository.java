package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.User;

public class UserRepository {

    private final MutableLiveData<User> userData = new MutableLiveData<>();
    private final LiveData<User> currentUser;
    private final LiveData<List<User>> users;

    private final UserDao userDao;

    public UserRepository(Application application) {
        ActivityRoomDatabase db;
        db = ActivityRoomDatabase.getDatabase(application);
        userDao = db.userDao();

        currentUser = userDao.getCurrentUser();
        users = userDao.getUsers();
    }

    public void insertUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.insertUser(user);
        });
        executor.shutdown();
    }

    public void updateUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.updateUser(user);
        });
        executor.shutdown();
    }

    public void loginUser(String id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.logIn(id);
        });
        executor.shutdown();
    }

    public void logoutUser(String id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.logOut(id);
        });
        executor.shutdown();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }
}
