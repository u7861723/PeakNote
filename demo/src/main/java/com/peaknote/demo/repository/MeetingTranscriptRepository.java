package com.peaknote.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peaknote.demo.entity.MeetingTranscript;

@Repository
public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, UUID> {
}
