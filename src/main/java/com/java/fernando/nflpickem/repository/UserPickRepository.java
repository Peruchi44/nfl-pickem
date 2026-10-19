package com.java.fernando.nflpickem.repository;

import com.java.fernando.nflpickem.model.UserPick;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPickRepository extends MongoRepository<UserPick, String> {

    Optional<UserPick> findByUserIdAndSeasonAndWeekNumber(String userId, int season, int weekNumber);

    List<UserPick> findByUserIdAndSeason(String userId, int season);

    // Método que faltava para a linha 87 do PickService:
    List<UserPick> findBySeasonAndWeekNumber(int season, int weekNumber);
}