package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.InvitationDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Invitation;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.InvitationStatus;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InvitationService {
    private final InvitationDao invitationDao;

    public InvitationService(InvitationDao invitationDao){
        this.invitationDao = invitationDao;
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

        invitationDao.addMember(invitation.invitedUserId(), invitation.bookclub().getId());
        return invitation.bookclub();
    }


    public List<InvitationUserResponse> getInvitations(String username){
        return invitationDao.getUserInvitations(username);
    }
}
