package com.example.demo.service;

import java.util.List;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CityResponseDTO;
import com.example.demo.entity.ChatEntity;
import com.example.demo.repository.ChatRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class OpenAIService {

	private final OpenAiChatModel openAiChatModel;
	private final OpenAiEmbeddingModel openAiEmbeddingModel;
	private final OpenAiImageModel openAiImageModel;
	private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
	private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

	private final ChatMemoryRepository chatMemoryRepository;
	private final ChatRepository chatRepository;
	private final ChatClient chatClient;

	// 1. Chat 모델 (Blocking)
	public CityResponseDTO generate(String text) {

		// 메시지
		SystemMessage systemMessage = new SystemMessage("");
		UserMessage userMessage = new UserMessage(text);
		AssistantMessage assistantMessage = new AssistantMessage("");

		// 옵션 (어떤 모델을 사용할지 등...)
		OpenAiChatOptions options = OpenAiChatOptions.builder()
			.model("gpt-4.1-mini")
			.temperature(0.7)
			.build();

		// 프롬프트
		Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

		// 요청 및 응답
		return chatClient.prompt(prompt)
			.call()
			.entity(CityResponseDTO.class);
	}

	// 1. Chat 모델 (Stream)
	public Flux<String> generateStream(String text) {

		// 유저&페이지별 ChatMemory를 관리하기 위한 key (우선은 명시적으로)
		// 보통은 시큐리티를 통해 유저에 대한 식별자 + a 로 id를 만듦.
		String userId = "xxxjjhhh" + "_" + "1";

		// 대화 저장용 (유저 질문)
		ChatEntity chatUserEntity = new ChatEntity();
		chatUserEntity.setUserId(userId);
		chatUserEntity.setType(MessageType.USER);
		chatUserEntity.setContent(text);

		// 챗 메모리를 관리해주는 클래스
		ChatMemory chatMemory = MessageWindowChatMemory.builder()
			.maxMessages(10)	// 10개의 데이터
			.chatMemoryRepository(chatMemoryRepository)
			.build();

		// 신규 메시지 추가 (내부적으로 repository saveAll() 호출함)
		chatMemory.add(userId, new UserMessage(text));

		// 옵션 (어떤 모델을 사용할지 등...)
		OpenAiChatOptions options = OpenAiChatOptions.builder()
			.model("gpt-4.1-mini")
			.temperature(0.7)
			.build();

		// 프롬프트
		// Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);
		// chatMemory.get()은 내부적으로 List<Message>를 반환
		Prompt prompt = new Prompt(chatMemory.get(userId), options);

		// 스트림 방식이므로 잘린 토큰을 다시 모아 챗 메모리에 저장해야 함.
		// 가변 길이 문자열을 다루기 위한 클래스 사용
		StringBuilder responseBuffer = new StringBuilder();

		// 요청 및 응답
		return chatClient.prompt(prompt)
			.tools(new ChatTools())
			.stream()
			.content()
			.map(token -> {
				responseBuffer.append(token);
				return token;
			})
			.doOnComplete(() -> {
				// chatMemory 저장
				chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
				chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

				// 전체 대화 저장용
				ChatEntity chatAssistantEntity = new ChatEntity();
				chatAssistantEntity.setUserId(userId);
				chatAssistantEntity.setType(MessageType.ASSISTANT);
				chatAssistantEntity.setContent(responseBuffer.toString());

				chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
			});

		// return openAiChatModel.stream(prompt)
		// 	.mapNotNull(response -> {
		// 		String token = response.getResult().getOutput().getText();
		// 		if (token != null) {
		// 			responseBuffer.append(token);
		// 			return token;
		// 		}
		// 		return null;
		// 	})
		// 	// 응답 또한 저장해야 하므로
		// 	.doOnComplete(() -> {
		// 		chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
		// 		chatMemoryRepository.saveAll(userId, chatMemory.get(userId));
		//
		// 		// 전체 대화 저장용 (AI의 응답)
		// 		ChatEntity chatAssistantEntity = new ChatEntity();
		// 		chatAssistantEntity.setUserId(userId);
		// 		chatAssistantEntity.setType(MessageType.ASSISTANT);
		// 		chatAssistantEntity.setContent(responseBuffer.toString());
		//
		// 		// 유저 질문 + AI 응답 저장
		// 		chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
		// 	});
	}

	// 2. 임베딩 모델
	public List<float[]> generateEmbedding(List<String> texts, String model) {

		// 옵션
		EmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
			.model(model).build();

		// 프롬프트
		EmbeddingRequest prompt = new EmbeddingRequest(texts, embeddingOptions);

		// 요청 및 응답
		EmbeddingResponse response = openAiEmbeddingModel.call(prompt);
		return response.getResults().stream()
			.map(Embedding::getOutput)
			.toList();
	}

	// 3. 이미지 모델
	public List<String> generateImages(String text, int count, int height, int width) {

		// 옵션
		OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
			.quality("hd")	// 퀄리티
			.N(count)		// 개수
			.height(height)	// 높이
			.width(width)	// 너비
			.build();

		// 프롬프트
		ImagePrompt prompt = new ImagePrompt(text, imageOptions);

		// 요청 및 응답
		ImageResponse response = openAiImageModel.call(prompt);

		return response.getResults().stream()
			.map(image -> image.getOutput().getUrl())
			.toList();
	}

	// 4. 오디오 모델 (TTS : 텍스트 -> 오디오)
	public byte[] tts(String text) {

		// 옵션
		OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
			.responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3) // MP3로 받기
			.speed(1.0f)  // 정배속으로
			.model(OpenAiAudioApi.TtsModel.TTS_1.value)  // 모델 선택
			.build();

		// 프롬프트
		SpeechPrompt prompt = new SpeechPrompt(text, speechOptions);

		// 요청 및 응답
		SpeechResponse response = openAiAudioSpeechModel.call(prompt);

		// Base64 기반의 Byte로 반환됨
		return response.getResult().getOutput();
	}

	// 4. 오디오 모델 (STT : 오디오 -> 텍스트)
	public String stt(Resource audioFile) {

		// 옵션
		OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT; // VTT : 표준 자막 포맷을 의미
		OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
			.language("ko") // 인식할 언어
			.prompt("Ask not this, but ask that") // 음성 인식 전 참고할 텍스트 프롬프트
			.temperature(0f)
			.model(OpenAiAudioApi.TtsModel.TTS_1.value) // 모델 지정
			.responseFormat(responseFormat) // 결과 타입 지정 VTT 자막형식 JSON 등 ... 다양함
			.build();

		// 프롬프트
		AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);

		// 요청 및 응답
		AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
		return response.getResult().getOutput();
	}

}
