package com.backend.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.backend.dao.AuthorityDao;
import com.backend.dao.UserDao;
import com.backend.domain.Authority;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService, UserDetailsManager {

    @Resource
    private UserDao      userDao;

    @Resource
    private AuthorityDao authorityDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("====================== load user ======================");

        try {
            com.backend.domain.User user = userDao.findByUsername(username);
            user.setAuthorities(getAuthorities(username));
            log.info(String.format("User : %s", user.toString()));
            return user;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param username
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthorities(String username) {
        Collection<Authority> items = authorityDao.getAuthorities(username);
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for(Authority item : items) {
            authorities.add(new SimpleGrantedAuthority(item.getAuthority()));
        }
        return authorities;
    }

    @Override
    public void createUser(UserDetails user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUser(UserDetails user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteUser(String username) {
        // TODO Auto-generated method stub

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean userExists(String username) {
        // TODO Auto-generated method stub
        return false;
    }
}
