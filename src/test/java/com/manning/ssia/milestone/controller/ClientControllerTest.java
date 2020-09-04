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
public class ClientControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void clients() throws Exception {
        String jsonStr =  this.restTemplate.getForObject("http://localhost:" + port + "/clients",  String.class);
        assertThat(jsonStr).contains("client");
    }

    @Test
    public void clientById() throws Exception {
        ClientDto clientDto =  this.restTemplate.getForObject("http://localhost:" + port + "/clients/1",  ClientDto.class);

        assertThat(clientDto.getName()).isEqualTo("client");
    }


    @Test
    public void createClient() throws Exception {
        ClientDto clientDto = new ClientDto();
        clientDto.setName("newclient");
        clientDto.setSecret("newpass");
        clientDto.setScope("read");
        clientDto.setGrants(Arrays.asList("user","admin","read"));
        clientDto.setRedirectUri("http://localhost:8080/authorized");
        ClientDto returnClientDto =  this.restTemplate.postForObject("http://localhost:" + port + "/clients",  clientDto,ClientDto.class);

        System.out.println(returnClientDto);

        assertThat(returnClientDto.getName()).isEqualTo(clientDto.getName());
    }

}