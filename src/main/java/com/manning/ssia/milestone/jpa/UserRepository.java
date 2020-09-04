package com.manning.ssia.milestone.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository   extends JpaRepository<User, Integer> {

  User findByUsername(String name);
 }
