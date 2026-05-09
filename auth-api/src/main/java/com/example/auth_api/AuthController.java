package com.example.auth_api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class AuthController {

	private UserRepository userRepo;
	private LogRepository logRepo;
	private BCryptPasswordEncoder encoder;
	@Value("${internal.token}")
	private String internalToken;
	private JwtUtil jwtUtil;

	public AuthController(UserRepository userRepo, LogRepository logRepo, BCryptPasswordEncoder encoder,
			JwtUtil jwtUtil) {
		this.userRepo = userRepo;
		this.logRepo = logRepo;
		this.encoder = encoder;
		this.jwtUtil = jwtUtil;
	}

	public record RegisterRequest(String email, String password) {
	}

	public record ProcessRequest(String text) {
	}

	public record TransformResponse(String result) {
	}

	public record LoginResponse(String token) {
	}

	public record ProcessResponse(String result) {
	}

	public record ErrorResponse(String error) {
	}

	@PostMapping("/auth/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		if (userRepo.findByEmail(request.email()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email already exists"));
		}

		User user = new User();
		user.setEmail(request.email());
		user.setPasswordHash(encoder.encode(request.password()));
		userRepo.save(user);
		return ResponseEntity.status(201).build();
	}

	@PostMapping("/auth/login")
	public ResponseEntity<?> login(@RequestBody RegisterRequest request) {
		User user = userRepo.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("User not found"));

		if (encoder.matches(request.password(), user.getPasswordHash())) {

			return ResponseEntity.ok(new LoginResponse(jwtUtil.generateToken(user.getEmail())));
		}
		return ResponseEntity.status(401).build();
	}

	@PostMapping("/process")
	public ResponseEntity<?> process(@RequestBody ProcessRequest request, Principal principal) {
		String text = request.text();

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Internal-Token", internalToken);

		HttpEntity<ProcessRequest> entity = new HttpEntity<>(request, headers);
		try {
			ResponseEntity<TransformResponse> response = restTemplate
					.postForEntity("http://data-api:8081/api/transform", entity, TransformResponse.class);

			String result = response.getBody().result();

			User user = userRepo.findByEmail(principal.getName()).orElseThrow();
			ProcessingLog log = new ProcessingLog();
			log.setUserId(user.getId());
			log.setInputText(text);
			log.setOutputText(result);
			logRepo.save(log);

			return ResponseEntity.ok(new ProcessResponse(result));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new ErrorResponse("Data API is unavailable or rejected the request"));

		}
	}
}
