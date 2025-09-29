package com.example.demo.config;

import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class VectorStoreConfig {

	private final EmbeddingModel embeddingModel;

	@Bean
	public VectorStore elasticsearchVectorStore(RestClient restClient) {
		ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
		options.setIndexName("piu-docs");
		options.setSimilarity(SimilarityFunction.cosine);
		options.setDimensions(1536);

		return ElasticsearchVectorStore.builder(restClient, embeddingModel)
			.options(options)
			.initializeSchema(true)
			.batchingStrategy(new TokenCountBatchingStrategy())
			.build();
	}
}
