package com.peaknote.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.peaknote.demo.entity.TeamsUser;

public interface UserRepository extends JpaRepository<TeamsUser, String> {
    // OID 为主键，无需自定义方法
}
