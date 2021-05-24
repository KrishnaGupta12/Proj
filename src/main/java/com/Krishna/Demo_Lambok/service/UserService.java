package com.Krishna.Demo_Lambok.service;


import com.Krishna.Demo_Lambok.dao.UserDao;
import com.Krishna.Demo_Lambok.dto.UserDto;
import com.Krishna.Demo_Lambok.entity.UserEntity;

public class UserService {
    private UserDao dao;


        private UserEntity convertDTOTOEntity(UserDto userDto)
        {

     return UserEntity.builder().
             userId(userDto.getUserId()).
            userEmail(userDto.getUserEmail()).
            firstName(userDto.getFirstName()).
            lastName(userDto.getLastName()).
            middleName(userDto.getMiddleName()).
            userMobileNo(userDto.getUserMobileNo()).build();


    }

    public void saveUserEntity(UserDto userDto) {
        UserEntity userEntity = convertDTOTOEntity(userDto);
        dao.saveUser(userEntity);
    }
}
