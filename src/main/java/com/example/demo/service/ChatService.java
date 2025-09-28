package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.ChatEntity;
import com.example.demo.repository.ChatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRepository chatRepository;

	@Transactional(readOnly = true)
	public List<ChatEntity> readAllChats(String userId) {
		// 실제로는 DTO 반환하도록

		return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
	}
}
