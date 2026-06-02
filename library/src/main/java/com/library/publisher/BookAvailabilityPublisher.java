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
public class BookAvailabilityPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookAvailabilityPublisher.class);

    private final SnsAsyncClient snsClient;

    @Value("${aws.sns.book-available.topic-arn}")
    private String topicArn;

    public BookAvailabilityPublisher(SnsAsyncClient snsClient) {
        this.snsClient = snsClient;
    }

    public void publishBookAvailable(Long bookId, String bookTitle,
                                     String userEmail, String userName) {
        String eventId = UUID.randomUUID().toString();

        log.info("Publishing BookAvailable event | eventId={} | bookId={} | book={} | userEmail={}",
                eventId, bookId, bookTitle, userEmail);

        String payload = """
                {
                  "eventId": "%s",
                  "eventType": "BookAvailable",
                  "data": {
                    "bookId": %s,
                    "bookTitle": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "occurredAt": "%s"
                  }
                }
                """.formatted(eventId, bookId, bookTitle, userEmail, userName, Instant.now());

        snsClient.publish(PublishRequest.builder()
                        .topicArn(topicArn)
                        .message(payload)
                        .build())
                .whenComplete((response, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BookAvailable event | eventId={} | bookId={} | userEmail={} | error={}",
                                eventId, bookId, userEmail, ex.getMessage());
                        throw new RuntimeException("Failed to publish BookAvailable event", ex);
                    }
                    log.info("BookAvailable event published successfully | eventId={} | messageId={}",
                            eventId, response.messageId());
                });
    }
}