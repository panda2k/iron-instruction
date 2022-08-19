package com.ironinstruction.api.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // use optional because it is not guaranteed that an athlete will be found
    Optional<User> findByEmail(String email);
    void deleteByEmail(String email);
}
