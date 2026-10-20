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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InvitationService {
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diese Einladung wurde nicht gefunden"));

        if(!invitationDao.answerIfPending(invitationId, status)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diese Einladung wurde bereits beantwortet");
        }

        if(status == InvitationStatus.DECLINED){
            return null;
        }

        BookClub bookclub = invitation.bookclub();
        invitationDao.addMember(invitation.invitedUserId(), bookclub.getId());
        amazonSQSService.sendNewMember("MEMBER_JOINED", invitation.invitedUserId(), username, bookclub.getId(), "MEMBER");
        return new BookClubResponse(bookclub.getId(), bookclub.getName(), bookclub.getDescription());
    }


    @Transactional
    public InvitationResponse registerNewInvitation(Long clubId, String invitedUsername, String username){
        if(!bookclubDao.bookclubExists(clubId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Buchclub nicht gefunden");
        }
        if(invitedUsername == null || invitedUsername.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bitte gib einen Benutzernamen ein.");
        }
        Long invitedById = userDao.getUserId(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Diese UserId existiert nicht."));

        if(!invitationDao.isUserAdmin(invitedById, clubId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Du bist kein Admin!");
        }

        Long invitedUserId = userDao.getUserId(invitedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden."));


        if(invitationDao.invitationPending(invitedUserId, clubId)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der User wurde schon eingeladen du Vogel");
        }

        if(invitationDao.isAlreadyMember(invitedUserId, clubId)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der User ist schon Member du Vogel");
        }
        try{
            return invitationDao.createInvitation(clubId, invitedUserId, invitedById, invitedUsername);
        }
        catch(DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Der User wurde schon eingeladen.");
        }
    }


    public List<InvitationUserResponse> getInvitations(String username){
        return invitationDao.getUserInvitations(username);
    }
}
