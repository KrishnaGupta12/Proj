package com.Krishna.Demo_Lambok.dao;

import com.Krishna.Demo_Lambok.dto.UserDto;
import com.Krishna.Demo_Lambok.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
    public void saveUser(UserEntity user){
        System.out.println("User saved in to the DataBase"+user.getUserId());
        System.out.println(user);
    }

}
