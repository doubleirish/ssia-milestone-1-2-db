package com.manning.ssia.ssiamilestone.security;

import com.manning.ssia.ssiamilestone.jpa.User;
import com.manning.ssia.ssiamilestone.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("looking up user {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("did not find user {}", username);
            throw new UsernameNotFoundException(username);
        }
        com.manning.ssia.ssiamilestone.security.CustomUserDetails userDetails = new com.manning.ssia.ssiamilestone.security.CustomUserDetails(user);
        log.info("found userdetails {}", userDetails);
        return userDetails;
    }
}
