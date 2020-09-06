package com.manning.ssia.milestone.controller;

import com.manning.ssia.milestone.jpa.Authority;
import com.manning.ssia.milestone.jpa.User;
import com.manning.ssia.milestone.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/users", produces = "application/json")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
          this.passwordEncoder =  passwordEncoder;
    }

    @GetMapping
    public Iterable<UserDto> listUsers() {

        log.debug("user repo is size " + userRepository.count());
        //userRepo.findAll().forEach(t->log.debug("found user "+t));

        PageRequest page = PageRequest.of(
                0, 12, Sort.by("username").ascending());

        return userRepository
                .findAll(page)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @GetMapping("{id}")
    public ResponseEntity<UserDto> userById(@PathVariable("id") Integer id) {
        Optional<User> optUser = userRepository.findById(id);
        return optUser.map(user -> new ResponseEntity<>(convertToDto(user), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserDto createUser(@RequestBody UserDto userDto) {
        User user = convertToEntity(userDto);
        user = userRepository.save(user);
        return convertToDto(user);
    }


    private UserDto convertToDto(User user) {
        return new UserDto(user);
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(this.passwordEncoder.encode(userDto.getPassword())); // TODO hash encode this later

        List<Authority> authorities = userDto.getAuthorities()
                .stream()
                .map(a -> new Authority(a,user))
                .collect(Collectors.toList());

        user.setAuthorities(authorities);
        userRepository.save(user);
        return user;
    }
}

