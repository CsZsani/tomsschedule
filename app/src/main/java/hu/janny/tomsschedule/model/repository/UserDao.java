package hu.janny.tomsschedule.model.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hu.janny.tomsschedule.model.User;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    /**
     * Returns the logged in user.
     * @return logged in user LiveData
     */
    @Query("SELECT * FROM users WHERE users.isLoggedIn = 1 ")
    LiveData<User> getCurrentUser();

    /**
     * Returns all users in database.
     * @return users in database as LiveData
     */
    @Query("SELECT * FROM users")
    LiveData<List<User>> getUsers();

    /**
     * Signs in the user by userId.
     * @param id user id
     */
    @Query("UPDATE users set isLoggedIn = 1 where users.userId = :id")
    void logIn(String id);

    /**
     * Signs out the user by userId.
     * @param id user id
     */
    @Query("UPDATE users set isLoggedIn = 0 where users.userId = :id")
    void logOut(String id);
}
