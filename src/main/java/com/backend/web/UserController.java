package com.backend.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    JdbcUserDetailsManager manager;

    @Autowired
    PasswordEncoder        passwordEncoder;

    @Autowired
    private TokenStore     tokenStore;

    /**
     * Create user
     * 
     * @param request
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "v1", method = RequestMethod.POST)
    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<UserDetails> create(HttpServletRequest request, HttpServletResponse response,
            @RequestParam String username, @RequestParam String password) throws IOException {
        if (manager.userExists(username)) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "User Already exsited in our system");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("======================= create user =================");
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        UserDetails userJoe = new User(username, passwordEncoder.encode(password), authorities);
        manager.createUser(userJoe);
        return new ResponseEntity<>(userJoe, HttpStatus.OK);
    }

    /**
     * Update User
     * 
     * @param request
     * @param username
     * @param password
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "v1", method = RequestMethod.PUT)
    public ResponseEntity<UserDetails> update(HttpServletRequest request) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Delete User
     * 
     * @param request
     * @param username
     * @param password
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "v1", method = RequestMethod.DELETE)
    public ResponseEntity<UserDetails> delete(HttpServletRequest request, @RequestParam String username,
            @RequestParam String password) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Change password
     * 
     * @param request
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "v1/change/password", method = RequestMethod.POST)
    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<Void> changePassword(HttpServletRequest request, HttpServletResponse response,
            @RequestParam String oldPassword, @RequestParam String newPassword) throws IOException {
        UserDetails userDetails = manager.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (passwordEncoder.matches(oldPassword, userDetails.getPassword())) {
            manager.changePassword(oldPassword, passwordEncoder.encode(newPassword));
            return new ResponseEntity<>(HttpStatus.OK);
        }
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Password is incorrect.");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Get principal (User owner)
     * 
     * @return
     */
    @RequestMapping(value = "v1/principal", method = RequestMethod.GET)
    public Object getPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal;
    }

    /**
     * Get roles (User owner)
     * 
     * @return
     */
    @RequestMapping(value = "v1/roles", method = RequestMethod.GET)
    public Object getRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    }

    /**
     * 
     * @param request
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_USER')")
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            String tokenValue = authHeader.replace("Bearer", "").trim();
            OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
            tokenStore.removeAccessToken(accessToken);
        }
    }
}
