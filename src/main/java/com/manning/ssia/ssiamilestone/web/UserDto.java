package com.manning.ssia.ssiamilestone.web;

import com.manning.ssia.ssiamilestone.jpa.Authority;
import com.manning.ssia.ssiamilestone.jpa.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserDto {
    private int id;
    private String username;
    private String password;
    private List<String> authorities;

    UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = user.getAuthorities()
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toList());
    }
}
