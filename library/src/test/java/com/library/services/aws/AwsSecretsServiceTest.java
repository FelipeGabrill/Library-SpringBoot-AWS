package com.library.services.aws;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsSecretsServiceTest {

    @Mock
    private SecretsManagerClient secretsManagerClient;

    private AwsSecretsService service;

    @BeforeEach
    void setUp() {
        service = new AwsSecretsService("us-east-1", "", "");
        ReflectionTestUtils.setField(service, "secretsManagerClient", secretsManagerClient);
    }

    @Test
    void getSecretShouldReturnJsonNodeWhenSecretExists() {
        String json = "{\"username\":\"admin\",\"password\":\"123456\",\"host\":\"rds.aws.com\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(json)
                .build();

        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        JsonNode result = service.getSecret("arn:test");

        assertNotNull(result);
        assertEquals("admin", result.get("username").asText());
        assertEquals("123456", result.get("password").asText());
        assertEquals("rds.aws.com", result.get("host").asText());
    }

    @Test
    void getSecretShouldThrowRuntimeExceptionWhenSecretNotFound() {
        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().message("not found").build());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getSecret("arn:invalid"));

        assertTrue(ex.getMessage().contains("Failed to fetch secret"));
    }

    @Test
    void getSecretShouldThrowRuntimeExceptionWhenInvalidJson() {
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("not a valid json {{{")
                .build();

        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        assertThrows(RuntimeException.class, () -> service.getSecret("arn:test"));
    }
}
