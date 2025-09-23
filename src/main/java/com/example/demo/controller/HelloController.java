package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "<!DOCTYPE html>" +
			"<html><head><title>고양이</title></head>" +
			"<body><h1>귀여운 고양이를 드립니다, , ,</h1>" +
			"<img src='https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRh6hzC1-4M-CArF60LxPBqSU2eI5ltueyDQk9kY0zZABJWbGd9zvGyHUI74w2vLsyNr0A&usqp=CAU'/>" +
			"</body></html>";
	}

	@GetMapping("/hello2")
	public String hello2() { return "Hello2 !!!"; }
}
