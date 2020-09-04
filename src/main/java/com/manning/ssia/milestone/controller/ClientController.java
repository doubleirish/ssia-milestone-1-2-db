package com.manning.ssia.milestone.controller;


import com.manning.ssia.milestone.jpa.Client;
import com.manning.ssia.milestone.jpa.ClientRepository;
import com.manning.ssia.milestone.jpa.Grant;
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
@RequestMapping(path = "/clients", produces = "application/json")
@CrossOrigin(origins = "*")
public class ClientController {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClientController(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder =  passwordEncoder;
    }

    @GetMapping
    public Iterable<ClientDto> list() {


        PageRequest page = PageRequest.of(
                0, 12, Sort.by("name").ascending());

        return clientRepository
                .findAll(page)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

    }


    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> byId(@PathVariable("id") Integer id) {
        Optional<Client> optClient = clientRepository.findById(id);
        if (optClient.isPresent()) {
            return new ResponseEntity<>(convertToDto(optClient.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ClientDto createClient(@RequestBody ClientDto clientDto) {
        Client client = convertToEntity(clientDto);
        client = clientRepository.save(client);
        log.info("created client {}",client);
        return convertToDto(client);
    }



    private Client convertToEntity(ClientDto clientDto) {
        Client client = new Client();
        client.setName(clientDto.getName());
        client.setScope(clientDto.getScope());
        client.setRedirectUri(clientDto.getRedirectUri());
        client.setSecret(this.passwordEncoder.encode(clientDto.getSecret())); // TODO hash encode this later

        List<Grant> grants = clientDto.getGrants()
                .stream()
                .map(a -> new Grant(a,client))
                .collect(Collectors.toList());

        client.setGrants(grants);
        return client;
    }
    private ClientDto convertToDto(Client client) {
        return new ClientDto(client);
    }

}

