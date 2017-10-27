package com.backend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.domain.User;

@Mapper
public interface UserDao {

    User findByUsername(@Param("username") String username);

    void create(@Param("domain") User domain);
}
