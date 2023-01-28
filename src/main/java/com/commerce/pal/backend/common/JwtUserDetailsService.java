package com.commerce.pal.backend.common;


import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.repo.LoginValidationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private LoginValidationRepository userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginValidation user = userDao.findByEmailAddressOrPhoneNumber(username,username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with phone number/email : " + username);
        }
        final List<GrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority(ROLES_ARRAY.getString(user.getUserType().toString())));
//        authorities.add(new SimpleGrantedAuthority(ROLES_ARRAY.getString(user.getUserType().toString())));
        UserDetails userDetails = new User(user.getEmailAddress(), user.getPinHash(), authorities);
        return userDetails;
    }
}