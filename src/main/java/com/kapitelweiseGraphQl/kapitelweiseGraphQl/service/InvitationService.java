package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.InvitationDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Invitation;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.InvitationStatus;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InvitationService {
    private static final Logger log = LoggerFactory.getLogger(InvitationService.class);

    private final InvitationDao invitationDao;
    private final UserDao userDao;
    private final BookclubDao bookclubDao;
    private final AmazonSQSService amazonSQSService;

    public InvitationService(InvitationDao invitationDao, UserDao userDao, BookclubDao bookclubDao, AmazonSQSService amazonSQSService){
        this.invitationDao = invitationDao;
        this.userDao = userDao;
        this.bookclubDao = bookclubDao;
        this.amazonSQSService = amazonSQSService;
    }


    // gibt beim Annehmen den Club zurück, beim Ablehnen null
    @Transactional
    public BookClubResponse responseInvitation(Long invitationId, InvitationStatus status, String username){

        Invitation invitation = invitationDao.findInvitation(invitationId, username)
                .orElseThrow(() -> {
                    log.warn("Einladung " + invitationId + " für " + username + " gibts nicht");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Diese Einladung wurde nicht gefunden");
                });

        if(!invitationDao.answerIfPending(invitationId, status)){
            log.warn(username + " wollte Einladung " + invitationId + " nochmal beantworten, ist aber schon durch");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diese Einladung wurde bereits beantwortet");
        }

        if(status == InvitationStatus.DECLINED){
            log.info(username + " hat Einladung " + invitationId + " abgelehnt, schade");
            return null;
        }

        BookClub bookclub = invitation.bookclub();
        invitationDao.addMember(invitation.invitedUserId(), bookclub.getId());
        log.info(username + " hat angenommen und ist jetzt in Club " + bookclub.getId());
        amazonSQSService.sendNewMember("MEMBER_JOINED", invitation.invitedUserId(), username, bookclub.getId(), "MEMBER");
        return new BookClubResponse(bookclub.getId(), bookclub.getName(), bookclub.getDescription());
    }


    @Transactional
    public InvitationResponse registerNewInvitation(Long clubId, String invitedUsername, String username){
        if(!bookclubDao.bookclubExists(clubId)){
            log.warn(username + " will in Club " + clubId + " einladen, den gibts aber nicht");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Buchclub nicht gefunden");
        }
        if(invitedUsername == null || invitedUsername.isBlank()){
            log.warn(username + " hat eine Einladung ohne Benutzernamen geschickt");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bitte gib einen Benutzernamen ein.");
        }
        Long invitedById = userDao.getUserId(username)
                .orElseThrow(() -> {
                    log.warn(username + " ist hier nicht bekannt");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Diese UserId existiert nicht.");
                });

        if(!invitationDao.isUserAdmin(invitedById, clubId)){
            log.warn(username + " ist kein Admin von Club " + clubId + " und wollte trotzdem einladen");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Du bist kein Admin!");
        }

        Long invitedUserId = userDao.getUserId(invitedUsername)
                .orElseThrow(() -> {
                    log.warn(invitedUsername + " gibts nicht, Einladung von " + username + " geht ins Leere");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden.");
                });


        if(invitationDao.invitationPending(invitedUserId, clubId)){
            log.warn(invitedUsername + " ist für Club " + clubId + " schon eingeladen");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der User wurde schon eingeladen du Vogel");
        }

        if(invitationDao.isAlreadyMember(invitedUserId, clubId)){
            log.warn(invitedUsername + " ist schon in Club " + clubId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der User ist schon Member du Vogel");
        }
        try{
            InvitationResponse response = invitationDao.createInvitation(clubId, invitedUserId, invitedById, invitedUsername);
            log.info(username + " hat " + invitedUsername + " in Club " + clubId + " eingeladen");
            return response;
        }
        catch(DuplicateKeyException e) {
            log.warn("Doppelte Einladung für " + invitedUsername + " in Club " + clubId + ", da war wer schneller");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der User wurde schon eingeladen.");
        }
    }


    public List<InvitationUserResponse> getInvitations(String username){
        List<InvitationUserResponse> invitations = invitationDao.getUserInvitations(username);
        log.info(username + " hat " + invitations.size() + " offene Einladungen");
        return invitations;
    }
}
