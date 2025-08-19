package com.peaknote.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.peaknote.demo.entity.TeamsUser;

public interface UserRepository extends JpaRepository<TeamsUser, String> {
    // OID is the primary key, no custom methods needed
}
