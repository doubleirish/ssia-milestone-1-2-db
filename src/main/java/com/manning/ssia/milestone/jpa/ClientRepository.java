package com.manning.ssia.milestone.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Integer> {

 Client findByName(String name);
 }
