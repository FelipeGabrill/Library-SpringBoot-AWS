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
public class PasswordRecoveryPublisher {

    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryPublisher.class);

    private final SnsAsyncClient snsClient;

    @Value("${aws.sns.password-recovery.topic-arn}")
    private String topicArn;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    public PasswordRecoveryPublisher(SnsAsyncClient snsClient) {
        this.snsClient = snsClient;
    }

    public void publishPasswordRecovery(String email, String token) {
        String eventId = UUID.randomUUID().toString();

        log.info("Publishing PasswordRecovery event | eventId={} | email={}", eventId, email);

        String payload = """
                {
                  "eventId": "%s",
                  "eventType": "PasswordRecoveryRequested",
                  "data": {
                    "email": "%s",
                    "token": "%s",
                    "recoverUrl": "%s",
                    "occurredAt": "%s"
                  }
                }
                """.formatted(eventId, email, token, recoverUri + token, Instant.now());

        snsClient.publish(PublishRequest.builder()
                        .topicArn(topicArn)
                        .message(payload)
                        .build())
                .whenComplete((response, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish PasswordRecovery event | eventId={} | email={} | error={}",
                                eventId, email, ex.getMessage());
                        throw new RuntimeException("Failed to publish PasswordRecovery event", ex);
                    }
                    log.info("PasswordRecovery event published successfully | eventId={} | messageId={}",
                            eventId, response.messageId());
                });
    }
}