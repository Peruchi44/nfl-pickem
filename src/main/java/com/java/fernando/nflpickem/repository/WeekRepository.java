package com.java.fernando.nflpickem.repository;

import com.java.fernando.nflpickem.model.Week;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeekRepository extends MongoRepository<Week, String> {
    Optional<Week> findBySeasonAndWeekNumber(int season, int weekNumber);
    Optional<Week> findByCurrentTrue();
}