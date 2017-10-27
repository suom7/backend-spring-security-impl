package com.backend.dao;

import java.util.Collection;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.Authority;

@Repository
@Mapper
public interface AuthorityDao {
    public Collection<Authority> getAuthorities(@Param("username") String username);

    public void create(@Param("domain") Authority domain);
}
