package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ChatEntity;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

	List<ChatEntity> findByUserIdOrderByCreatedAtAsc(String userId);
}
