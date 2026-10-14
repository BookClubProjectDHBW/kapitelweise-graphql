package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.InvitationDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class InvitationService {
    private final InvitationDao invitationDao;
    private final UserDao userDao;
    private final BookclubDao bookclubDao;

    public InvitationService(InvitationDao invitationDao, UserDao userDao, BookclubDao bookclubDao){
        this.invitationDao = invitationDao;
        this.userDao = userDao;
        this.bookclubDao = bookclubDao;
    }



    public List<InvitationUserResponse> getInvitations(String username){
        return invitationDao.getUserInvitations(username);
    }
}