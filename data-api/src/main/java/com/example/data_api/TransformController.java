package com.example.data_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransformController {

	@Value("${internal.token}")
	private String internalToken;

	public record TransformRequest(String text) {
	}

	public record TransformResponse(String result) {
	}

	@PostMapping("/transform")
	public ResponseEntity<?> transofrm(
			@RequestHeader(value = "X-Internal-Token", required = false) String incomingToken,
			@RequestBody TransformRequest request) {
		if (incomingToken == null || !incomingToken.equals(internalToken)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		String text = request.text();
		String transformed = (text != null ? text.toUpperCase() : "");
		return ResponseEntity.ok(new TransformResponse(transformed));
	}

}
