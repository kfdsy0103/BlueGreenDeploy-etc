package com.example.demo.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.service.OpenAIService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final OpenAIService openAIService;

	@GetMapping("/chat")
	public String chatPage() {
		return "chat";
	}

	// Blocking(non-stream) 형태의 서비스 호출
	@ResponseBody
	@PostMapping("/chat")
	public String chat(@RequestBody Map<String, String> body) {	// Map이 아닌, DTO로 리팩토링
		return openAIService.generate(body.get("text"));
	}

	// stream 형태의 서비스 호출
	@ResponseBody
	@PostMapping("/chat/stream")
	public Flux<String> stream(@RequestBody Map<String, String> body) {
		return openAIService.generateStream(body.get("text"));
	}
}
