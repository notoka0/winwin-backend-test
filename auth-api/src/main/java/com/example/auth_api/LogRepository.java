package com.example.auth_api;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<ProcessingLog, UUID> {

}
