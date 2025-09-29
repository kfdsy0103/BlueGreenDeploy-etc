package com.example.demo.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.reader.MyJsonReader;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReaderController {

	private final MyJsonReader jsonReader;

	@GetMapping("/reader")
	public List<Document> reader() {
		return jsonReader.loadJsonAsDocuments();
	}
}
