package com.ironinstruction.api.program;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends MongoRepository<Program, String> {
    @Query("{$or: [ {'coachEmail': ?0}, {'athleteEmail': ?0} ]}")
    public List<Program> findByEmail(String email);
}


