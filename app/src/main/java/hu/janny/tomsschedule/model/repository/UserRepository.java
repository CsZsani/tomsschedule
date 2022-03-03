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

    // The logged in user
    private final LiveData<User> currentUser;
    // All users
    private final LiveData<List<User>> users;

    private final UserDao userDao;

    public UserRepository(Application application) {
        ActivityRoomDatabase db;
        db = ActivityRoomDatabase.getDatabase(application);
        userDao = db.userDao();

        currentUser = userDao.getCurrentUser();
        users = userDao.getUsers();
    }

    /**
     * Inserts a new user into the database
     *
     * @param user new user
     */
    public void insertUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.insertUser(user);
        });
        executor.shutdown();
    }

    /**
     * Updates the given user in the database
     *
     * @param user user to update
     */
    public void updateUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.updateUser(user);
        });
        executor.shutdown();
    }

    /**
     * Signs in the user in database, means that it sets isLoggedIn field to 1 (true).
     *
     * @param id user id
     */
    public void loginUser(String id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.logIn(id);
        });
        executor.shutdown();
    }

    /**
     * Signs out the user in database, means that it sets isLoggedIn field to 0 (false).
     *
     * @param id user id
     */
    public void logoutUser(String id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.logOut(id);
        });
        executor.shutdown();
    }

    /**
     * Returns the logged in user LiveData
     *
     * @return the logged in user LiveData
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns all users from the database with LiveData
     *
     * @return all user from database LiveData
     */
    public LiveData<List<User>> getUsers() {
        return users;
    }
}
