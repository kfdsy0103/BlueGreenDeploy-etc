package com.example.demo.config;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AIConfig {

	// 기본 설정시 자동 등록이지만, 수동 등록 가능
	// @Bean
	// public ChatMemoryRepository chatMemoryRepository() {
	// 	return new InMemoryChatMemoryRepository();
	// }

	@Bean
	public ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
		return JdbcChatMemoryRepository.builder()
			.jdbcTemplate(jdbcTemplate)
			.transactionManager(transactionManager)
			.build();
	}
}
