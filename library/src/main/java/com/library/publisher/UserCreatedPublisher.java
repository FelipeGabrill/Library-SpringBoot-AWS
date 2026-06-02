package com.library.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserCreatedPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedPublisher.class);

    private final SnsAsyncClient snsClient;

    @Value("${aws.sns.user-created.topic-arn}")
    private String topicArn;

    public UserCreatedPublisher(SnsAsyncClient snsClient) {
        this.snsClient = snsClient;
    }

    public void publishUserCreated(Long userId, String email, String name) {
        String eventId = UUID.randomUUID().toString();

        log.info("Publishing UserCreated event | eventId={} | userId={} | email={}",
                eventId, userId, email);

        String payload = """
                {
                  "eventId": "%s",
                  "eventType": "UserCreated",
                  "data": {
                    "userId": %s,
                    "email": "%s",
                    "name": "%s",
                    "occurredAt": "%s"
                  }
                }
                """.formatted(eventId, userId, email, name, Instant.now());

        snsClient.publish(PublishRequest.builder()
                        .topicArn(topicArn)
                        .message(payload)
                        .build())
                .whenComplete((response, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserCreated event | eventId={} | userId={} | email={} | error={}",
                                eventId, userId, email, ex.getMessage());
                        throw new RuntimeException("Failed to publish UserCreated event", ex);
                    }
                    log.info("UserCreated event published successfully | eventId={} | messageId={}",
                            eventId, response.messageId());
                });
    }
}