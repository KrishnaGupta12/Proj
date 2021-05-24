package com.Krishna.Demo_Lambok.controller;

import com.Krishna.Demo_Lambok.dto.UserDto;
import com.Krishna.Demo_Lambok.entity.UserEntity;
import com.Krishna.Demo_Lambok.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    UserService userService;



    @PostMapping(value = "/user/")
    public ResponseEntity<?> saveUser(@RequestBody UserEntity user, UserDto usersDto) {
        userService.saveUserEntity(usersDto);
                return ResponseEntity.ok("User "+usersDto.getFirstName()+"Saved User SuccessFull...");


    }

}
