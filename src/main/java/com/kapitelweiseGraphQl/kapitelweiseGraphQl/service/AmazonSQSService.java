package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.MemberEvent;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

@Service
public class AmazonSQSService {

    private static final Logger log = LoggerFactory.getLogger(AmazonSQSService.class);


    private final AmazonSQS sqs;
    private final UserDao userDao;
    private final JsonMapper jsonMapper;
    private static final String USER_QUEUE_URL = "https://sqs.eu-north-1.amazonaws.com/608531435564/userDetails.fifo";
    private static final String MEMBER_QUEUE_URL = "https://sqs.eu-north-1.amazonaws.com/608531435564/clubMembers.fifo";

    public AmazonSQSService(AmazonSQS sqs, UserDao userDao, JsonMapper jsonMapper) {
        this.sqs = sqs;
        this.userDao = userDao;
        this.jsonMapper = jsonMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void pollNewUser(){
        ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withQueueUrl(USER_QUEUE_URL)
                .withWaitTimeSeconds(10)
                .withMaxNumberOfMessages(5);

        List<Message> messages = sqs.receiveMessage(request).getMessages();
        for (Message message : messages) {
            log.info("SQS-Nachricht empfangen: id={}, body={}", message.getMessageId(), message.getBody());
            try {
                UserEvent event = jsonMapper.readValue(message.getBody(), UserEvent.class);

                if ("USER_CREATED".equals(event.type())) {
                    userDao.upsertUser(event.userId(), event.username());
                }
                sqs.deleteMessage(USER_QUEUE_URL, message.getReceiptHandle());
            } catch (Exception e) {
                log.error("SQS-Nachricht konnte nicht verarbeitet werden: id={}", message.getMessageId(), e);
            }
        }
    }

    public void sendNewMember(String type, Long userId, String username, Long bookclubId, String role){

        try {
            String body = jsonMapper.writeValueAsString(new MemberEvent(type, userId, username, bookclubId, role));
            SendMessageRequest send = new SendMessageRequest()
                    .withQueueUrl(MEMBER_QUEUE_URL)
                    .withMessageBody(body)
                    .withMessageGroupId(String.valueOf(bookclubId))
                    .withMessageDeduplicationId(UUID.randomUUID().toString());
            sqs.sendMessage(send);
        } catch (Exception e) {
            log.error("MemberEvent konnte nicht gesendet werden: {}", e.getMessage());
        }
    }
}
