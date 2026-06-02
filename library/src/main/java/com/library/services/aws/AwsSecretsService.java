package com.library.services.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Service
public class AwsSecretsService {

    private static final Logger log = LoggerFactory.getLogger(AwsSecretsService.class);

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AwsSecretsService(
            @Value("${cloud.aws.region.static}") String region,
            @Value("${cloud.aws.credentials.access-key:}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key:}") String secretKey) {

        var builder = SecretsManagerClient.builder()
                .region(Region.of(region));

        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)));
        }

        this.secretsManagerClient = builder.build();
    }

    public JsonNode getSecret(String secretArn) {
        log.info("Fetching secret from Secrets Manager | arn={}", secretArn);
        try {
            String secretString = secretsManagerClient.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(secretArn)
                            .build()
            ).secretString();

            JsonNode node = objectMapper.readTree(secretString);
            log.info("Secret fetched successfully | arn={}", secretArn);
            return node;
        } catch (Exception e) {
            log.error("Failed to fetch secret | arn={} | error={}", secretArn, e.getMessage());
            throw new RuntimeException("Failed to fetch secret: " + secretArn, e);
        }
    }
}