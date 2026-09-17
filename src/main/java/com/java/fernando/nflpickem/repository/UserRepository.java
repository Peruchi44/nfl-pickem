package com.java.fernando.nflpickem.repository;

import com.java.fernando.nflpickem.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
}