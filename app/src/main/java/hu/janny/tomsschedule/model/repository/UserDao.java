package hu.janny.tomsschedule.model.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import hu.janny.tomsschedule.model.User;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE users.userId = :id")
    User getUserById(String id);

    @Query("SELECT * FROM users WHERE users.isLoggedIn = 1 ")
    LiveData<User> getCurrentUser();

    @Query("UPDATE users set isLoggedIn = 1 where users.userId = :id")
    void logIn(String id);

    @Query("UPDATE users set isLoggedIn = 0 where users.userId = :id")
    void logOut(String id);
}
