package com.example.demo.service;

import org.springframework.ai.tool.annotation.Tool;

import com.example.demo.dto.UserResponseDTO;

public class ChatTools {

	@Tool(description = "User personal information : name, age, address, phone, etc")
	public UserResponseDTO getUserInfoTool() {
		return new UserResponseDTO("피우", 15L, "서울특별시 종로구 청와대로 1", "010-0000-0000", "03048");
	}
}
