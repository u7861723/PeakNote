package com.peaknote.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.peaknote.demo.entity.GraphUserSubscription;

public interface GraphUserSubscriptionRepository extends JpaRepository<GraphUserSubscription, String> {
}
