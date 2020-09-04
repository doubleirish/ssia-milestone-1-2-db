package com.manning.ssia.milestone.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void usersListAsUser() throws Exception {
        String jsonStr = this.restTemplate
                .withBasicAuth("john", "12345")
                .getForObject("http://localhost:" + port + "/users", String.class);
        assertThat(jsonStr).contains("john");
    }

    @Test
    public void userById() throws Exception {
        UserDto userDto = this.restTemplate
                .withBasicAuth("john", "12345")
                .getForObject("http://localhost:" + port + "/users/1", UserDto.class);
        assertThat(userDto.getId()).isEqualTo(1);
        assertThat(userDto.getUsername()).isEqualTo("john");
    }


    @Test
    public void createUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setPassword("newpass");
        userDto.setAuthorities(Arrays.asList("user", "admin"));
        UserDto returnUserDto = this.restTemplate
                .withBasicAuth("john", "12345")
                .postForObject("http://localhost:" + port + "/users", userDto, UserDto.class);

        System.out.println(returnUserDto);

        assertThat(returnUserDto.getUsername()).isEqualTo(userDto.getUsername());
    }

}