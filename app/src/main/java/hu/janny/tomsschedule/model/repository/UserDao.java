package hu.janny.tomsschedule.model.repository;

import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import hu.janny.tomsschedule.model.User;

public interface UserDao {

    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE users.userId = :id")
    User getUserById(String id);
}
