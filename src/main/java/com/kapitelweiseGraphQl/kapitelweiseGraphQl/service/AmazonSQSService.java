package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.MemberEvent;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.UserEvent;

import tools.jackson.databind.json.JsonMapper;

@Service
public class AmazonSQSService {

    private static final Logger log = LoggerFactory.getLogger(AmazonSQSService.class);


    private final AmazonSQS sqs;
    private final UserDao userDao;
    private final BookclubDao bookclubDao;
    private final JsonMapper jsonMapper;
    private static final String USER_QUEUE_URL = "https://sqs.eu-north-1.amazonaws.com/608531435564/userDetails.fifo";
    private static final String MEMBER_QUEUE_URL = "https://sqs.eu-north-1.amazonaws.com/608531435564/clubMembers.fifo";

    public AmazonSQSService(AmazonSQS sqs, UserDao userDao, BookclubDao bookclubDao, JsonMapper jsonMapper) {
        this.sqs = sqs;
        this.userDao = userDao;
        this.bookclubDao = bookclubDao;
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
            log.info("SQS-Nachricht von User Queue empfangen: " + message.getBody());
            try {
                UserEvent event = jsonMapper.readValue(message.getBody(), UserEvent.class);

                if (event.type().equals("USER_CREATED")) {
                    userDao.upsertUser(event.userId(), event.username());
                    log.info(event.username() + " ist jetzt auch hier angekommen");
                } else {
                    log.warn("Unbekannter Event-Typ in userDetails: " + event.type() + ", keine Ahnung was das soll");
                }
                sqs.deleteMessage(USER_QUEUE_URL, message.getReceiptHandle());
            } catch (Exception e) {
                log.error("SQS-Nachricht konnte nicht verarbeitet werden: " + e.getMessage());
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
            bookclubDao.markMemberSent(userId, bookclubId);
            log.info(username + " als " + role + " von Club " + bookclubId + " in die Queue gelegt");
        } catch (Exception e) {
            log.error("MemberEvent für " + username + " konnte nicht gesendet werden, probieren wir gleich nochmal: " + e.getMessage());
        }
    }

    public void sendMemberDeparture(Long userId, String username, Long bookclubId) {
    try {
        String body = jsonMapper.writeValueAsString(
                new MemberEvent("MEMBER_LEFT", userId, username, bookclubId, "MEMBER")
        );

        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(MEMBER_QUEUE_URL)
                .withMessageBody(body)
                .withMessageGroupId(String.valueOf(bookclubId))
                .withMessageDeduplicationId(UUID.randomUUID().toString());

        sqs.sendMessage(request);

        log.info(username + " hat Club " + bookclubId + " verlassen. Event gesendet.");
    } catch (Exception e) {
        throw new IllegalStateException("Buchclub-Austritt konnte nicht übertragen werden.", e);
    }
}

    @Scheduled(fixedDelay = 60000)
    public void sendMemberAgain(){
        List<MemberEvent> unsent = bookclubDao.getUnsentMembers();
        for (MemberEvent event : unsent) {
            log.warn(event.username() + " hängt noch in Club " + event.bookclubId() + ", schicken wir nochmal los");
            sendNewMember(event.type(), event.userId(), event.username(), event.bookclubId(), event.role());
        }
        
    }
}
