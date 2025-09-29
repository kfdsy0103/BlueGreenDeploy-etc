package com.example.demo.reader;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class MyJsonReader {

	@Value("classpath:data.PageCollection.json")
	private final Resource resource;

	public MyJsonReader(@Value("classpath:data.PageCollection.json") Resource resource) {
		this.resource = resource;
	}

	public List<Document> loadJsonAsDocuments() {
		JsonReader jsonReader = new JsonReader(this.resource, "_id");
		return jsonReader.get();
	}

}
