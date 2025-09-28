package com.example.demo.dto;

public record UserResponseDTO(
	String name,
	Long age,
	String address,
	String phoneNumber,
	String zipCode
) {}
