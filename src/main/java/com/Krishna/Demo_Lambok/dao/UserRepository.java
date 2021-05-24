package com.Krishna.Demo_Lambok.dao;

import com.Krishna.Demo_Lambok.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Integer, UserEntity> {

}
