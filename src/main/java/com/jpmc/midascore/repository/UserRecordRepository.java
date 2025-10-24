package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.UserRecord; // <-- Imports your class
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {

    Optional<UserRecord> findByName(String name);
}